package com.forgeflow.app.navigation

import android.content.Context
import android.content.Intent
import com.forgeflow.app.MainActivity

enum class AppLaunchDestination(val value: String) {
    HOME("home"),
    ROUTINES("routines"),
    HISTORY("history"),
    ACTIVE_WORKOUT("active_workout"),
    NUTRITION("nutrition"),
}

data class AppLaunchRequest(
    val id: Long,
    val destination: AppLaunchDestination,
)

fun Context.forgeFlowLaunchIntent(destination: AppLaunchDestination): Intent =
    Intent(this, MainActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        putExtra(EXTRA_LAUNCH_DESTINATION, destination.value)
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }

fun Intent?.toAppLaunchRequest(id: Long): AppLaunchRequest? {
    val destinationValue = this?.getStringExtra(EXTRA_LAUNCH_DESTINATION) ?: return null
    val destination = AppLaunchDestination.entries
        .firstOrNull { it.value == destinationValue }
        ?: return null
    return AppLaunchRequest(id = id, destination = destination)
}

private const val EXTRA_LAUNCH_DESTINATION =
    "com.forgeflow.app.extra.LAUNCH_DESTINATION"
