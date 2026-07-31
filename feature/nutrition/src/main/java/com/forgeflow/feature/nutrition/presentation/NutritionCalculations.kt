package com.forgeflow.feature.nutrition.presentation

import kotlin.math.roundToInt

internal fun caloriesFromMacros(
    proteinGrams: Double,
    carbohydrateGrams: Double,
    fatGrams: Double,
): Int = (
    proteinGrams.coerceAtLeast(0.0) * 4 +
        carbohydrateGrams.coerceAtLeast(0.0) * 4 +
        fatGrams.coerceAtLeast(0.0) * 9
    ).roundToInt()
