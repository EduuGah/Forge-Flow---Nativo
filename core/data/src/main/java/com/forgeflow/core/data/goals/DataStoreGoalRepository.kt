package com.forgeflow.core.data.goals

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.forgeflow.core.common.result.AppError
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.model.ExerciseId
import com.forgeflow.core.model.GoalCadence
import com.forgeflow.core.model.PerformanceGoal
import com.forgeflow.core.model.PerformanceGoalType
import java.io.IOException
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DataStoreGoalRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : GoalRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override fun observeGoals(): Flow<List<PerformanceGoal>> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences -> decodeGoals(preferences[GOALS]) }

    override suspend fun saveGoal(goal: PerformanceGoal): DataResult<Unit> = runCatching {
        dataStore.edit { preferences ->
            val goals = decodeGoals(preferences[GOALS]).toMutableList()
            val existingIndex = goals.indexOfFirst { it.id == goal.id }
            if (existingIndex >= 0) goals[existingIndex] = goal else goals += goal
            preferences[GOALS] = json.encodeToString(
                goals.sortedBy(PerformanceGoal::createdAt).map(PerformanceGoalDto::from),
            )
        }
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun deleteGoal(goalId: String): DataResult<Unit> = runCatching {
        dataStore.edit { preferences ->
            val goals = decodeGoals(preferences[GOALS]).filterNot { it.id == goalId }
            preferences[GOALS] = json.encodeToString(goals.map(PerformanceGoalDto::from))
        }
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    private fun decodeGoals(encoded: String?): List<PerformanceGoal> {
        if (encoded.isNullOrBlank()) return emptyList()
        return runCatching {
            json.decodeFromString<List<PerformanceGoalDto>>(encoded)
                .mapNotNull { dto -> runCatching(dto::asModel).getOrNull() }
                .sortedByDescending(PerformanceGoal::createdAt)
        }.getOrDefault(emptyList())
    }

    private companion object {
        val GOALS = stringPreferencesKey("performance_goals_v1")
    }
}

@Serializable
private data class PerformanceGoalDto(
    val id: String,
    val type: String,
    val cadence: String,
    val title: String,
    val targetValue: Long,
    val exerciseId: String? = null,
    val exerciseNameSnapshot: String? = null,
    val minimumRepetitions: Int = 1,
    val deadlineEpochDay: Long? = null,
    val createdAtEpochMillis: Long,
) {
    fun asModel(): PerformanceGoal = PerformanceGoal(
        id = id,
        type = PerformanceGoalType.valueOf(type),
        cadence = GoalCadence.valueOf(cadence),
        title = title,
        targetValue = targetValue,
        exerciseId = exerciseId?.let(::ExerciseId),
        exerciseNameSnapshot = exerciseNameSnapshot,
        minimumRepetitions = minimumRepetitions,
        deadlineEpochDay = deadlineEpochDay,
        createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    )

    companion object {
        fun from(goal: PerformanceGoal): PerformanceGoalDto = PerformanceGoalDto(
            id = goal.id,
            type = goal.type.name,
            cadence = goal.cadence.name,
            title = goal.title,
            targetValue = goal.targetValue,
            exerciseId = goal.exerciseId?.value,
            exerciseNameSnapshot = goal.exerciseNameSnapshot,
            minimumRepetitions = goal.minimumRepetitions,
            deadlineEpochDay = goal.deadlineEpochDay,
            createdAtEpochMillis = goal.createdAt.toEpochMilli(),
        )
    }
}
