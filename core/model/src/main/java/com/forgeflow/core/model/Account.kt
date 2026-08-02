package com.forgeflow.core.model

enum class AccountProvider {
    GOOGLE,
    EMAIL,
}

enum class AccountSyncState {
    LOCAL_ONLY,
    SYNCING,
    SYNCED,
    ERROR,
}

data class AccountSession(
    val userId: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
    val providers: Set<AccountProvider>,
    val hasPassword: Boolean,
    val syncState: AccountSyncState,
)
