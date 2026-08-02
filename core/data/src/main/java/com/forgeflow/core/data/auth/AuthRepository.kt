package com.forgeflow.core.data.auth

import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.model.AccountSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun isConfigured(): Boolean

    fun observeSession(): Flow<AccountSession?>

    suspend fun signInWithGoogleIdToken(idToken: String): DataResult<AccountSession>

    suspend fun createAccountWithEmail(
        email: String,
        password: String,
    ): DataResult<AccountSession>

    suspend fun signInWithEmail(
        email: String,
        password: String,
    ): DataResult<AccountSession>

    suspend fun sendEmailVerification(): DataResult<Unit>

    suspend fun sendPasswordReset(email: String): DataResult<Unit>

    suspend fun linkPassword(password: String): DataResult<AccountSession>

    suspend fun refreshSession(): DataResult<AccountSession>

    suspend fun signOut(): DataResult<Unit>

    suspend fun deleteAccount(): DataResult<Unit>
}
