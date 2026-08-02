package com.forgeflow.core.data.backup

import com.forgeflow.core.common.result.DataResult

interface CloudBackupRepository {
    fun isConfigured(): Boolean

    suspend fun uploadLatest(): DataResult<Unit>

    suspend fun prepareLatestRestore(): DataResult<Unit>
}
