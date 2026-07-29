package com.forgeflow.core.database.workout

import androidx.room.Embedded
import androidx.room.Relation
import com.forgeflow.core.database.exercise.ExerciseEntity

data class WorkoutExerciseRecord(
    @Embedded
    val item: WorkoutSessionExerciseEntity,
    @Relation(
        parentColumn = "exercise_id",
        entityColumn = "id",
    )
    val exercise: ExerciseEntity?,
    @Relation(
        parentColumn = "id",
        entityColumn = "session_exercise_id",
    )
    val sets: List<WorkoutSetEntity>,
)

data class WorkoutRecord(
    @Embedded
    val session: WorkoutSessionEntity,
    @Relation(
        entity = WorkoutSessionExerciseEntity::class,
        parentColumn = "id",
        entityColumn = "session_id",
    )
    val exercises: List<WorkoutExerciseRecord>,
)
