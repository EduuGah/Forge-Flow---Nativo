package com.forgeflow.core.data.auth

import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.model.AccountSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun isConfigured(): Boolean

    fun observeSession(): Flow<AccountSession?>

    suspend fun signInWithGoogleIdToken(idToken: String): DataResult<AccountSession>

    suspend fun signOut(): DataResult<Unit>

    suspend fun deleteAccount(): DataResult<Unit>
}
