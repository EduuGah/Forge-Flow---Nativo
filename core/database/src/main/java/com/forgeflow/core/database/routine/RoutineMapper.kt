package com.forgeflow.core.database.routine

import com.forgeflow.core.database.exercise.asExternalModel
import com.forgeflow.core.model.RepetitionRange
import com.forgeflow.core.model.RoutineFolder
import com.forgeflow.core.model.RoutineFolderId
import com.forgeflow.core.model.RoutineDetails
import com.forgeflow.core.model.RoutineExercise
import com.forgeflow.core.model.RoutineExerciseDetails
import com.forgeflow.core.model.RoutineExerciseId
import com.forgeflow.core.model.RoutineId
import com.forgeflow.core.model.WorkoutRoutine
import java.time.Instant

fun RoutineRecord.asExternalModel(): RoutineDetails = RoutineDetails(
    routine = WorkoutRoutine(
        id = RoutineId(routine.id),
        folderId = routine.folderId?.let(::RoutineFolderId),
        name = routine.name,
        description = routine.description,
        createdAt = Instant.ofEpochMilli(routine.createdAtEpochMillis),
        updatedAt = Instant.ofEpochMilli(routine.updatedAtEpochMillis),
        archivedAt = routine.archivedAtEpochMillis?.let(Instant::ofEpochMilli),
    ),
    exercises = exercises
        .sortedBy { it.item.position }
        .map { record ->
            val minimum = record.item.plannedRepetitionsMinimum
            val maximum = record.item.plannedRepetitionsMaximum
            RoutineExerciseDetails(
                routineExercise = RoutineExercise(
                    id = RoutineExerciseId(record.item.id),
                    routineId = RoutineId(record.item.routineId),
                    exerciseId = com.forgeflow.core.model.ExerciseId(record.item.exerciseId),
                    position = record.item.position,
                    notes = record.item.notes,
                    defaultRestSeconds = record.item.defaultRestSeconds,
                    plannedRepetitions = if (minimum != null && maximum != null) {
                        RepetitionRange(minimum, maximum)
                    } else {
                        null
                    },
                    plannedSets = record.item.plannedSets,
                ),
                exercise = record.exercise.asExternalModel(),
            )
        },
)

fun RoutineFolderEntity.asExternalModel(): RoutineFolder = RoutineFolder(
    id = RoutineFolderId(id),
    name = name,
    position = position,
    createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
)
