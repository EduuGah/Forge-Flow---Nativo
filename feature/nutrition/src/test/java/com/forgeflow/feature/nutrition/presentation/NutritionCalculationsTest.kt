package com.forgeflow.feature.nutrition.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class NutritionCalculationsTest {
    @Test
    fun `calculates calories from all macros`() {
        assertEquals(
            565,
            caloriesFromMacros(
                proteinGrams = 40.0,
                carbohydrateGrams = 70.0,
                fatGrams = 13.9,
            ),
        )
    }

    @Test
    fun `negative values do not reduce calories`() {
        assertEquals(
            400,
            caloriesFromMacros(
                proteinGrams = 100.0,
                carbohydrateGrams = -10.0,
                fatGrams = -3.0,
            ),
        )
    }
}
