package com.forgeflow.core.data.auth

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

fun AuthRepository.observeCurrentUserId() = observeSession()
    .map { session -> session?.userId }
    .distinctUntilChanged()

suspend fun AuthRepository.requireCurrentUserId(): String =
    requireNotNull(observeSession().first()?.userId) { "An authenticated account is required" }
