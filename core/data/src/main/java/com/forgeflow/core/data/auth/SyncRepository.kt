package com.forgeflow.core.data.auth

import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.model.AccountSyncState
import kotlinx.coroutines.flow.Flow

interface SyncRepository {
    fun observeSyncState(): Flow<AccountSyncState>

    suspend fun enqueueLocalChanges(): DataResult<Unit>

    suspend fun mergeLocalAndRemote(): DataResult<Unit>
}
