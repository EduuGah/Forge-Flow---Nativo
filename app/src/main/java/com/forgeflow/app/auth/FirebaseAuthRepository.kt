package com.forgeflow.app.auth

import android.content.Context
import com.forgeflow.core.common.result.AppError
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.auth.AuthRepository
import com.forgeflow.core.model.AccountProvider
import com.forgeflow.core.model.AccountSession
import com.forgeflow.core.model.AccountSyncState
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class FirebaseAuthRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : AuthRepository {
    override fun isConfigured(): Boolean = FirebaseApp.getApps(context).isNotEmpty()

    override fun observeSession(): Flow<AccountSession?> = callbackFlow {
        if (!isConfigured()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener { current ->
            trySend(current.currentUser?.toAccountSession())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithGoogleIdToken(
        idToken: String,
    ): DataResult<AccountSession> {
        if (!isConfigured()) return DataResult.Failure(AppError.WriteFailed)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = suspendCancellableCoroutine { continuation ->
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    continuation.resume(task.takeIf { it.isSuccessful }?.result?.user)
                }
        }
        return result?.let { DataResult.Success(it.toAccountSession()) }
            ?: DataResult.Failure(AppError.WriteFailed)
    }

    override suspend fun signOut(): DataResult<Unit> = runCatching {
        if (isConfigured()) FirebaseAuth.getInstance().signOut()
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun deleteAccount(): DataResult<Unit> {
        if (!isConfigured()) return DataResult.Failure(AppError.WriteFailed)
        val deleted = suspendCancellableCoroutine { continuation ->
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                continuation.resume(false)
            } else {
                user.delete().addOnCompleteListener { continuation.resume(it.isSuccessful) }
            }
        }
        return if (deleted) DataResult.Success(Unit) else DataResult.Failure(AppError.WriteFailed)
    }
}

private fun FirebaseUser.toAccountSession(): AccountSession = AccountSession(
    userId = uid,
    displayName = displayName,
    email = email,
    photoUrl = photoUrl?.toString(),
    providers = setOf(AccountProvider.GOOGLE),
    syncState = AccountSyncState.LOCAL_ONLY,
)

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(implementation: FirebaseAuthRepository): AuthRepository
}
