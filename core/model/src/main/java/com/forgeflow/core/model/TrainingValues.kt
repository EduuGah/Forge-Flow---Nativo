package com.forgeflow.core.model

@JvmInline
value class Weight private constructor(val grams: Long) {
    companion object {
        val Zero = Weight(0)

        fun fromGrams(grams: Long): Weight {
            require(grams >= 0) { "Weight cannot be negative" }
            return Weight(grams)
        }
    }
}

@JvmInline
value class Repetitions(val count: Int) {
    init {
        require(count >= 0) { "Repetitions cannot be negative" }
    }
}

data class RepetitionRange(
    val minimum: Int,
    val maximum: Int,
) {
    init {
        require(minimum >= 0) { "Minimum repetitions cannot be negative" }
        require(maximum >= minimum) { "Maximum repetitions must be at least the minimum" }
    }
}

@JvmInline
value class Rpe(val value: Int) {
    init {
        require(value in 1..10) { "RPE must be between 1 and 10" }
    }
}
