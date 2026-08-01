package com.forgeflow.feature.home.presentation

import com.forgeflow.core.model.GoalCadence
import com.forgeflow.core.model.PerformanceGoal
import com.forgeflow.core.model.PerformanceGoalType
import com.forgeflow.core.model.WorkoutDetails
import com.forgeflow.core.model.WorkoutSetType
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

internal data class GoalProgress(
    val currentValue: Long,
    val fraction: Float,
    val isCompleted: Boolean,
    val isOverdue: Boolean,
)

internal fun calculateGoalProgress(
    goal: PerformanceGoal,
    history: List<WorkoutDetails>,
    today: LocalDate = LocalDate.now(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): GoalProgress {
    val deadline = goal.deadlineEpochDay?.let(LocalDate::ofEpochDay)
    val evaluationDate = deadline?.takeIf { it.isBefore(today) } ?: today
    val eligibleHistory = history.filter { workout ->
        !workout.session.startedAt.atZone(zoneId).toLocalDate().isAfter(evaluationDate)
    }
    val current = when (goal.type) {
        PerformanceGoalType.EXERCISE_WEIGHT -> eligibleHistory
            .asSequence()
            .flatMap(WorkoutDetails::exercises)
            .filter { it.sessionExercise.exerciseId == goal.exerciseId }
            .flatMap { it.sets }
            .filter { set ->
                set.isCompleted &&
                    set.setType == WorkoutSetType.NORMAL &&
                    set.repetitions.count >= goal.minimumRepetitions
            }
            .maxOfOrNull { it.weight.grams }
            ?: 0L

        else -> {
            val periodStart = goal.periodStart(evaluationDate, zoneId)
            val workouts = eligibleHistory.filter { workout ->
                val workoutDate = workout.session.startedAt.atZone(zoneId).toLocalDate()
                !workoutDate.isBefore(periodStart)
            }
            when (goal.type) {
                PerformanceGoalType.WORKOUT_COUNT -> workouts.size.toLong()
                PerformanceGoalType.TOTAL_VOLUME -> workouts.sumOf(WorkoutDetails::totalVolumeGrams)
                PerformanceGoalType.TRAINING_DURATION -> workouts.sumOf { workout ->
                    workout.session.finishedAt?.let { finishedAt ->
                        Duration.between(workout.session.startedAt, finishedAt)
                            .toMinutes()
                            .coerceAtLeast(0L)
                    } ?: 0L
                }
                PerformanceGoalType.EXERCISE_WEIGHT -> 0L
            }
        }
    }
    val completed = current >= goal.targetValue
    return GoalProgress(
        currentValue = current,
        fraction = (current.toDouble() / goal.targetValue)
            .toFloat()
            .coerceIn(0f, 1f),
        isCompleted = completed,
        isOverdue = !completed && goal.deadlineEpochDay?.let { today.toEpochDay() > it } == true,
    )
}

private fun PerformanceGoal.periodStart(today: LocalDate, zoneId: ZoneId): LocalDate = when (cadence) {
    GoalCadence.ONCE -> createdAt.atZone(zoneId).toLocalDate()
    GoalCadence.DAILY -> today
    GoalCadence.WEEKLY -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    GoalCadence.MONTHLY -> today.withDayOfMonth(1)
}
