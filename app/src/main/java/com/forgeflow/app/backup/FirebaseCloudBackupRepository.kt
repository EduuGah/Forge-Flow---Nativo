package com.forgeflow.app.backup

import android.content.Context
import android.net.Uri
import com.forgeflow.core.common.result.AppError
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.backup.CloudBackupRepository
import com.forgeflow.core.data.backup.LocalDataExportRepository
import com.forgeflow.core.data.backup.PendingLocalDataRestore
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

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
            val metadata = StorageMetadata.Builder()
                .setContentType("application/zip")
                .setCustomMetadata("formatVersion", "1")
                .setCustomMetadata("ownerUid", user.uid)
                .build()
            val uploaded = backupReference(user.uid)
                .putFile(Uri.fromFile(file), metadata)
                .awaitSuccess()
            if (uploaded) DataResult.Success(Unit) else DataResult.Failure(AppError.WriteFailed)
        } finally {
            file.delete()
        }
    }

    override suspend fun prepareLatestRestore(): DataResult<Unit> {
        val user = currentUser() ?: return DataResult.Failure(AppError.WriteFailed)
        val directory = File(context.cacheDir, "cloud_restores").apply { mkdirs() }
        val downloaded = File.createTempFile("forgeflow-cloud-", ".zip", directory)
        return try {
            if (!backupReference(user.uid).getFile(downloaded).awaitSuccess()) {
                DataResult.Failure(AppError.WriteFailed)
            } else {
                runCatching { PendingLocalDataRestore.prepare(context, downloaded) }.fold(
                    onSuccess = { DataResult.Success(Unit) },
                    onFailure = { DataResult.Failure(AppError.WriteFailed) },
                )
            }
        } finally {
            downloaded.delete()
        }
    }

    private fun currentUser() = if (isConfigured()) {
        FirebaseAuth.getInstance().currentUser
    } else {
        null
    }

    private fun backupReference(uid: String) = FirebaseStorage.getInstance()
        .reference
        .child("users")
        .child(uid)
        .child("backups")
        .child("latest.zip")
}

private suspend fun com.google.android.gms.tasks.Task<*>.awaitSuccess(): Boolean =
    suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (continuation.isActive) continuation.resume(task.isSuccessful)
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
