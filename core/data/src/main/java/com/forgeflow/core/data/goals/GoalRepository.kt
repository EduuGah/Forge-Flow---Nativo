package com.forgeflow.core.data.goals

import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.model.PerformanceGoal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun observeGoals(): Flow<List<PerformanceGoal>>

    suspend fun saveGoal(goal: PerformanceGoal): DataResult<Unit>

    suspend fun deleteGoal(goalId: String): DataResult<Unit>
}
