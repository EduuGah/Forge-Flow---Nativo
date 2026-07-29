package com.forgeflow.core.database.routine

import androidx.room.Embedded
import androidx.room.Relation
import com.forgeflow.core.database.exercise.ExerciseEntity

data class RoutineExerciseRecord(
    @Embedded
    val item: RoutineExerciseEntity,
    @Relation(
        parentColumn = "exercise_id",
        entityColumn = "id",
    )
    val exercise: ExerciseEntity,
)

data class RoutineRecord(
    @Embedded
    val routine: RoutineEntity,
    @Relation(
        entity = RoutineExerciseEntity::class,
        parentColumn = "id",
        entityColumn = "routine_id",
    )
    val exercises: List<RoutineExerciseRecord>,
)
