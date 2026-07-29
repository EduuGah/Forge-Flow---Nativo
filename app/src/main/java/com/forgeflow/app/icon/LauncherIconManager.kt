package com.forgeflow.app.icon

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.forgeflow.core.model.AccentColor
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LauncherIconManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val packageManager = context.packageManager
    private val defaultAlias = component("Default")
    private val accentAliases = AccentColor.entries.associateWith { color ->
        component(color.aliasSuffix())
    }

    fun sync(accentColor: AccentColor) {
        val selectedAlias = accentAliases.getValue(accentColor)
        setEnabled(selectedAlias, enabled = true)
        (accentAliases.values + defaultAlias)
            .filterNot { it == selectedAlias }
            .forEach { setEnabled(it, enabled = false) }
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
}

private fun AccentColor.aliasSuffix(): String = when (this) {
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
