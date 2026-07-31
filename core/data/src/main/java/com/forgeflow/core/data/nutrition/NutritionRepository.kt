package com.forgeflow.core.data.nutrition

import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.model.NutritionGoals
import com.forgeflow.core.model.NutritionJournal
import com.forgeflow.core.model.NutritionMeal
import kotlinx.coroutines.flow.Flow

interface NutritionRepository {
    fun observeJournal(): Flow<NutritionJournal>

    suspend fun saveMeal(
        meal: NutritionMeal,
        sourcePhotoUri: String?,
    ): DataResult<Unit>

    suspend fun deleteMeal(mealId: String): DataResult<Unit>

    suspend fun saveGoals(goals: NutritionGoals): DataResult<Unit>
}
