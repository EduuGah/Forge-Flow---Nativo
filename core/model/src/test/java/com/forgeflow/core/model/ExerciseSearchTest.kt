package com.forgeflow.core.model

import java.time.Instant
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseSearchTest {
    @Test
    fun normalizedSearch_ignoresAccentsAndTrailingWhitespace() {
        assertTrue("Cadeira extensora".normalizedSearchText().contains("Extensora ".normalizedSearchText()))
        assertTrue("Elevação lateral".normalizedSearchText().contains("elevacao".normalizedSearchText()))
    }

    @Test
    fun exerciseSearch_matchesCommonPortugueseAndEnglishAliases() {
        assertTrue(exercise("Cadeira extensora").matchesSearch("leg extension"))
        assertTrue(
            exercise("Rosca alternada com halteres")
                .matchesSearch("rosca direta com halter alternado"),
        )
        assertTrue(exercise("Supino reto com barra").matchesSearch("bench press"))
    }

    @Test
    fun exerciseSearch_matchesCommonGymNames() {
        assertTrue(exercise("Remada unilateral com halter").matchesSearch("serrote"))
        assertTrue(exercise("Elevação pélvica").matchesSearch("hip thrust"))
        assertTrue(exercise("Rosca Scott").matchesSearch("preacher curl"))
        assertTrue(exercise("Wood chop no cabo").matchesSearch("lenhador"))
    }

    private fun exercise(name: String) = Exercise(
        id = ExerciseId("71c3d5c8-8bde-5e8a-a6b1-51ae0a10d139"),
        name = name,
        primaryMuscleGroup = MuscleGroup.BICEPS,
        secondaryMuscleGroups = emptySet(),
        equipment = Equipment.DUMBBELL,
        instructions = "",
        isCustom = false,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
    )
}
