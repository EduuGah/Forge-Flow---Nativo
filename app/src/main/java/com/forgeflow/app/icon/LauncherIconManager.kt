package com.forgeflow.app.icon

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.forgeflow.core.model.AccentColor
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.atomic.AtomicReference

@Singleton
class LauncherIconManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val packageManager = context.packageManager
    private val defaultAlias = component("Default")
    private val legacyBlueAlias = component("Blue")
    private val accentAliases = AccentColor.entries.associateWith { color ->
        if (color == AccentColor.BLUE) defaultAlias else component(color.aliasSuffix())
    }
    private val allAliases = (accentAliases.values + defaultAlias + legacyBlueAlias).distinct()
    private val updateQueue = LauncherIconUpdateQueue()

    fun scheduleUpdate(accentColor: AccentColor) {
        updateQueue.schedule(accentColor)
    }

    fun applyScheduledUpdate() {
        val accentColor = updateQueue.take() ?: return
        if (!applyUpdate(accentColor)) {
            updateQueue.retry(accentColor)
        }
    }

    private fun applyUpdate(accentColor: AccentColor): Boolean {
        val selectedAlias = accentAliases.getValue(accentColor)
        return runCatching {
            if (allAliases.all { component ->
                    component.isEffectivelyEnabled() == (component == selectedAlias)
                }
            ) {
                return@runCatching true
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.setComponentEnabledSettings(
                    allAliases.map { component ->
                        PackageManager.ComponentEnabledSetting(
                            component,
                            component.stateFor(selectedAlias),
                            PackageManager.DONT_KILL_APP,
                        )
                    },
                )
            } else {
                setEnabled(selectedAlias, enabled = true)
                allAliases
                    .filterNot { it == selectedAlias }
                    .forEach { setEnabled(it, enabled = false) }
            }
            true
        }.onFailure { error ->
            Log.e(TAG, "Unable to update launcher icon", error)
        }.getOrDefault(false)
    }

    private fun ComponentName.isEffectivelyEnabled(): Boolean =
        when (packageManager.getComponentEnabledSetting(this)) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> this == defaultAlias
            else -> false
        }

    private fun ComponentName.stateFor(selectedAlias: ComponentName): Int =
        if (this == selectedAlias) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }

    private fun setEnabled(component: ComponentName, enabled: Boolean) {
        val desiredState = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        if (packageManager.getComponentEnabledSetting(component) == desiredState) return
        packageManager.setComponentEnabledSetting(
            component,
            desiredState,
            PackageManager.DONT_KILL_APP,
        )
    }

    private fun component(suffix: String) = ComponentName(
        context.packageName,
        "${context.packageName}.launcher.$suffix",
    )

    private companion object {
        const val TAG = "LauncherIconManager"
    }
}

internal class LauncherIconUpdateQueue {
    private val pendingColor = AtomicReference<AccentColor?>()

    fun schedule(color: AccentColor) {
        pendingColor.set(color)
    }

    fun take(): AccentColor? = pendingColor.getAndSet(null)

    fun retry(color: AccentColor) {
        pendingColor.compareAndSet(null, color)
    }
}

internal fun AccentColor.aliasSuffix(): String = when (this) {
    AccentColor.BLUE -> "Blue"
    AccentColor.CYAN -> "Cyan"
    AccentColor.TEAL -> "Teal"
    AccentColor.GREEN -> "Green"
    AccentColor.LIME -> "Lime"
    AccentColor.AMBER -> "Amber"
    AccentColor.ORANGE -> "Orange"
    AccentColor.RED -> "Red"
    AccentColor.ROSE -> "Rose"
    AccentColor.PINK -> "Pink"
    AccentColor.PURPLE -> "Purple"
    AccentColor.INDIGO -> "Indigo"
}
