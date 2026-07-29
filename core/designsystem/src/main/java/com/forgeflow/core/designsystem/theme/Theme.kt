package com.forgeflow.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.forgeflow.core.model.AccentColor

@Immutable
data class ForgeFlowSemanticColors(
    val success: Color,
    val warning: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = LightOnPrimary,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = LightError,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = DarkError,
)

private val LightSemanticColors = ForgeFlowSemanticColors(
    success = LightSuccess,
    warning = LightWarning,
    textPrimary = LightOnSurface,
    textSecondary = LightOnSurfaceVariant,
    divider = LightOutline.copy(alpha = 0.35f),
)

private val DarkSemanticColors = ForgeFlowSemanticColors(
    success = DarkSuccess,
    warning = DarkWarning,
    textPrimary = DarkOnSurface,
    textSecondary = DarkOnSurfaceVariant,
    divider = DarkOutline.copy(alpha = 0.45f),
)

private val LocalSemanticColors = staticCompositionLocalOf { LightSemanticColors }

object ForgeFlowDesign {
    val spacing: ForgeFlowSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalForgeFlowSpacing.current

    val colors: ForgeFlowSemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSemanticColors.current
}

@Composable
fun ForgeFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColor: AccentColor = AccentColor.BLUE,
    content: @Composable () -> Unit,
) {
    val accent = accentColor.palette()
    val baseColors = if (darkTheme) DarkColors else LightColors
    val colorScheme = baseColors.copy(
        primary = accent.primary,
        onPrimary = Color.White,
        primaryContainer = accent.primary.copy(alpha = if (darkTheme) 0.22f else 0.12f),
        onPrimaryContainer = if (darkTheme) accent.darkText else accent.lightText,
    )
    val semanticColors = if (darkTheme) DarkSemanticColors else LightSemanticColors
    val shapes = Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(6.dp),
        medium = RoundedCornerShape(8.dp),
        large = RoundedCornerShape(8.dp),
        extraLarge = RoundedCornerShape(8.dp),
    )

    androidx.compose.runtime.CompositionLocalProvider(
        LocalForgeFlowSpacing provides ForgeFlowSpacing(),
        LocalSemanticColors provides semanticColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ForgeFlowTypography,
            shapes = shapes,
            content = content,
        )
    }
}
