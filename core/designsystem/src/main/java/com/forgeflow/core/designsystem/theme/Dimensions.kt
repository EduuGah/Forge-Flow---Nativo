package com.forgeflow.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ForgeFlowSpacing(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 12.dp,
    val large: Dp = 20.dp,
    val extraLarge: Dp = 28.dp,
    val screenHorizontal: Dp = 20.dp,
    val card: Dp = 16.dp,
    val section: Dp = 24.dp,
)

internal val LocalForgeFlowSpacing = staticCompositionLocalOf { ForgeFlowSpacing() }

internal fun forgeFlowSpacing(compact: Boolean): ForgeFlowSpacing =
    if (compact) {
        ForgeFlowSpacing(
            small = 6.dp,
            medium = 8.dp,
            large = 16.dp,
            extraLarge = 22.dp,
            screenHorizontal = 16.dp,
            card = 12.dp,
            section = 18.dp,
        )
    } else {
        ForgeFlowSpacing()
    }
