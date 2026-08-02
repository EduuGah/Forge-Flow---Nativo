package com.forgeflow.core.data.backup

import android.content.Context
import androidx.datastore.preferences.preferencesDataStoreFile
import com.forgeflow.core.database.ForgeFlowDatabase
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipInputStream

object PendingLocalDataRestore {
    fun prepare(context: Context, source: File) {
        require(source.isFile)
        validateArchive(source)
        val restoreRoot = restoreRoot(context).apply { mkdirs() }
        source.copyTo(File(restoreRoot, PENDING_FILE), overwrite = true)
    }

    fun applyIfPending(context: Context): Boolean {
        val restoreRoot = restoreRoot(context)
        val pending = File(restoreRoot, PENDING_FILE)
        if (!pending.isFile) return false

        val staging = File(restoreRoot, "staging").apply {
            deleteRecursively()
            mkdirs()
        }
        val rollback = File(restoreRoot, "rollback").apply {
            deleteRecursively()
            mkdirs()
        }
        return runCatching {
            extract(pending, staging)
            require(File(staging, DATABASE_ENTRY).isFile)
            require(File(staging, DATASTORE_ENTRY).isFile)
            snapshotCurrentData(context, rollback)
            runCatching { install(context, staging) }
                .onFailure { restoreSnapshot(context, rollback) }
                .getOrThrow()
            true
        }.getOrDefault(false).also {
            pending.delete()
            staging.deleteRecursively()
            rollback.deleteRecursively()
        }
    }

    internal fun validateArchive(source: File) {
        val seen = mutableSetOf<String>()
        var totalBytes = 0L
        var entries = 0
        var hasManifest = false
        var hasDatabase = false
        var hasDataStore = false
        var manifest: String? = null
        ZipInputStream(BufferedInputStream(source.inputStream())).use { archive ->
            var entry = archive.nextEntry
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (entry != null) {
                if (!entry.isDirectory) {
                    require(isAllowed(entry.name) && seen.add(entry.name))
                    entries++
                    require(entries <= MAX_ENTRIES)
                    var entryBytes = 0L
                    val manifestOutput = if (entry.name == MANIFEST_ENTRY) {
                        ByteArrayOutputStream()
                    } else {
                        null
                    }
                    while (true) {
                        val read = archive.read(buffer)
                        if (read < 0) break
                        entryBytes += read
                        totalBytes += read
                        require(entryBytes <= MAX_ENTRY_BYTES && totalBytes <= MAX_TOTAL_BYTES)
                        manifestOutput?.write(buffer, 0, read)
                    }
                    if (manifestOutput != null) {
                        manifest = manifestOutput.toString(Charsets.UTF_8.name())
                    }
                    hasManifest = hasManifest || entry.name == MANIFEST_ENTRY
                    hasDatabase = hasDatabase || entry.name == DATABASE_ENTRY
                    hasDataStore = hasDataStore || entry.name == DATASTORE_ENTRY
                }
                archive.closeEntry()
                entry = archive.nextEntry
            }
        }
        require(hasManifest && hasDatabase && hasDataStore)
        require(manifest?.lineSequence()?.any { it == "formatVersion=1" } == true)
    }

    private fun extract(source: File, targetRoot: File) {
        validateArchive(source)
        val canonicalRoot = targetRoot.canonicalFile
        ZipInputStream(BufferedInputStream(source.inputStream())).use { archive ->
            var entry = archive.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val target = File(canonicalRoot, entry.name).canonicalFile
                    require(target.path.startsWith(canonicalRoot.path + File.separator))
                    target.parentFile?.mkdirs()
                    target.outputStream().use { output -> archive.copyTo(output) }
                }
                archive.closeEntry()
                entry = archive.nextEntry
            }
        }
    }

    private fun snapshotCurrentData(context: Context, rollback: File) {
        copyIfPresent(context.getDatabasePath(ForgeFlowDatabase.NAME), File(rollback, DATABASE_ENTRY))
        copyIfPresent(context.preferencesDataStoreFile(SETTINGS_FILE), File(rollback, DATASTORE_ENTRY))
        EXPORT_DIRECTORIES.forEach { name ->
            copyIfPresent(File(context.filesDir, name), File(rollback, "files/$name"))
        }
    }

    private fun install(context: Context, staging: File) {
        val database = context.getDatabasePath(ForgeFlowDatabase.NAME)
        replaceFile(File(staging, DATABASE_ENTRY), database)
        File(database.path + "-wal").delete()
        File(database.path + "-shm").delete()
        replaceFile(File(staging, DATASTORE_ENTRY), context.preferencesDataStoreFile(SETTINGS_FILE))
        EXPORT_DIRECTORIES.forEach { name ->
            val target = File(context.filesDir, name)
            target.deleteRecursively()
            val source = File(staging, "files/$name")
            if (source.exists()) source.copyRecursively(target, overwrite = true)
        }
    }

    private fun restoreSnapshot(context: Context, rollback: File) {
        val database = context.getDatabasePath(ForgeFlowDatabase.NAME)
        database.delete()
        copyIfPresent(File(rollback, DATABASE_ENTRY), database)
        val dataStore = context.preferencesDataStoreFile(SETTINGS_FILE)
        dataStore.delete()
        copyIfPresent(File(rollback, DATASTORE_ENTRY), dataStore)
        EXPORT_DIRECTORIES.forEach { name ->
            val target = File(context.filesDir, name)
            target.deleteRecursively()
            copyIfPresent(File(rollback, "files/$name"), target)
        }
    }

    private fun replaceFile(source: File, target: File) {
        require(source.isFile)
        target.parentFile?.mkdirs()
        val temporary = File(target.parentFile, "${target.name}.restore")
        source.copyTo(temporary, overwrite = true)
        if (target.exists()) require(target.delete())
        require(temporary.renameTo(target))
    }

    private fun copyIfPresent(source: File, target: File) {
        if (!source.exists()) return
        target.parentFile?.mkdirs()
        if (source.isDirectory) {
            source.copyRecursively(target, overwrite = true)
        } else {
            source.copyTo(target, overwrite = true)
        }
    }

    private fun isAllowed(name: String): Boolean =
        name == MANIFEST_ENTRY ||
            name == DATABASE_ENTRY ||
            name == "database/${ForgeFlowDatabase.NAME}-wal" ||
            name == "database/${ForgeFlowDatabase.NAME}-shm" ||
            name == DATASTORE_ENTRY ||
            EXPORT_DIRECTORIES.any { directory ->
                name.startsWith("files/$directory/") && name.length > "files/$directory/".length
            }

    private fun restoreRoot(context: Context): File = File(context.noBackupFilesDir, "restore")

    private const val PENDING_FILE = "pending.zip"
    private const val MANIFEST_ENTRY = "forgeflow-export.txt"
    private const val SETTINGS_FILE = "forgeflow_settings.preferences_pb"
    private const val DATABASE_ENTRY = "database/${ForgeFlowDatabase.NAME}"
    private const val DATASTORE_ENTRY = "datastore/$SETTINGS_FILE"
    private const val MAX_ENTRIES = 20_000
    private const val MAX_ENTRY_BYTES = 512L * 1024L * 1024L
    private const val MAX_TOTAL_BYTES = 1024L * 1024L * 1024L
    private val EXPORT_DIRECTORIES = listOf(
        "profile",
        "progress_photos",
        "nutrition_photos",
        "exercise_media",
    )
}
