package com.forgeflow.app.auth

import android.content.Context
import com.forgeflow.core.data.auth.EntitlementRepository
import com.forgeflow.core.model.AccountEntitlement
import com.forgeflow.core.model.SupporterTier
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@Singleton
class FirebaseEntitlementRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : EntitlementRepository {
    override fun observeEntitlement(): Flow<AccountEntitlement?> = callbackFlow {
        if (FirebaseApp.getApps(context).isEmpty()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        var documentListener: ListenerRegistration? = null
        fun observeUser(uid: String?) {
            documentListener?.remove()
            documentListener = null
            if (uid == null) {
                trySend(null)
                return
            }
            documentListener = FirebaseFirestore.getInstance()
                .collection("entitlements")
                .document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        trySend(null)
                        return@addSnapshotListener
                    }
                    val tier = snapshot.getString("supporterTier")
                        ?.let { value ->
                            SupporterTier.entries.firstOrNull {
                                it.name.equals(value, ignoreCase = true)
                            }
                        }
                    trySend(tier?.let(::AccountEntitlement))
                }
        }
        val auth = FirebaseAuth.getInstance()
        val authListener = FirebaseAuth.AuthStateListener { observeUser(it.currentUser?.uid) }
        auth.addAuthStateListener(authListener)
        observeUser(auth.currentUser?.uid)
        awaitClose {
            documentListener?.remove()
            auth.removeAuthStateListener(authListener)
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class EntitlementModule {
    @Binds
    @Singleton
    abstract fun bindEntitlementRepository(
        implementation: FirebaseEntitlementRepository,
    ): EntitlementRepository
}
