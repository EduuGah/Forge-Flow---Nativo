package com.forgeflow.core.model

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalRecordDetectionTest {
    @Test
    fun firstCompletedWorkingSetEstablishesAllRecordBaselines() {
        val records = workoutSet(weightKg = 60.0, repetitions = 8)
            .personalRecordsAgainst(emptyList())

        assertEquals(PersonalRecordType.entries.toSet(), records)
    }

    @Test
    fun warmUpAndIncompleteSetsDoNotCreateRecords() {
        val warmUp = workoutSet(
            weightKg = 40.0,
            repetitions = 12,
            type = WorkoutSetType.WARM_UP,
        )
        val incomplete = workoutSet(
            weightKg = 80.0,
            repetitions = 8,
            completed = false,
        )

        assertTrue(warmUp.personalRecordsAgainst(emptyList()).isEmpty())
        assertTrue(incomplete.personalRecordsAgainst(emptyList()).isEmpty())
    }

    @Test
    fun moreRepetitionsAtTheSameWeightCreatesProgressRecordsWithoutWeightRecord() {
        val previous = workoutSet(weightKg = 60.0, repetitions = 6)
        val current = workoutSet(weightKg = 60.0, repetitions = 8)

        val records = current.personalRecordsAgainst(listOf(previous))

        assertTrue(PersonalRecordType.WEIGHT !in records)
        assertTrue(PersonalRecordType.REPETITIONS_AT_WEIGHT in records)
        assertTrue(PersonalRecordType.ESTIMATED_ONE_REP_MAX in records)
        assertTrue(PersonalRecordType.SET_VOLUME in records)
    }

    @Test
    fun lowerPerformanceDoesNotCreateARecord() {
        val previous = workoutSet(weightKg = 60.0, repetitions = 8)
        val current = workoutSet(weightKg = 60.0, repetitions = 6)

        assertTrue(current.personalRecordsAgainst(listOf(previous)).isEmpty())
    }

    private fun workoutSet(
        weightKg: Double,
        repetitions: Int,
        type: WorkoutSetType = WorkoutSetType.NORMAL,
        completed: Boolean = true,
    ) = WorkoutSet(
        id = WorkoutSetId("85bce28d-5d5b-4cdd-b40f-65802848f095"),
        sessionExerciseId = SessionExerciseId("baef4c18-3261-4530-a93a-a79aad88e069"),
        position = 0,
        setType = type,
        weight = Weight.from(weightKg, WeightUnit.KILOGRAM),
        repetitions = Repetitions(repetitions),
        rpe = null,
        isCompleted = completed,
        completedAt = if (completed) Instant.EPOCH else null,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
    )
}
