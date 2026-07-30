package com.forgeflow.core.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutProgressTest {
    @Test
    fun streaksAndCurrentWeekUseUniqueWorkoutDays() {
        val today = LocalDate.of(2026, 7, 29)
        val workoutDates = listOf(
            LocalDate.of(2026, 7, 20),
            LocalDate.of(2026, 7, 21),
            LocalDate.of(2026, 7, 22),
            LocalDate.of(2026, 7, 27),
            LocalDate.of(2026, 7, 28),
            LocalDate.of(2026, 7, 28),
            LocalDate.of(2026, 7, 29),
        )

        val result = calculateWorkoutStreakStats(workoutDates, today)

        assertEquals(3, result.current)
        assertEquals(3, result.best)
        assertEquals(3, result.currentWeekCount)
    }

    @Test
    fun currentStreakCanContinueFromYesterday() {
        val today = LocalDate.of(2026, 7, 29)

        val result = calculateWorkoutStreakStats(
            workoutDates = listOf(today.minusDays(2), today.minusDays(1)),
            today = today,
        )

        assertEquals(2, result.current)
    }

    @Test
    fun nextScheduleSkipsACompletedWorkoutToday() {
        val today = LocalDate.of(2026, 7, 29)

        val result = nextScheduledWorkoutDate(
            today = today,
            trainingDays = setOf(TrainingDay.WEDNESDAY, TrainingDay.FRIDAY),
            completedDates = setOf(today),
        )

        assertEquals(LocalDate.of(2026, 7, 31), result)
    }
}
