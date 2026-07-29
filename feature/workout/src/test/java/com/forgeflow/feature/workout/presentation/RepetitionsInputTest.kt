package com.forgeflow.feature.workout.presentation

import com.forgeflow.core.model.Repetitions
import org.junit.Assert.assertEquals
import org.junit.Test

class RepetitionsInputTest {
    @Test
    fun zeroRepetitionsUseAnEmptyInputValue() {
        assertEquals("", Repetitions(0).asInputValue())
    }

    @Test
    fun positiveRepetitionsRemainVisible() {
        assertEquals("12", Repetitions(12).asInputValue())
    }
}
