package com.forgeflow.core.model

import java.time.Instant

enum class PerformanceGoalType {
    EXERCISE_WEIGHT,
    WORKOUT_COUNT,
    TOTAL_VOLUME,
    TRAINING_DURATION,
}

enum class GoalCadence {
    ONCE,
    DAILY,
    WEEKLY,
    MONTHLY,
}

data class PerformanceGoal(
    val id: String,
    val type: PerformanceGoalType,
    val cadence: GoalCadence,
    val title: String,
    val targetValue: Long,
    val exerciseId: ExerciseId? = null,
    val exerciseNameSnapshot: String? = null,
    val minimumRepetitions: Int = 1,
    val deadlineEpochDay: Long? = null,
    val createdAt: Instant,
) {
    init {
        require(id.isNotBlank()) { "Goal id cannot be blank" }
        require(targetValue > 0L) { "Goal target must be positive" }
        require(minimumRepetitions > 0) { "Minimum repetitions must be positive" }
        if (type == PerformanceGoalType.EXERCISE_WEIGHT) {
            requireNotNull(exerciseId) { "Exercise goal requires an exercise" }
            require(!exerciseNameSnapshot.isNullOrBlank()) {
                "Exercise goal requires an exercise name"
            }
            require(cadence == GoalCadence.ONCE) {
                "Exercise goals use one-time cadence"
            }
        }
    }
}
