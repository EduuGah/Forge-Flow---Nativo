package com.forgeflow.core.data.exercise

import com.forgeflow.core.model.Equipment
import com.forgeflow.core.model.Exercise
import com.forgeflow.core.model.ExerciseId
import com.forgeflow.core.model.ExerciseMedia
import com.forgeflow.core.model.ExerciseMediaType
import com.forgeflow.core.model.ExerciseMediaUris
import com.forgeflow.core.model.MuscleGroup
import java.time.Instant

internal object ExerciseSeedData {
    fun create(timestamp: Instant): List<Exercise> = definitions.map { definition ->
        Exercise(
            id = ExerciseId(definition.id),
            name = definition.name,
            primaryMuscleGroup = definition.primaryMuscleGroup,
            secondaryMuscleGroups = definition.secondaryMuscleGroups,
            equipment = Equipment.BARBELL,
            instructions = definition.instructions,
            media = ExerciseMedia(
                uri = definition.mediaUri,
                type = ExerciseMediaType.IMAGE,
            ),
            isCustom = false,
            createdAt = timestamp,
            updatedAt = timestamp,
        )
    }

    private val definitions = listOf(
        Definition(
            id = "04f35c8f-e525-469e-8238-25e31087e07a",
            name = "Supino com barra",
            primaryMuscleGroup = MuscleGroup.CHEST,
            secondaryMuscleGroups = setOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS),
            instructions = "Mantenha os pés firmes e controle a barra durante todo o movimento.",
            mediaUri = ExerciseMediaUris.BENCH_PRESS,
        ),
        Definition(
            id = "42e026cb-c0a2-442c-8d46-a5c2ae5e4293",
            name = "Agachamento livre",
            primaryMuscleGroup = MuscleGroup.QUADRICEPS,
            secondaryMuscleGroups = setOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS),
            instructions = "Mantenha o tronco estável e os joelhos alinhados com os pés.",
            mediaUri = ExerciseMediaUris.BACK_SQUAT,
        ),
        Definition(
            id = "234590ba-5ae9-407f-afaa-3bfaf62a1c42",
            name = "Levantamento terra",
            primaryMuscleGroup = MuscleGroup.FULL_BODY,
            secondaryMuscleGroups = setOf(MuscleGroup.BACK, MuscleGroup.HAMSTRINGS),
            instructions = "Inicie com a barra próxima às pernas e preserve a coluna neutra.",
            mediaUri = ExerciseMediaUris.DEADLIFT,
        ),
        Definition(
            id = "eae8a5fc-acb1-465a-8162-246bcbafd7b1",
            name = "Remada com barra",
            primaryMuscleGroup = MuscleGroup.BACK,
            secondaryMuscleGroups = setOf(MuscleGroup.BICEPS),
            instructions = "Estabilize o tronco e leve a barra em direção ao abdômen.",
            mediaUri = ExerciseMediaUris.BARBELL_ROW,
        ),
        Definition(
            id = "ca3218ec-516b-455b-af27-51dc846065e0",
            name = "Desenvolvimento de ombros",
            primaryMuscleGroup = MuscleGroup.SHOULDERS,
            secondaryMuscleGroups = setOf(MuscleGroup.TRICEPS),
            instructions = "Pressione a barra acima da cabeça sem perder a estabilidade do tronco.",
            mediaUri = ExerciseMediaUris.OVERHEAD_PRESS,
        ),
    )

    private data class Definition(
        val id: String,
        val name: String,
        val primaryMuscleGroup: MuscleGroup,
        val secondaryMuscleGroups: Set<MuscleGroup>,
        val instructions: String,
        val mediaUri: String,
    )
}
