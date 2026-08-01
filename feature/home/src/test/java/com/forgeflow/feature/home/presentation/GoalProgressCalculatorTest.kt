package com.forgeflow.feature.home.presentation

import com.forgeflow.core.model.ExerciseId
import com.forgeflow.core.model.GoalCadence
import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.core.model.PerformanceGoal
import com.forgeflow.core.model.PerformanceGoalType
import com.forgeflow.core.model.Repetitions
import com.forgeflow.core.model.SessionExerciseId
import com.forgeflow.core.model.Weight
import com.forgeflow.core.model.WorkoutDetails
import com.forgeflow.core.model.WorkoutExerciseDetails
import com.forgeflow.core.model.WorkoutSession
import com.forgeflow.core.model.WorkoutSessionExercise
import com.forgeflow.core.model.WorkoutSessionId
import com.forgeflow.core.model.WorkoutSessionStatus
import com.forgeflow.core.model.WorkoutSet
import com.forgeflow.core.model.WorkoutSetId
import com.forgeflow.core.model.WorkoutSetType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalProgressCalculatorTest {
    @Test
    fun weeklyWorkoutGoalCountsOnlyCurrentWeek() {
        val goal = PerformanceGoal(
            id = "weekly",
            type = PerformanceGoalType.WORKOUT_COUNT,
            cadence = GoalCadence.WEEKLY,
            title = "Treinar três vezes",
            targetValue = 3,
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        )
        val result = calculateGoalProgress(
            goal = goal,
            history = listOf(
                workout("2026-08-03T18:00:00Z"),
                workout("2026-08-02T18:00:00Z"),
            ),
            today = LocalDate.of(2026, 8, 5),
            zoneId = ZoneOffset.UTC,
        )

        assertEquals(1L, result.currentValue)
        assertEquals(1f / 3f, result.fraction, 0.001f)
        assertFalse(result.isCompleted)
    }

    @Test
    fun exerciseGoalUsesNormalSetMeetingMinimumRepetitions() {
        val exerciseId = ExerciseId.create()
        val goal = PerformanceGoal(
            id = "bench",
            type = PerformanceGoalType.EXERCISE_WEIGHT,
            cadence = GoalCadence.ONCE,
            title = "Supino com 100 kg",
            targetValue = 100_000,
            exerciseId = exerciseId,
            exerciseNameSnapshot = "Supino reto",
            minimumRepetitions = 5,
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        )
        val result = calculateGoalProgress(
            goal = goal,
            history = listOf(
                workout(
                    startedAt = "2026-08-03T18:00:00Z",
                    exerciseId = exerciseId,
                    sets = listOf(
                        set(120_000, 8, WorkoutSetType.WARM_UP),
                        set(110_000, 2, WorkoutSetType.NORMAL),
                        set(100_000, 5, WorkoutSetType.NORMAL),
                    ),
                ),
            ),
            today = LocalDate.of(2026, 8, 5),
            zoneId = ZoneOffset.UTC,
        )

        assertEquals(100_000L, result.currentValue)
        assertTrue(result.isCompleted)
        assertEquals(1f, result.fraction, 0.001f)
    }

    @Test
    fun unfinishedGoalBecomesOverdueAfterDeadline() {
        val goal = PerformanceGoal(
            id = "deadline",
            type = PerformanceGoalType.TRAINING_DURATION,
            cadence = GoalCadence.MONTHLY,
            title = "Treinar 300 minutos",
            targetValue = 300,
            deadlineEpochDay = LocalDate.of(2026, 8, 4).toEpochDay(),
            createdAt = Instant.parse("2026-08-01T00:00:00Z"),
        )
        val result = calculateGoalProgress(
            goal = goal,
            history = emptyList(),
            today = LocalDate.of(2026, 8, 5),
            zoneId = ZoneOffset.UTC,
        )

        assertTrue(result.isOverdue)
        assertFalse(result.isCompleted)
    }

    @Test
    fun workoutAfterDeadlineDoesNotCompleteGoalRetroactively() {
        val deadline = LocalDate.of(2026, 8, 4)
        val goal = PerformanceGoal(
            id = "deadline-count",
            type = PerformanceGoalType.WORKOUT_COUNT,
            cadence = GoalCadence.ONCE,
            title = "Treinar duas vezes",
            targetValue = 2,
            deadlineEpochDay = deadline.toEpochDay(),
            createdAt = Instant.parse("2026-08-01T00:00:00Z"),
        )

        val result = calculateGoalProgress(
            goal = goal,
            history = listOf(
                workout("2026-08-03T18:00:00Z"),
                workout("2026-08-05T18:00:00Z"),
            ),
            today = LocalDate.of(2026, 8, 6),
            zoneId = ZoneOffset.UTC,
        )

        assertEquals(1L, result.currentValue)
        assertFalse(result.isCompleted)
        assertTrue(result.isOverdue)
    }

    @Test
    fun recurringGoalCompletedByDeadlineRemainsCompletedAfterDeadline() {
        val deadline = LocalDate.of(2026, 8, 9)
        val goal = PerformanceGoal(
            id = "weekly-deadline",
            type = PerformanceGoalType.WORKOUT_COUNT,
            cadence = GoalCadence.WEEKLY,
            title = "Treinar duas vezes",
            targetValue = 2,
            deadlineEpochDay = deadline.toEpochDay(),
            createdAt = Instant.parse("2026-08-01T00:00:00Z"),
        )

        val result = calculateGoalProgress(
            goal = goal,
            history = listOf(
                workout("2026-08-03T18:00:00Z"),
                workout("2026-08-08T18:00:00Z"),
            ),
            today = LocalDate.of(2026, 8, 17),
            zoneId = ZoneOffset.UTC,
        )

        assertEquals(2L, result.currentValue)
        assertTrue(result.isCompleted)
        assertFalse(result.isOverdue)
    }
}

