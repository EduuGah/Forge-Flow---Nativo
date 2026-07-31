package com.forgeflow.app.navigation

import android.content.Context
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.forgeflow.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppShortcutInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun initialize() {
        val shortcuts = listOf(
            ShortcutInfoCompat.Builder(context, SHORTCUT_WORKOUT)
                .setShortLabel(context.getString(R.string.shortcut_workout_short))
                .setLongLabel(context.getString(R.string.shortcut_workout_long))
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_workout))
                .setIntent(context.forgeFlowLaunchIntent(AppLaunchDestination.ROUTINES))
                .build(),
            ShortcutInfoCompat.Builder(context, SHORTCUT_HISTORY)
                .setShortLabel(context.getString(R.string.shortcut_history_short))
                .setLongLabel(context.getString(R.string.shortcut_history_long))
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_history))
                .setIntent(context.forgeFlowLaunchIntent(AppLaunchDestination.HISTORY))
                .build(),
        )
        ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
    }

    private companion object {
        const val SHORTCUT_WORKOUT = "forgeflow_workout"
        const val SHORTCUT_HISTORY = "forgeflow_history"
    }
}
