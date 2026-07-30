package com.forgeflow.feature.home.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EvolutionAnalyticsTest {
    @Test
    fun percentageChangeComparesEquivalentPeriods() {
        assertEquals(50, percentageChange(current = 6.0, previous = 4.0))
        assertEquals(-25, percentageChange(current = 3.0, previous = 4.0))
        assertEquals(0, percentageChange(current = 4.0, previous = 4.0))
    }

    @Test
    fun percentageChangeHasNoBaselineWhenPreviousPeriodIsEmpty() {
        assertNull(percentageChange(current = 4.0, previous = 0.0))
    }

    @Test
    fun chartTapSelectsNearestVisiblePoint() {
        assertEquals(
            0,
            nearestEvolutionChartPointIndex(
                tapX = 0f,
                chartWidth = 300f,
                pointCount = 6,
            ),
        )
        assertEquals(
            3,
            nearestEvolutionChartPointIndex(
                tapX = 165f,
                chartWidth = 300f,
                pointCount = 6,
            ),
        )
        assertEquals(
            5,
            nearestEvolutionChartPointIndex(
                tapX = 300f,
                chartWidth = 300f,
                pointCount = 6,
            ),
        )
    }
}
