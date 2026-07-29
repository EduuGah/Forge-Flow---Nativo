package com.forgeflow.core.model

import java.time.Instant

data class WorkoutRoutine(
    val id: RoutineId,
    val folderId: RoutineFolderId?,
    val name: String,
    val description: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val archivedAt: Instant?,
)

data class RoutineFolder(
    val id: RoutineFolderId,
    val name: String,
    val position: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class RoutineExercise(
    val id: RoutineExerciseId,
    val routineId: RoutineId,
    val exerciseId: ExerciseId,
    val position: Int,
    val notes: String,
    val defaultRestSeconds: Int,
    val plannedRepetitions: RepetitionRange?,
    val plannedSets: Int,
)

data class RoutineExerciseDetails(
    val routineExercise: RoutineExercise,
    val exercise: Exercise,
)

data class RoutineDetails(
    val routine: WorkoutRoutine,
    val exercises: List<RoutineExerciseDetails>,
)

data class RoutineExerciseDraft(
    val exerciseId: ExerciseId,
    val plannedSets: Int = 3,
    val plannedRepetitions: RepetitionRange = RepetitionRange(8, 12),
    val restSeconds: Int = 90,
    val notes: String = "",
)

data class RoutineDraft(
    val id: RoutineId? = null,
    val folderId: RoutineFolderId? = null,
    val name: String,
    val description: String = "",
    val exercises: List<RoutineExerciseDraft>,
)

enum class PersonalRecordType {
    WEIGHT,
    SET_VOLUME,
}

fun WorkoutSet.estimatedOneRepMaxGrams(): Double {
    if (weight.grams == 0L || repetitions.count == 0) return 0.0
    return weight.grams * (1.0 + repetitions.count / 30.0)
}

fun WorkoutSet.personalRecordsAgainst(previous: List<WorkoutSet>): Set<PersonalRecordType> {
    if (
        !isCompleted ||
        setType == WorkoutSetType.WARM_UP ||
        weight.grams == 0L ||
        repetitions.count == 0
    ) {
        return emptySet()
    }
    val comparable = previous.filter {
        it.isCompleted &&
            it.setType != WorkoutSetType.WARM_UP &&
            it.weight.grams > 0 &&
            it.repetitions.count > 0
    }
    val records = mutableSetOf<PersonalRecordType>()
    if (comparable.none { it.weight.grams >= weight.grams }) {
        records += PersonalRecordType.WEIGHT
    }
    val volume = weight.grams * repetitions.count
    if (comparable.none { it.weight.grams * it.repetitions.count >= volume }) {
        records += PersonalRecordType.SET_VOLUME
    }
    return records
}

enum class WorkoutSessionStatus {
    ACTIVE,
    COMPLETED,
    DISCARDED,
    TUTORIAL,
}

data class WorkoutSession(
    val id: WorkoutSessionId,
    val routineId: RoutineId?,
    val name: String,
    val startedAt: Instant,
    val finishedAt: Instant?,
    val status: WorkoutSessionStatus,
    val notes: String,
    val location: WorkoutLocation? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class WorkoutSessionExercise(
    val id: SessionExerciseId,
    val sessionId: WorkoutSessionId,
    val exerciseId: ExerciseId?,
    val exerciseNameSnapshot: String,
    val muscleGroupSnapshot: MuscleGroup,
    val mediaUriSnapshot: String? = null,
    val mediaTypeSnapshot: ExerciseMediaType? = null,
    val mediaThumbnailUriSnapshot: String? = null,
    val position: Int,
    val notes: String,
)

enum class WorkoutSetType {
    WARM_UP,
    NORMAL,
}

data class WorkoutSet(
    val id: WorkoutSetId,
    val sessionExerciseId: SessionExerciseId,
    val position: Int,
    val setType: WorkoutSetType,
    val weight: Weight,
    val repetitions: Repetitions,
    val rpe: Rpe?,
    val isCompleted: Boolean,
    val completedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class WorkoutExerciseDetails(
    val sessionExercise: WorkoutSessionExercise,
    val exercise: Exercise?,
    val sets: List<WorkoutSet>,
)

data class WorkoutDetails(
    val session: WorkoutSession,
    val exercises: List<WorkoutExerciseDetails>,
) {
    val completedSetCount: Int
        get() = exercises.sumOf { exercise ->
            exercise.sets.count {
                it.isCompleted && it.repetitions.count > 0
            }
        }

    val totalSetCount: Int
        get() = exercises.sumOf { exercise -> exercise.sets.size }

    val totalVolumeGrams: Long
        get() = exercises.sumOf { exercise ->
            exercise.sets
                .filter(WorkoutSet::isCompleted)
                .sumOf { set -> set.weight.grams * set.repetitions.count }
        }
}
