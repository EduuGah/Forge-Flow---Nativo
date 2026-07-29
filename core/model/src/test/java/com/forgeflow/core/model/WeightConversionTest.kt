package com.forgeflow.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WeightConversionTest {
    @Test
    fun kilograms_areConvertedToPoundsWithoutChangingStoredGrams() {
        val weight = Weight.from(value = 100.0, unit = WeightUnit.KILOGRAM)

        assertEquals(100_000L, weight.grams)
        assertEquals(220.46, weight.valueIn(WeightUnit.POUND), 0.01)
    }

    @Test
    fun pounds_areStoredInGramsAndCanBeReadAsKilograms() {
        val weight = Weight.from(value = 220.462, unit = WeightUnit.POUND)

        assertEquals(100.0, weight.valueIn(WeightUnit.KILOGRAM), 0.01)
    }
}
