package com.forgeflow.feature.exercises.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExerciseChartSelectionTest {
    @Test
    fun tapSelectsTheNearestPointAcrossTheWholeChart() {
        assertEquals(0, nearestChartPointIndex(tapX = 0f, chartWidth = 300f, pointCount = 4))
        assertEquals(1, nearestChartPointIndex(tapX = 110f, chartWidth = 300f, pointCount = 4))
        assertEquals(3, nearestChartPointIndex(tapX = 300f, chartWidth = 300f, pointCount = 4))
    }

    @Test
    fun invalidChartHasNoSelection() {
        assertNull(nearestChartPointIndex(tapX = 20f, chartWidth = 0f, pointCount = 4))
        assertNull(nearestChartPointIndex(tapX = 20f, chartWidth = 100f, pointCount = 0))
    }
}
