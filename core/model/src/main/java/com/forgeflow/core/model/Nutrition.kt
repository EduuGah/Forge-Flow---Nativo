package com.forgeflow.core.model

import java.time.Instant

enum class NutritionMealType {
    BREAKFAST,
    LUNCH,
    SNACK,
    DINNER,
    PRE_WORKOUT,
    POST_WORKOUT,
    OTHER,
}

data class NutritionMeal(
    val id: String,
    val name: String,
    val type: NutritionMealType,
    val calories: Int,
    val proteinGrams: Double,
    val carbohydrateGrams: Double,
    val fatGrams: Double,
    val notes: String,
    val photoPath: String?,
    val eatenAt: Instant,
)

data class NutritionGoals(
    val calories: Int = 2_200,
    val proteinGrams: Double = 160.0,
    val carbohydrateGrams: Double = 250.0,
    val fatGrams: Double = 70.0,
)

data class NutritionJournal(
    val meals: List<NutritionMeal> = emptyList(),
    val goals: NutritionGoals = NutritionGoals(),
)
