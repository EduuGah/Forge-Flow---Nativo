package com.forgeflow.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import com.forgeflow.core.model.AccentColor

internal val LightOnPrimary = Color.White
internal val LightSecondary = Color(0xFF475569)
internal val LightOnSecondary = Color.White
internal val LightBackground = Color(0xFFF7F7FB)
internal val LightSurface = Color(0xFFFFFFFF)
internal val LightSurfaceVariant = Color(0xFFF0F2F6)
internal val LightOnSurface = Color(0xFF111827)
internal val LightOnSurfaceVariant = Color(0xFF4B5563)
internal val LightOutline = Color(0xFFCBD1DC)
internal val LightError = Color(0xFFBA1A1A)
internal val LightSuccess = Color(0xFF047857)
internal val LightWarning = Color(0xFFB45309)

internal val DarkOnPrimary = Color.White
internal val DarkSecondary = Color(0xFFA1A1AA)
internal val DarkOnSecondary = Color(0xFF111214)
internal val DarkBackground = Color.Black
internal val DarkSurface = Color(0xFF18191B)
internal val DarkSurfaceVariant = Color(0xFF222327)
internal val DarkOnSurface = Color.White
internal val DarkOnSurfaceVariant = Color(0xFFA1A1AA)
internal val DarkOutline = Color(0xFF34363B)
internal val DarkError = Color(0xFFFFB4AB)
internal val DarkSuccess = Color(0xFF6EE7B7)
internal val DarkWarning = Color(0xFFFCD34D)

internal data class AccentPalette(
    val primary: Color,
    val lightText: Color,
    val darkText: Color,
)

internal fun AccentColor.palette(): AccentPalette = when (this) {
    AccentColor.BLUE -> AccentPalette(Color(0xFF3B82F6), Color(0xFF1D4ED8), Color(0xFF93C5FD))
    AccentColor.CYAN -> AccentPalette(Color(0xFF06B6D4), Color(0xFF0E7490), Color(0xFF67E8F9))
    AccentColor.TEAL -> AccentPalette(Color(0xFF14B8A6), Color(0xFF0F766E), Color(0xFF5EEAD4))
    AccentColor.GREEN -> AccentPalette(Color(0xFF10B981), Color(0xFF047857), Color(0xFF6EE7B7))
    AccentColor.LIME -> AccentPalette(Color(0xFF84CC16), Color(0xFF4D7C0F), Color(0xFFBEF264))
    AccentColor.AMBER -> AccentPalette(Color(0xFFF59E0B), Color(0xFFB45309), Color(0xFFFCD34D))
    AccentColor.ORANGE -> AccentPalette(Color(0xFFF97316), Color(0xFFC2410C), Color(0xFFFDBA74))
    AccentColor.RED -> AccentPalette(Color(0xFFEF4444), Color(0xFFB91C1C), Color(0xFFFCA5A5))
    AccentColor.ROSE -> AccentPalette(Color(0xFFF43F5E), Color(0xFFBE123C), Color(0xFFFDA4AF))
    AccentColor.PINK -> AccentPalette(Color(0xFFEC4899), Color(0xFFBE185D), Color(0xFFF9A8D4))
    AccentColor.PURPLE -> AccentPalette(Color(0xFF8B5CF6), Color(0xFF6D28D9), Color(0xFFC4B5FD))
    AccentColor.INDIGO -> AccentPalette(Color(0xFF6366F1), Color(0xFF4338CA), Color(0xFFA5B4FC))
}
