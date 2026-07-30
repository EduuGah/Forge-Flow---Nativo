package com.forgeflow.core.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class WorkoutStreakStats(
    val current: Int,
    val best: Int,
    val currentWeekCount: Int,
)

fun calculateWorkoutStreakStats(
    workoutDates: Collection<LocalDate>,
    today: LocalDate = LocalDate.now(),
): WorkoutStreakStats {
    val uniqueDates = workoutDates.toSortedSet()
    if (uniqueDates.isEmpty()) {
        return WorkoutStreakStats(current = 0, best = 0, currentWeekCount = 0)
    }

    val streakStart = if (today in uniqueDates) today else today.minusDays(1)
    var cursor = streakStart
    var currentStreak = 0
    while (cursor in uniqueDates) {
        currentStreak += 1
        cursor = cursor.minusDays(1)
    }

    var bestStreak = 1
    var runningStreak = 1
    uniqueDates.zipWithNext().forEach { (previous, next) ->
        if (previous.plusDays(1) == next) {
            runningStreak += 1
            bestStreak = maxOf(bestStreak, runningStreak)
        } else {
            runningStreak = 1
        }
    }

    val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekEnd = weekStart.plusDays(6)
    val currentWeekCount = uniqueDates.count { date ->
        !date.isBefore(weekStart) && !date.isAfter(weekEnd)
    }

    return WorkoutStreakStats(
        current = currentStreak,
        best = bestStreak,
        currentWeekCount = currentWeekCount,
    )
}

fun nextScheduledWorkoutDate(
    today: LocalDate,
    trainingDays: Set<TrainingDay>,
    completedDates: Set<LocalDate> = emptySet(),
): LocalDate? {
    if (trainingDays.isEmpty()) return null
    return (0L..13L)
        .asSequence()
        .map(today::plusDays)
        .firstOrNull { date ->
            date.dayOfWeek.toTrainingDay() in trainingDays && date !in completedDates
        }
}

fun DayOfWeek.toTrainingDay(): TrainingDay = when (this) {
    DayOfWeek.MONDAY -> TrainingDay.MONDAY
    DayOfWeek.TUESDAY -> TrainingDay.TUESDAY
    DayOfWeek.WEDNESDAY -> TrainingDay.WEDNESDAY
    DayOfWeek.THURSDAY -> TrainingDay.THURSDAY
    DayOfWeek.FRIDAY -> TrainingDay.FRIDAY
    DayOfWeek.SATURDAY -> TrainingDay.SATURDAY
    DayOfWeek.SUNDAY -> TrainingDay.SUNDAY
}
