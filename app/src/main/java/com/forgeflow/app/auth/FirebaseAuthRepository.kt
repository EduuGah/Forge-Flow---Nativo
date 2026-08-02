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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class FirebaseAuthRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : AuthRepository {
    private val session = MutableStateFlow<AccountSession?>(null)

    init {
        if (isConfigured()) {
            val auth = FirebaseAuth.getInstance()
            auth.useAppLanguage()
            session.value = auth.currentUser?.toAccountSession()
            auth.addAuthStateListener { current ->
                session.value = current.currentUser?.toAccountSession()
            }
        }
    }

    override fun isConfigured(): Boolean = FirebaseApp.getApps(context).isNotEmpty()

    override fun observeSession(): Flow<AccountSession?> = session.asStateFlow()

    override suspend fun signInWithGoogleIdToken(
        idToken: String,
    ): DataResult<AccountSession> {
        if (!isConfigured()) return DataResult.Failure(AppError.WriteFailed)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = suspendCancellableCoroutine { continuation ->
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (continuation.isActive) {
                        continuation.resume(task.takeIf { it.isSuccessful }?.result?.user)
                    }
                }
        }
        return result?.let { user -> success(user) }
            ?: DataResult.Failure(AppError.WriteFailed)
    }

    override suspend fun createAccountWithEmail(
        email: String,
        password: String,
    ): DataResult<AccountSession> {
        if (!isConfigured()) return DataResult.Failure(AppError.WriteFailed)
        val user = suspendCancellableCoroutine<FirebaseUser?> { continuation ->
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener { task ->
                    if (continuation.isActive) {
                        continuation.resume(task.takeIf { it.isSuccessful }?.result?.user)
                    }
                }
        } ?: return DataResult.Failure(AppError.WriteFailed)
        val verificationSent = user.sendEmailVerification().awaitSuccess()
        return if (verificationSent) success(user) else DataResult.Failure(AppError.WriteFailed)
    }

    override suspend fun signInWithEmail(
        email: String,
        password: String,
    ): DataResult<AccountSession> {
        if (!isConfigured()) return DataResult.Failure(AppError.WriteFailed)
        val user = suspendCancellableCoroutine { continuation ->
            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener { task ->
                    if (continuation.isActive) {
                        continuation.resume(task.takeIf { it.isSuccessful }?.result?.user)
                    }
                }
        }
        return user?.let(::success) ?: DataResult.Failure(AppError.WriteFailed)
    }

    override suspend fun sendEmailVerification(): DataResult<Unit> {
        if (!isConfigured()) return DataResult.Failure(AppError.WriteFailed)
        val user = FirebaseAuth.getInstance().currentUser
            ?: return DataResult.Failure(AppError.WriteFailed)
        return if (user.sendEmailVerification().awaitSuccess()) {
            DataResult.Success(Unit)
        } else {
            DataResult.Failure(AppError.WriteFailed)
        }
    }

    override suspend fun sendPasswordReset(email: String): DataResult<Unit> {
        if (!isConfigured()) return DataResult.Failure(AppError.WriteFailed)
        return if (
            FirebaseAuth.getInstance().sendPasswordResetEmail(email.trim()).awaitSuccess()
        ) {
            DataResult.Success(Unit)
        } else {
            DataResult.Failure(AppError.WriteFailed)
        }
    }

    override suspend fun linkPassword(password: String): DataResult<AccountSession> {
        if (!isConfigured()) return DataResult.Failure(AppError.WriteFailed)
        val user = FirebaseAuth.getInstance().currentUser
            ?: return DataResult.Failure(AppError.WriteFailed)
        val email = user.email ?: return DataResult.Failure(AppError.WriteFailed)
        val credential = EmailAuthProvider.getCredential(email, password)
        val linkedUser = suspendCancellableCoroutine { continuation ->
            user.linkWithCredential(credential).addOnCompleteListener { task ->
                if (continuation.isActive) {
                    continuation.resume(task.takeIf { it.isSuccessful }?.result?.user)
                }
            }
        }
        return linkedUser?.let(::success) ?: DataResult.Failure(AppError.WriteFailed)
    }

    override suspend fun refreshSession(): DataResult<AccountSession> {
        if (!isConfigured()) return DataResult.Failure(AppError.WriteFailed)
        val user = FirebaseAuth.getInstance().currentUser
            ?: return DataResult.Failure(AppError.WriteFailed)
        if (!user.reload().awaitSuccess()) return DataResult.Failure(AppError.WriteFailed)
        val refreshed = FirebaseAuth.getInstance().currentUser
            ?: return DataResult.Failure(AppError.WriteFailed)
        return success(refreshed)
    }

    override suspend fun signOut(): DataResult<Unit> = runCatching {
        if (isConfigured()) FirebaseAuth.getInstance().signOut()
        session.value = null
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun deleteAccount(): DataResult<Unit> {
        if (!isConfigured()) return DataResult.Failure(AppError.WriteFailed)
        val deleted = suspendCancellableCoroutine { continuation ->
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                if (continuation.isActive) continuation.resume(false)
            } else {
                user.delete().addOnCompleteListener {
                    if (continuation.isActive) continuation.resume(it.isSuccessful)
                }
            }
        }
        return if (deleted) DataResult.Success(Unit) else DataResult.Failure(AppError.WriteFailed)
    }

    private fun success(user: FirebaseUser): DataResult<AccountSession> {
        val accountSession = user.toAccountSession()
        session.value = accountSession
        return DataResult.Success(accountSession)
    }
}

private suspend fun com.google.android.gms.tasks.Task<*>.awaitSuccess(): Boolean =
    suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (continuation.isActive) continuation.resume(task.isSuccessful)
        }
    }

private fun FirebaseUser.toAccountSession(): AccountSession {
    val providers = providerData.mapNotNull { data ->
        when (data.providerId) {
            GoogleAuthProvider.PROVIDER_ID -> AccountProvider.GOOGLE
            EmailAuthProvider.PROVIDER_ID -> AccountProvider.EMAIL
            else -> null
        }
    }.toSet()
    return AccountSession(
        userId = uid,
        displayName = displayName,
        email = email,
        photoUrl = photoUrl?.toString(),
        providers = providers,
        isEmailVerified = isEmailVerified,
        hasPassword = AccountProvider.EMAIL in providers,
        syncState = AccountSyncState.LOCAL_ONLY,
    )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(implementation: FirebaseAuthRepository): AuthRepository
}
