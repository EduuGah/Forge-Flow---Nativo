package com.forgeflow.core.data.profile

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.forgeflow.core.common.di.IoDispatcher
import com.forgeflow.core.common.result.AppError
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.model.ExperienceLevel
import com.forgeflow.core.model.ProgressPhoto
import com.forgeflow.core.model.TrainingGoal
import com.forgeflow.core.model.UserProfile
import com.forgeflow.core.model.Weight
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DataStoreProfileRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ProfileRepository {
    override fun observeProfile(): Flow<UserProfile> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw error
            }
        }
        .map(::profileFrom)

    override suspend fun saveProfile(profile: UserProfile): DataResult<Unit> =
        updatePreferences { preferences ->
            preferences[DISPLAY_NAME] = profile.displayName.trim()
            preferences.setOrRemove(BIRTH_YEAR, profile.birthYear)
            preferences.setOrRemove(HEIGHT_CENTIMETERS, profile.heightCentimeters)
            preferences.setOrRemove(BODY_WEIGHT_GRAMS, profile.bodyWeight?.grams)
            preferences[TRAINING_GOAL] = profile.trainingGoal.name
            preferences[EXPERIENCE_LEVEL] = profile.experienceLevel.name
        }

    override suspend fun importProgressPhoto(
        sourceUri: String,
    ): DataResult<Unit> = withContext(ioDispatcher) {
        val photoDirectory = progressPhotoDirectory()
        val id = UUID.randomUUID().toString()
        val source = Uri.parse(sourceUri)
        val target = File(photoDirectory, "$id.${source.imageExtension()}")
        runCatching {
            photoDirectory.mkdirs()
            require(photoDirectory.isDirectory)
            context.contentResolver.openInputStream(source).use { input ->
                requireNotNull(input)
                target.outputStream().use(input::copyTo)
            }
            require(target.length() > 0)
            dataStore.edit { preferences ->
                val photos = preferences[PROGRESS_PHOTOS].orEmpty().toMutableSet()
                photos += ProgressPhoto(
                    id = id,
                    filePath = target.absolutePath,
                    capturedAt = Instant.now(),
                ).encode()
                preferences[PROGRESS_PHOTOS] = photos
            }
        }.fold(
            onSuccess = { DataResult.Success(Unit) },
            onFailure = {
                target.delete()
                DataResult.Failure(AppError.WriteFailed)
            },
        )
    }

    override suspend fun deleteProgressPhoto(
        photoId: String,
    ): DataResult<Unit> = withContext(ioDispatcher) {
        var filePath: String? = null
        runCatching {
            dataStore.edit { preferences ->
                val photos = preferences[PROGRESS_PHOTOS].orEmpty()
                val retained = photos.filterTo(mutableSetOf()) { encoded ->
                    val photo = encoded.decodePhoto()
                    if (photo?.id == photoId) {
                        filePath = photo.filePath
                        false
                    } else {
                        true
                    }
                }
                preferences[PROGRESS_PHOTOS] = retained
            }
            filePath?.let(::deletePrivatePhoto)
        }.fold(
            onSuccess = { DataResult.Success(Unit) },
            onFailure = { DataResult.Failure(AppError.WriteFailed) },
        )
    }

    private suspend fun updatePreferences(
        transform: suspend (MutablePreferences) -> Unit,
    ): DataResult<Unit> = runCatching {
        dataStore.edit(transform)
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    private fun profileFrom(preferences: Preferences): UserProfile = UserProfile(
        displayName = preferences[DISPLAY_NAME].orEmpty(),
        birthYear = preferences[BIRTH_YEAR],
        heightCentimeters = preferences[HEIGHT_CENTIMETERS],
        bodyWeight = preferences[BODY_WEIGHT_GRAMS]?.let(Weight::fromGrams),
        trainingGoal = preferences[TRAINING_GOAL]
            ?.let { stored -> TrainingGoal.entries.firstOrNull { it.name == stored } }
            ?: TrainingGoal.HYPERTROPHY,
        experienceLevel = preferences[EXPERIENCE_LEVEL]
            ?.let { stored -> ExperienceLevel.entries.firstOrNull { it.name == stored } }
            ?: ExperienceLevel.INTERMEDIATE,
        progressPhotos = preferences[PROGRESS_PHOTOS]
            .orEmpty()
            .mapNotNull { encoded -> encoded.decodePhoto() }
            .sortedBy(ProgressPhoto::capturedAt),
    )

    private fun ProgressPhoto.encode(): String =
        "$id$PHOTO_SEPARATOR${capturedAt.toEpochMilli()}$PHOTO_SEPARATOR$filePath"

    private fun String.decodePhoto(): ProgressPhoto? {
        val fields = split(PHOTO_SEPARATOR, limit = 3)
        if (fields.size != 3) return null
        return ProgressPhoto(
            id = fields[0],
            capturedAt = fields[1].toLongOrNull()?.let(Instant::ofEpochMilli) ?: return null,
            filePath = fields[2],
        )
    }

    private fun Uri.imageExtension(): String = when (context.contentResolver.getType(this)) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/gif" -> "gif"
        "image/heic", "image/heif" -> "heic"
        else -> "jpg"
    }

    private fun progressPhotoDirectory(): File =
        File(context.filesDir, PROGRESS_PHOTO_DIRECTORY)

    private fun deletePrivatePhoto(path: String) {
        val directory = progressPhotoDirectory().canonicalFile
        val photo = File(path).canonicalFile
        if (photo.parentFile == directory) {
            photo.delete()
        }
    }

    private fun <T> MutablePreferences.setOrRemove(
        key: Preferences.Key<T>,
        value: T?,
    ) {
        if (value == null) remove(key) else this[key] = value
    }

    private companion object {
        val DISPLAY_NAME = stringPreferencesKey("profile_display_name")
        val BIRTH_YEAR = intPreferencesKey("profile_birth_year")
        val HEIGHT_CENTIMETERS = intPreferencesKey("profile_height_centimeters")
        val BODY_WEIGHT_GRAMS = longPreferencesKey("profile_body_weight_grams")
        val TRAINING_GOAL = stringPreferencesKey("profile_training_goal")
        val EXPERIENCE_LEVEL = stringPreferencesKey("profile_experience_level")
        val PROGRESS_PHOTOS = stringSetPreferencesKey("profile_progress_photos")
        const val PROGRESS_PHOTO_DIRECTORY = "progress_photos"
        const val PHOTO_SEPARATOR = '\t'
    }
}
