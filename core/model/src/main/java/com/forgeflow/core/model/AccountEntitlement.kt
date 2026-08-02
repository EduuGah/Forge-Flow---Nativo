package com.forgeflow.core.model

enum class SupporterTier {
    ADMIN,
    SUPPORTER,
    PRO,
    FOUNDER,
    LIFETIME,
}

data class AccountEntitlement(
    val supporterTier: SupporterTier,
)
