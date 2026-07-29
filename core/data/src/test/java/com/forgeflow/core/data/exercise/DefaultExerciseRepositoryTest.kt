package com.forgeflow.core.data.exercise

import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.common.time.AppClock
import com.forgeflow.core.database.exercise.ExerciseEntity
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultExerciseRepositoryTest {
    @Test
    fun observeExercises_mapsDatabaseEntitiesToDomainModels() = runTest {
        val dataSource = FakeExerciseLocalDataSource(
            entities = listOf(
                exerciseEntity(
                    id = "04f35c8f-e525-469e-8238-25e31087e07a",
                    name = "Supino com barra",
                ),
            ),
        )
        val repository = DefaultExerciseRepository(dataSource, FixedClock)

        val result = repository.observeExercises().first()

        assertTrue(result is DataResult.Success)
        val exercise = (result as DataResult.Success).value.single()
        assertEquals("Supino com barra", exercise.name)
        assertEquals("04f35c8f-e525-469e-8238-25e31087e07a", exercise.id.value)
    }

    private class FakeExerciseLocalDataSource(
        private val entities: List<ExerciseEntity>,
    ) : ExerciseLocalDataSource {
        override fun observeExercises(): Flow<List<ExerciseEntity>> = flowOf(entities)

        override suspend fun insertExercises(exercises: List<ExerciseEntity>): Int =
            exercises.size
    }

    private object FixedClock : AppClock {
        override fun now(): Instant = Instant.parse("2026-01-01T00:00:00Z")
    }

    private fun exerciseEntity(id: String, name: String) = ExerciseEntity(
        id = id,
        name = name,
        primaryMuscleGroup = "CHEST",
        secondaryMuscleGroups = "TRICEPS",
        equipment = "BARBELL",
        instructions = "",
        isCustom = false,
        createdAtEpochMillis = 0,
        updatedAtEpochMillis = 0,
    )
}
