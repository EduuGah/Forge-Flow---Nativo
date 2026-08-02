package com.forgeflow.app.backup

import android.content.Context
import com.forgeflow.core.common.result.AppError
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.backup.CloudBackupRepository
import com.forgeflow.core.data.backup.LocalDataExportRepository
import com.forgeflow.core.data.backup.PendingLocalDataRestore
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@Singleton
class FirebaseCloudBackupRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val localDataExportRepository: LocalDataExportRepository,
) : CloudBackupRepository {
    override fun isConfigured(): Boolean = FirebaseApp.getApps(context).isNotEmpty()

    override suspend fun uploadLatest(): DataResult<Unit> {
        val user = currentUser() ?: return DataResult.Failure(AppError.WriteFailed)
        val localExport = localDataExportRepository.createTemporaryExport()
        val file = (localExport as? DataResult.Success)?.value
            ?: return DataResult.Failure(AppError.WriteFailed)
        return try {
            runCatching {
                if (file.length() !in 1..MAX_BACKUP_SIZE_BYTES) {
                    DataResult.Failure(AppError.WriteFailed)
                } else if (uploadVersion(user.uid, file)) {
                    DataResult.Success(Unit)
                } else {
                    DataResult.Failure(AppError.WriteFailed)
                }
            }.getOrElse { DataResult.Failure(AppError.WriteFailed) }
        } finally {
            file.delete()
        }
    }

    override suspend fun prepareLatestRestore(): DataResult<Unit> {
        val user = currentUser() ?: return DataResult.Failure(AppError.WriteFailed)
        val directory = File(context.cacheDir, "cloud_restores").apply { mkdirs() }
        val downloaded = File.createTempFile("forgeflow-cloud-", ".zip", directory)
        return try {
            runCatching {
                if (!downloadLatest(user.uid, downloaded)) {
                    DataResult.Failure(AppError.WriteFailed)
                } else {
                    runCatching { PendingLocalDataRestore.prepare(context, downloaded) }.fold(
                        onSuccess = { DataResult.Success(Unit) },
                        onFailure = { DataResult.Failure(AppError.WriteFailed) },
                    )
                }
            }.getOrElse { DataResult.Failure(AppError.WriteFailed) }
        } finally {
            downloaded.delete()
        }
    }

    private suspend fun uploadVersion(uid: String, file: File): Boolean {
        val latest = latestDocument(uid)
        val previousVersion = latest.get().awaitResult()?.getString(ACTIVE_VERSION)
        val version = "${System.currentTimeMillis()}-${UUID.randomUUID()}"
        val versionDocument = latest.collection(VERSIONS_COLLECTION).document(version)
        val digest = MessageDigest.getInstance(SHA_256)
        var chunkCount = 0
        var uploaded = true

        withContext(Dispatchers.IO) {
            file.inputStream().buffered().use { input ->
                val buffer = ByteArray(CHUNK_SIZE_BYTES)
                while (uploaded) {
                    val read = input.readChunk(buffer)
                    if (read <= 0) break
                    digest.update(buffer, 0, read)
                    val chunk = if (read == buffer.size) buffer.clone() else buffer.copyOf(read)
                    uploaded = versionDocument.collection(CHUNKS_COLLECTION)
                        .document(chunkCount.toString().padStart(CHUNK_ID_WIDTH, '0'))
                        .set(mapOf(CHUNK_DATA to Blob.fromBytes(chunk)))
                        .awaitSuccess()
                    chunkCount += 1
                }
            }
        }
        if (!uploaded || chunkCount == 0 || chunkCount > MAX_CHUNK_COUNT) {
            deleteVersion(versionDocument)
            return false
        }

        val metadata = mapOf(
            OWNER_UID to uid,
            FORMAT_VERSION to CURRENT_FORMAT_VERSION,
            CHUNK_COUNT to chunkCount,
            SIZE_BYTES to file.length(),
            SHA_256_FIELD to digest.digest().toHexString(),
            CREATED_AT_EPOCH_MILLIS to System.currentTimeMillis(),
        )
        if (!versionDocument.set(metadata).awaitSuccess()) {
            deleteVersion(versionDocument)
            return false
        }
        if (!latest.set(metadata + (ACTIVE_VERSION to version)).awaitSuccess()) {
            deleteVersion(versionDocument)
            return false
        }
        if (previousVersion != null && previousVersion != version) {
            deleteVersion(latest.collection(VERSIONS_COLLECTION).document(previousVersion))
        }
        return true
    }

    private suspend fun downloadLatest(uid: String, target: File): Boolean {
        val latest = latestDocument(uid).get().awaitResult() ?: return false
        val version = latest.getString(ACTIVE_VERSION) ?: return false
        val versionDocument = latest.reference.collection(VERSIONS_COLLECTION).document(version)
        val metadata = versionDocument.get().awaitResult() ?: return false
        if (!metadata.exists() || metadata.getString(OWNER_UID) != uid) return false
        val formatVersion = metadata.getLong(FORMAT_VERSION)?.toInt() ?: return false
        val chunkCount = metadata.getLong(CHUNK_COUNT)?.toInt() ?: return false
        val expectedSize = metadata.getLong(SIZE_BYTES) ?: return false
        val expectedDigest = metadata.getString(SHA_256_FIELD) ?: return false
        if (
            formatVersion != CURRENT_FORMAT_VERSION ||
            chunkCount !in 1..MAX_CHUNK_COUNT ||
            expectedSize !in 1..MAX_BACKUP_SIZE_BYTES
        ) {
            return false
        }

        val digest = MessageDigest.getInstance(SHA_256)
        var written = 0L
        val completed = withContext(Dispatchers.IO) {
            target.outputStream().buffered().use { output ->
                repeat(chunkCount) { index ->
                    val snapshot = versionDocument.collection(CHUNKS_COLLECTION)
                        .document(index.toString().padStart(CHUNK_ID_WIDTH, '0'))
                        .get()
                        .awaitResult()
                        ?: return@withContext false
                    val bytes = snapshot.getBlob(CHUNK_DATA)?.toBytes()
                        ?: return@withContext false
                    written += bytes.size
                    if (written > expectedSize || written > MAX_BACKUP_SIZE_BYTES) {
                        return@withContext false
                    }
                    digest.update(bytes)
                    output.write(bytes)
                }
                true
            }
        }
        return completed &&
            written == expectedSize &&
            digest.digest().toHexString().equals(expectedDigest, ignoreCase = true)
    }

    private suspend fun deleteVersion(versionDocument: DocumentReference) {
        val chunks = versionDocument.collection(CHUNKS_COLLECTION).get().awaitResult()
        chunks?.documents?.forEach { chunk -> chunk.reference.delete().awaitSuccess() }
        versionDocument.delete().awaitSuccess()
    }

    private fun latestDocument(uid: String) = FirebaseFirestore.getInstance()
        .collection(USERS_COLLECTION)
        .document(uid)
        .collection(BACKUPS_COLLECTION)
        .document(LATEST_DOCUMENT)

    private fun currentUser() = if (isConfigured()) {
        FirebaseAuth.getInstance().currentUser
    } else {
        null
    }

    private companion object {
        const val USERS_COLLECTION = "users"
        const val BACKUPS_COLLECTION = "backups"
        const val LATEST_DOCUMENT = "latest"
        const val VERSIONS_COLLECTION = "versions"
        const val CHUNKS_COLLECTION = "chunks"
        const val ACTIVE_VERSION = "activeVersion"
        const val OWNER_UID = "ownerUid"
        const val FORMAT_VERSION = "formatVersion"
        const val CHUNK_COUNT = "chunkCount"
        const val CHUNK_DATA = "data"
        const val SIZE_BYTES = "sizeBytes"
        const val SHA_256_FIELD = "sha256"
        const val CREATED_AT_EPOCH_MILLIS = "createdAtEpochMillis"
        const val SHA_256 = "SHA-256"
        const val CURRENT_FORMAT_VERSION = 1
        const val CHUNK_SIZE_BYTES = 700 * 1024
        const val CHUNK_ID_WIDTH = 4
        const val MAX_CHUNK_COUNT = 192
        const val MAX_BACKUP_SIZE_BYTES = CHUNK_SIZE_BYTES.toLong() * MAX_CHUNK_COUNT
    }
}

private fun java.io.InputStream.readChunk(buffer: ByteArray): Int {
    var total = 0
    while (total < buffer.size) {
        val read = read(buffer, total, buffer.size - total)
        if (read < 0) break
        total += read
    }
    return total
}

private fun ByteArray.toHexString(): String = joinToString(separator = "") { byte ->
    "%02x".format(byte.toInt() and 0xff)
}

private suspend fun Task<Void>.awaitSuccess(): Boolean =
    suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (continuation.isActive) continuation.resume(task.isSuccessful)
        }
    }

private suspend fun <T> Task<T>.awaitResult(): T? =
    suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (continuation.isActive) {
                continuation.resume(task.takeIf { it.isSuccessful }?.result)
            }
        }
    }

@Module
@InstallIn(SingletonComponent::class)
abstract class CloudBackupModule {
    @Binds
    @Singleton
    abstract fun bindCloudBackupRepository(
        implementation: FirebaseCloudBackupRepository,
    ): CloudBackupRepository
}
