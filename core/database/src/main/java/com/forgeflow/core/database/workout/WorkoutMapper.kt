package com.forgeflow.core.database.workout

import com.forgeflow.core.database.exercise.asExternalModel
import com.forgeflow.core.model.ExerciseId
import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.core.model.Repetitions
import com.forgeflow.core.model.RoutineId
import com.forgeflow.core.model.Rpe
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

fun WorkoutRecord.asExternalModel(): WorkoutDetails = WorkoutDetails(
    session = session.asExternalModel(),
    exercises = exercises
        .sortedBy { it.item.position }
        .map { record ->
            WorkoutExerciseDetails(
                sessionExercise = WorkoutSessionExercise(
                    id = SessionExerciseId(record.item.id),
                    sessionId = WorkoutSessionId(record.item.sessionId),
                    exerciseId = record.item.exerciseId?.let(::ExerciseId),
                    exerciseNameSnapshot = record.item.exerciseNameSnapshot,
                    muscleGroupSnapshot = MuscleGroup.valueOf(record.item.muscleGroupSnapshot),
                    position = record.item.position,
                    notes = record.item.notes,
                ),
                exercise = record.exercise?.asExternalModel(),
                sets = record.sets.sortedBy(WorkoutSetEntity::position).map { it.asExternalModel() },
            )
        },
)

private fun WorkoutSessionEntity.asExternalModel(): WorkoutSession = WorkoutSession(
    id = WorkoutSessionId(id),
    routineId = routineId?.let(::RoutineId),
    name = name,
    startedAt = Instant.ofEpochMilli(startedAtEpochMillis),
    finishedAt = finishedAtEpochMillis?.let(Instant::ofEpochMilli),
    status = WorkoutSessionStatus.valueOf(status),
    notes = notes,
    createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
)

private fun WorkoutSetEntity.asExternalModel(): WorkoutSet = WorkoutSet(
    id = WorkoutSetId(id),
    sessionExerciseId = SessionExerciseId(sessionExerciseId),
    position = position,
    setType = WorkoutSetType.valueOf(setType),
    weight = Weight.fromGrams(weightGrams),
    repetitions = Repetitions(repetitions),
    rpe = rpe?.let(::Rpe),
    isCompleted = isCompleted,
    completedAt = completedAtEpochMillis?.let(Instant::ofEpochMilli),
    createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
)
