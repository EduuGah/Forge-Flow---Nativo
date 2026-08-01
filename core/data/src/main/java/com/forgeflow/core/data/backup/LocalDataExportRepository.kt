package com.forgeflow.core.data.backup

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.forgeflow.core.common.di.IoDispatcher
import com.forgeflow.core.common.result.AppError
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.database.ForgeFlowDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

interface LocalDataExportRepository {
    suspend fun export(targetUri: String): DataResult<Unit>
}

@Singleton
class DefaultLocalDataExportRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: ForgeFlowDatabase,
    private val dataStore: DataStore<Preferences>,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : LocalDataExportRepository {
    override suspend fun export(targetUri: String): DataResult<Unit> = withContext(ioDispatcher) {
        runCatching {
            dataStore.data.first()
            database.openHelper.writableDatabase
                .query("PRAGMA wal_checkpoint(FULL)")
                .close()
            val output = requireNotNull(
                context.contentResolver.openOutputStream(Uri.parse(targetUri), "w"),
            )
            ZipOutputStream(BufferedOutputStream(output)).use { archive ->
                archive.putNextEntry(ZipEntry("forgeflow-export.txt"))
                archive.write(
                    "ForgeFlow local export\ncreatedAt=${Instant.now()}\nformatVersion=1\n"
                        .toByteArray(),
                )
                archive.closeEntry()
                exportDatabaseFiles(archive)
                exportFile(
                    file = context.preferencesDataStoreFile(SETTINGS_FILE_NAME),
                    entryName = "datastore/$SETTINGS_FILE_NAME",
                    archive = archive,
                )
                EXPORT_DIRECTORIES.forEach { directoryName ->
                    exportDirectory(
                        root = File(context.filesDir, directoryName),
                        entryPrefix = "files/$directoryName",
                        archive = archive,
                    )
                }
            }
        }.fold(
            onSuccess = { DataResult.Success(Unit) },
            onFailure = { DataResult.Failure(AppError.WriteFailed) },
        )
    }

    private fun exportDatabaseFiles(archive: ZipOutputStream) {
        val databaseFile = context.getDatabasePath(ForgeFlowDatabase.NAME)
        listOf(
            databaseFile,
            File(databaseFile.path + "-wal"),
            File(databaseFile.path + "-shm"),
        ).forEach { file ->
            exportFile(file, "database/${file.name}", archive)
        }
    }

    private fun exportDirectory(root: File, entryPrefix: String, archive: ZipOutputStream) {
        if (!root.exists()) return
        val canonicalRoot = root.canonicalFile
        canonicalRoot.walkTopDown()
            .filter(File::isFile)
            .forEach { file ->
                val canonicalFile = file.canonicalFile
                check(canonicalFile.path.startsWith(canonicalRoot.path + File.separator))
                val relativePath = canonicalFile.relativeTo(canonicalRoot)
                    .invariantSeparatorsPath
                exportFile(canonicalFile, "$entryPrefix/$relativePath", archive)
            }
    }

    private fun exportFile(file: File, entryName: String, archive: ZipOutputStream) {
        if (!file.isFile) return
        archive.putNextEntry(ZipEntry(entryName))
        BufferedInputStream(file.inputStream()).use { input -> input.copyTo(archive) }
        archive.closeEntry()
    }

    private companion object {
        const val SETTINGS_FILE_NAME = "forgeflow_settings.preferences_pb"
        val EXPORT_DIRECTORIES = listOf(
            "profile",
            "progress_photos",
            "nutrition_photos",
            "exercise_media",
        )
    }
}
