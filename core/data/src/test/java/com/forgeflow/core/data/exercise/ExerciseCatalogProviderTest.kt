package com.forgeflow.core.data.exercise

import com.forgeflow.core.model.MuscleGroup
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ExerciseCatalogProviderTest {
    @Test
    fun decodeExerciseCatalog_mapsValidNativeExercise() {
        val exercises = decodeExerciseCatalog(
            content = catalogJson(id = VALID_ID),
            timestamp = TIMESTAMP,
        )

        assertEquals(1, exercises.size)
        assertEquals(VALID_ID, exercises.single().id.value)
        assertEquals(MuscleGroup.CHEST, exercises.single().primaryMuscleGroup)
        assertEquals(TIMESTAMP, exercises.single().createdAt)
    }

    @Test
    fun decodeExerciseCatalog_rejectsNonUuidIdentifier() {
        assertThrows(IllegalArgumentException::class.java) {
            decodeExerciseCatalog(
                content = catalogJson(id = "forgeflow-catalog-bench-press"),
                timestamp = TIMESTAMP,
            )
        }
    }

    private fun catalogJson(id: String) = """
        [
          {
            "id": "$id",
            "name": "Supino inclinado",
            "primaryMuscleGroup": "CHEST",
            "secondaryMuscleGroups": ["TRICEPS"],
            "equipment": "BARBELL",
            "instructions": "Controle a barra.",
            "mediaUri": null
          }
        ]
    """.trimIndent()

    private companion object {
        const val VALID_ID = "7dc4a6b8-5274-5cf1-a74d-e5095fa95c91"
        val TIMESTAMP: Instant = Instant.parse("2026-07-29T12:00:00Z")
    }
}
