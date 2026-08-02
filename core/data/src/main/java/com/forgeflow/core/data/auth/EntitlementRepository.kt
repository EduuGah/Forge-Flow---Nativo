package com.forgeflow.core.data.auth

import com.forgeflow.core.model.AccountEntitlement
import kotlinx.coroutines.flow.Flow

interface EntitlementRepository {
    fun observeEntitlement(): Flow<AccountEntitlement?>
}