private fun workout(
    startedAt: String,
    exerciseId: ExerciseId? = null,
    sets: List<WorkoutSet> = emptyList(),
): WorkoutDetails {
    val sessionId = WorkoutSessionId.create()
    val started = Instant.parse(startedAt)
    val sessionExerciseId = SessionExerciseId.create()
    return WorkoutDetails(
        session = WorkoutSession(
            id = sessionId,
            routineId = null,
            name = "Treino",
            startedAt = started,
            finishedAt = started.plusSeconds(3_600),
            status = WorkoutSessionStatus.COMPLETED,
            notes = "",
            createdAt = started,
            updatedAt = started,
        ),
        exercises = exerciseId?.let {
            listOf(
                WorkoutExerciseDetails(
                    sessionExercise = WorkoutSessionExercise(
                        id = sessionExerciseId,
                        sessionId = sessionId,
                        exerciseId = exerciseId,
                        exerciseNameSnapshot = "Supino reto",
                        muscleGroupSnapshot = MuscleGroup.CHEST,
                        position = 0,
                        notes = "",
                    ),
                    exercise = null,
                    sets = sets.map { set ->
                        set.copy(sessionExerciseId = sessionExerciseId)
                    },
                ),
            )
        }.orEmpty(),
    )
}

private fun set(weightGrams: Long, repetitions: Int, type: WorkoutSetType) = WorkoutSet(
    id = WorkoutSetId.create(),
    sessionExerciseId = SessionExerciseId.create(),
    position = 0,
    setType = type,
    weight = Weight.fromGrams(weightGrams),
    repetitions = Repetitions(repetitions),
    rpe = null,
    isCompleted = true,
    completedAt = Instant.parse("2026-08-03T18:10:00Z"),
    createdAt = Instant.parse("2026-08-03T18:00:00Z"),
    updatedAt = Instant.parse("2026-08-03T18:10:00Z"),
)
