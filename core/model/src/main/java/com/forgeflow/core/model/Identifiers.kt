package com.forgeflow.core.model

import java.util.UUID

@JvmInline
value class ExerciseId(val value: String) {
    init {
        requireUuid(value)
    }

    companion object {
        fun create(): ExerciseId = ExerciseId(UUID.randomUUID().toString())
    }
}

@JvmInline
value class RoutineId(val value: String) {
    init {
        requireUuid(value)
    }

    companion object {
        fun create(): RoutineId = RoutineId(UUID.randomUUID().toString())
    }
}

@JvmInline
value class RoutineFolderId(val value: String) {
    init {
        requireUuid(value)
    }

    companion object {
        fun create(): RoutineFolderId = RoutineFolderId(UUID.randomUUID().toString())
    }
}

@JvmInline
value class RoutineExerciseId(val value: String) {
    init {
        requireUuid(value)
    }

    companion object {
        fun create(): RoutineExerciseId = RoutineExerciseId(UUID.randomUUID().toString())
    }
}

@JvmInline
value class WorkoutSessionId(val value: String) {
    init {
        requireUuid(value)
    }

    companion object {
        fun create(): WorkoutSessionId = WorkoutSessionId(UUID.randomUUID().toString())
    }
}

@JvmInline
value class SessionExerciseId(val value: String) {
    init {
        requireUuid(value)
    }

    companion object {
        fun create(): SessionExerciseId = SessionExerciseId(UUID.randomUUID().toString())
    }
}

@JvmInline
value class WorkoutSetId(val value: String) {
    init {
        requireUuid(value)
    }

    companion object {
        fun create(): WorkoutSetId = WorkoutSetId(UUID.randomUUID().toString())
    }
}

private fun requireUuid(value: String) {
    require(runCatching { UUID.fromString(value) }.isSuccess) {
        "Identifier must be a valid UUID"
    }
}
