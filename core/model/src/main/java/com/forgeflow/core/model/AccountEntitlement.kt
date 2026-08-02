package com.forgeflow.core.model

enum class SupporterTier {
    SUPPORTER,
    FOUNDER,
    LIFETIME,
}

data class AccountEntitlement(
    val supporterTier: SupporterTier,
)
