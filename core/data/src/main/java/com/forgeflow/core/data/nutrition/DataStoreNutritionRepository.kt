package com.forgeflow.core.data.nutrition

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.forgeflow.core.common.di.IoDispatcher
import com.forgeflow.core.common.result.AppError
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.model.NutritionGoals
import com.forgeflow.core.model.NutritionJournal
import com.forgeflow.core.model.NutritionMeal
import com.forgeflow.core.model.NutritionMealType
import com.forgeflow.core.model.HydrationEntry
import com.forgeflow.core.model.NutritionReminderSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DataStoreNutritionRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : NutritionRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override fun observeJournal(): Flow<NutritionJournal> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            NutritionJournal(
                meals = preferences[MEALS]
                    ?.let { encoded ->
                        runCatching {
                            json.decodeFromString<List<NutritionMealDto>>(encoded)
                                .map(NutritionMealDto::asModel)
                                .sortedByDescending(NutritionMeal::eatenAt)
                        }.getOrDefault(emptyList())
                    }
                    .orEmpty(),
                goals = preferences[GOALS]
                    ?.let { encoded ->
                        runCatching {
                            json.decodeFromString<NutritionGoalsDto>(encoded).asModel()
                        }.getOrNull()
                    }
                    ?: NutritionGoals(),
                hydration = preferences[HYDRATION]
                    ?.let { encoded ->
                        runCatching {
                            json.decodeFromString<List<HydrationEntryDto>>(encoded)
                                .map(HydrationEntryDto::asModel)
                                .sortedByDescending(HydrationEntry::consumedAt)
                        }.getOrDefault(emptyList())
                    }
                    .orEmpty(),
                reminders = preferences[REMINDERS]
                    ?.let { encoded ->
                        runCatching {
                            json.decodeFromString<NutritionReminderSettingsDto>(encoded).asModel()
                        }.getOrNull()
                    }
                    ?: NutritionReminderSettings(),
            )
        }

    override suspend fun saveMeal(
        meal: NutritionMeal,
        sourcePhotoUri: String?,
    ): DataResult<Unit> = withContext(ioDispatcher) {
        var importedPhotoPath: String? = null
        runCatching {
            importedPhotoPath = sourcePhotoUri?.let { uri -> importPhoto(meal.id, Uri.parse(uri)) }
            dataStore.edit { preferences ->
                val meals = preferences[MEALS]
                    ?.let { encoded ->
                        runCatching {
                            json.decodeFromString<List<NutritionMealDto>>(encoded).toMutableList()
                        }.getOrDefault(mutableListOf())
                    }
                    ?: mutableListOf()
                val existingIndex = meals.indexOfFirst { it.id == meal.id }
                val existingPhoto = meals.getOrNull(existingIndex)?.photoPath
                val stored = NutritionMealDto.from(
                    meal.copy(photoPath = importedPhotoPath ?: meal.photoPath ?: existingPhoto),
                )
                if (existingIndex >= 0) meals[existingIndex] = stored else meals += stored
                preferences[MEALS] = json.encodeToString(meals)
            }
        }.fold(
            onSuccess = { DataResult.Success(Unit) },
            onFailure = {
                importedPhotoPath?.let(::deletePrivatePhoto)
                DataResult.Failure(AppError.WriteFailed)
            },
        )
    }

    override suspend fun deleteMeal(mealId: String): DataResult<Unit> = withContext(ioDispatcher) {
        var photoPath: String? = null
        runCatching {
            dataStore.edit { preferences ->
                val meals = preferences[MEALS]
                    ?.let { encoded ->
                        runCatching {
                            json.decodeFromString<List<NutritionMealDto>>(encoded).toMutableList()
                        }.getOrDefault(mutableListOf())
                    }
                    ?: mutableListOf()
                photoPath = meals.firstOrNull { it.id == mealId }?.photoPath
                meals.removeAll { it.id == mealId }
                preferences[MEALS] = json.encodeToString(meals)
            }
            photoPath?.let(::deletePrivatePhoto)
        }.fold(
            onSuccess = { DataResult.Success(Unit) },
            onFailure = { DataResult.Failure(AppError.WriteFailed) },
        )
    }

    override suspend fun saveGoals(goals: NutritionGoals): DataResult<Unit> =
        runCatching {
            dataStore.edit { preferences ->
                preferences[GOALS] = json.encodeToString(NutritionGoalsDto.from(goals))
            }
        }.fold(
            onSuccess = { DataResult.Success(Unit) },
            onFailure = { DataResult.Failure(AppError.WriteFailed) },
        )

    override suspend fun addHydration(entry: HydrationEntry): DataResult<Unit> = runCatching {
        dataStore.edit { preferences ->
            val entries = preferences[HYDRATION]
                ?.let { encoded ->
                    runCatching {
                        json.decodeFromString<List<HydrationEntryDto>>(encoded).toMutableList()
                    }.getOrDefault(mutableListOf())
                }
                ?: mutableListOf()
            entries.removeAll { it.id == entry.id }
            entries += HydrationEntryDto.from(entry)
            preferences[HYDRATION] = json.encodeToString(entries)
        }
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun removeHydration(entryId: String): DataResult<Unit> = runCatching {
        dataStore.edit { preferences ->
            val entries = preferences[HYDRATION]
                ?.let { encoded ->
                    runCatching {
                        json.decodeFromString<List<HydrationEntryDto>>(encoded).toMutableList()
                    }.getOrDefault(mutableListOf())
                }
                ?: mutableListOf()
            entries.removeAll { it.id == entryId }
            preferences[HYDRATION] = json.encodeToString(entries)
        }
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun saveReminderSettings(
        settings: NutritionReminderSettings,
    ): DataResult<Unit> = runCatching {
        dataStore.edit { preferences ->
            preferences[REMINDERS] = json.encodeToString(
                NutritionReminderSettingsDto.from(settings),
            )
        }
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    private fun importPhoto(mealId: String, sourceUri: Uri): String {
        val directory = nutritionPhotoDirectory().apply { mkdirs() }
        require(directory.isDirectory)
        val extension = when (context.contentResolver.getType(sourceUri)) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val target = File(directory, "$mealId.$extension")
        context.contentResolver.openInputStream(sourceUri).use { input ->
            requireNotNull(input)
            target.outputStream().use(input::copyTo)
        }
        require(target.length() > 0)
        return target.absolutePath
    }

    private fun nutritionPhotoDirectory(): File = File(context.filesDir, PHOTO_DIRECTORY)

    private fun deletePrivatePhoto(path: String) {
        val directory = nutritionPhotoDirectory().canonicalFile
        val photo = File(path).canonicalFile
        if (photo.parentFile == directory) photo.delete()
    }

    private companion object {
        val MEALS = stringPreferencesKey("nutrition_meals_json")
        val GOALS = stringPreferencesKey("nutrition_goals_json")
        val HYDRATION = stringPreferencesKey("nutrition_hydration_json")
        val REMINDERS = stringPreferencesKey("nutrition_reminders_json")
        const val PHOTO_DIRECTORY = "nutrition_photos"
    }
}

@Serializable
private data class NutritionMealDto(
    val id: String,
    val name: String,
    val type: String,
    val calories: Int,
    val proteinGrams: Double,
    val carbohydrateGrams: Double,
    val fatGrams: Double,
    val notes: String,
    val photoPath: String?,
    val eatenAtEpochMillis: Long,
) {
    fun asModel(): NutritionMeal = NutritionMeal(
        id = id,
        name = name,
        type = NutritionMealType.entries.firstOrNull { it.name == type }
            ?: NutritionMealType.OTHER,
        calories = calories,
        proteinGrams = proteinGrams,
        carbohydrateGrams = carbohydrateGrams,
        fatGrams = fatGrams,
        notes = notes,
        photoPath = photoPath,
        eatenAt = Instant.ofEpochMilli(eatenAtEpochMillis),
    )

    companion object {
        fun from(meal: NutritionMeal): NutritionMealDto = NutritionMealDto(
            id = meal.id,
            name = meal.name,
            type = meal.type.name,
            calories = meal.calories,
            proteinGrams = meal.proteinGrams,
            carbohydrateGrams = meal.carbohydrateGrams,
            fatGrams = meal.fatGrams,
            notes = meal.notes,
            photoPath = meal.photoPath,
            eatenAtEpochMillis = meal.eatenAt.toEpochMilli(),
        )
    }
}

@Serializable
private data class NutritionGoalsDto(
    val calories: Int,
    val proteinGrams: Double,
    val carbohydrateGrams: Double,
    val fatGrams: Double,
    val waterMilliliters: Int = 2_500,
) {
    fun asModel(): NutritionGoals = NutritionGoals(
        calories = calories,
        proteinGrams = proteinGrams,
        carbohydrateGrams = carbohydrateGrams,
        fatGrams = fatGrams,
        waterMilliliters = waterMilliliters,
    )

    companion object {
        fun from(goals: NutritionGoals): NutritionGoalsDto = NutritionGoalsDto(
            calories = goals.calories,
            proteinGrams = goals.proteinGrams,
            carbohydrateGrams = goals.carbohydrateGrams,
            fatGrams = goals.fatGrams,
            waterMilliliters = goals.waterMilliliters,
        )
    }
}

@Serializable
private data class HydrationEntryDto(
    val id: String,
    val milliliters: Int,
    val consumedAtEpochMillis: Long,
) {
    fun asModel(): HydrationEntry = HydrationEntry(
        id = id,
        milliliters = milliliters,
        consumedAt = Instant.ofEpochMilli(consumedAtEpochMillis),
    )

    companion object {
        fun from(entry: HydrationEntry): HydrationEntryDto = HydrationEntryDto(
            id = entry.id,
            milliliters = entry.milliliters,
            consumedAtEpochMillis = entry.consumedAt.toEpochMilli(),
        )
    }
}

@Serializable
private data class NutritionReminderSettingsDto(
    val enabled: Boolean = false,
    val intervalHours: Int = 2,
) {
    fun asModel(): NutritionReminderSettings = NutritionReminderSettings(
        enabled = enabled,
        intervalHours = intervalHours,
    )

    companion object {
        fun from(settings: NutritionReminderSettings): NutritionReminderSettingsDto =
            NutritionReminderSettingsDto(
                enabled = settings.enabled,
                intervalHours = settings.intervalHours,
            )
    }
}
