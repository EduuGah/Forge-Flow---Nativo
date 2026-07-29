package com.forgeflow.core.database.exercise

import com.forgeflow.core.model.Equipment
import com.forgeflow.core.model.Exercise
import com.forgeflow.core.model.ExerciseId
import com.forgeflow.core.model.MuscleGroup
import java.time.Instant

fun ExerciseEntity.asExternalModel(): Exercise = Exercise(
    id = ExerciseId(id),
    name = name,
    primaryMuscleGroup = MuscleGroup.valueOf(primaryMuscleGroup),
    secondaryMuscleGroups = secondaryMuscleGroups
        .split(MUSCLE_GROUP_SEPARATOR)
        .filter(String::isNotBlank)
        .mapTo(linkedSetOf(), MuscleGroup::valueOf),
    equipment = Equipment.valueOf(equipment),
    instructions = instructions,
    isCustom = isCustom,
    createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
)

fun Exercise.asEntity(): ExerciseEntity = ExerciseEntity(
    id = id.value,
    name = name,
    primaryMuscleGroup = primaryMuscleGroup.name,
    secondaryMuscleGroups = secondaryMuscleGroups.joinToString(MUSCLE_GROUP_SEPARATOR) { it.name },
    equipment = equipment.name,
    instructions = instructions,
    isCustom = isCustom,
    createdAtEpochMillis = createdAt.toEpochMilli(),
    updatedAtEpochMillis = updatedAt.toEpochMilli(),
)

private const val MUSCLE_GROUP_SEPARATOR = ","
