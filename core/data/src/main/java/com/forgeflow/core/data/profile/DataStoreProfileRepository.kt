package com.forgeflow.core.data.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
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
import com.forgeflow.core.model.BodyWeightEntry
import com.forgeflow.core.model.BodyWeightSource
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

    override suspend fun importProfilePhoto(
        sourceUri: String,
        zoom: Float,
        horizontalOffset: Float,
        verticalOffset: Float,
    ): DataResult<Unit> = withContext(ioDispatcher) {
        val photoDirectory = profileDirectory()
        val source = Uri.parse(sourceUri)
        val target = File(photoDirectory, "avatar.jpg")
        val temporary = File(photoDirectory, "avatar.tmp.jpg")
        runCatching {
            photoDirectory.mkdirs()
            require(photoDirectory.isDirectory)
            val sourceBitmap = decodeBitmap(source)
            val croppedBitmap = sourceBitmap.cropSquare(
                outputSize = AVATAR_OUTPUT_SIZE,
                zoom = zoom.coerceIn(MIN_AVATAR_ZOOM, MAX_AVATAR_ZOOM),
                horizontalOffset = horizontalOffset.coerceIn(-1f, 1f),
                verticalOffset = verticalOffset.coerceIn(-1f, 1f),
            )
            temporary.outputStream().use { output ->
                check(croppedBitmap.compress(Bitmap.CompressFormat.JPEG, AVATAR_JPEG_QUALITY, output))
            }
            if (croppedBitmap !== sourceBitmap) croppedBitmap.recycle()
            sourceBitmap.recycle()
            require(temporary.length() > 0)
            photoDirectory.listFiles()
                .orEmpty()
                .filter { it.name.startsWith("avatar.") && it != temporary }
                .forEach(File::delete)
            require(temporary.renameTo(target))
            dataStore.edit { preferences ->
                preferences[PROFILE_PHOTO_PATH] = target.absolutePath
            }
        }.fold(
            onSuccess = { DataResult.Success(Unit) },
            onFailure = {
                temporary.delete()
                DataResult.Failure(AppError.WriteFailed)
            },
        )
    }

    private fun decodeBitmap(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val longestEdge = maxOf(info.size.width, info.size.height)
                if (longestEdge > MAX_SOURCE_EDGE) {
                    val ratio = MAX_SOURCE_EDGE.toFloat() / longestEdge
                    decoder.setTargetSize(
                        (info.size.width * ratio).toInt().coerceAtLeast(1),
                        (info.size.height * ratio).toInt().coerceAtLeast(1),
                    )
                }
            }
        } else {
            context.contentResolver.openInputStream(uri).use { input ->
                requireNotNull(input)
                requireNotNull(BitmapFactory.decodeStream(input))
            }
        }
    }

    private fun Bitmap.cropSquare(
        outputSize: Int,
        zoom: Float,
        horizontalOffset: Float,
        verticalOffset: Float,
    ): Bitmap {
        val output = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
        val baseScale = maxOf(
            outputSize.toFloat() / width,
            outputSize.toFloat() / height,
        )
        val scale = baseScale * zoom
        val scaledWidth = width * scale
        val scaledHeight = height * scale
        val maxHorizontalShift = ((scaledWidth - outputSize) / 2f).coerceAtLeast(0f)
        val maxVerticalShift = ((scaledHeight - outputSize) / 2f).coerceAtLeast(0f)
        val matrix = Matrix().apply {
            postScale(scale, scale)
            postTranslate(
                (outputSize - scaledWidth) / 2f + horizontalOffset * maxHorizontalShift,
                (outputSize - scaledHeight) / 2f + verticalOffset * maxVerticalShift,
            )
        }
        Canvas(output).drawBitmap(this, matrix, null)
        return output
    }

    override suspend fun addBodyWeight(
        weight: Weight,
        measuredAt: Instant,
        bodyFatPercent: Double?,
    ): DataResult<Unit> {
        return when (
            mergeBodyWeightEntries(
                listOf(
                    BodyWeightEntry(
                        id = UUID.randomUUID().toString(),
                        weight = weight,
                        measuredAt = measuredAt,
                        bodyFatPercent = bodyFatPercent,
                        source = BodyWeightSource.MANUAL,
                    ),
                ),
            )
        ) {
            is DataResult.Success -> DataResult.Success(Unit)
            is DataResult.Failure -> DataResult.Failure(AppError.WriteFailed)
        }
    }

    override suspend fun mergeBodyWeightEntries(
        entries: List<BodyWeightEntry>,
    ): DataResult<Int> = withContext(ioDispatcher) {
        var importedCount = 0
        runCatching {
            dataStore.edit { preferences ->
                val current = preferences[BODY_WEIGHT_HISTORY]
                    .orEmpty()
                    .mapNotNull { encoded -> encoded.decodeWeightEntry() }
                    .associateByTo(linkedMapOf(), BodyWeightEntry::id)
                entries.forEach { entry ->
                    if (entry.id !in current) importedCount += 1
                    current[entry.id] = entry
                }
                val sorted = current.values.sortedBy(BodyWeightEntry::measuredAt)
                preferences[BODY_WEIGHT_HISTORY] = sorted
                    .mapTo(mutableSetOf()) { entry -> entry.encode() }
                sorted.lastOrNull()?.let { latest ->
                    preferences[BODY_WEIGHT_GRAMS] = latest.weight.grams
                }
            }
        }.fold(
            onSuccess = { DataResult.Success(importedCount) },
            onFailure = { DataResult.Failure(AppError.WriteFailed) },
        )
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
        profilePhotoPath = preferences[PROFILE_PHOTO_PATH],
        bodyWeightHistory = preferences[BODY_WEIGHT_HISTORY]
            .orEmpty()
            .mapNotNull { encoded -> encoded.decodeWeightEntry() }
            .sortedBy(BodyWeightEntry::measuredAt),
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

    private fun BodyWeightEntry.encode(): String = listOf(
        id,
        measuredAt.toEpochMilli().toString(),
        weight.grams.toString(),
        bodyFatPercent?.toString().orEmpty(),
        source.name,
    ).joinToString(WEIGHT_SEPARATOR.toString())

    private fun String.decodeWeightEntry(): BodyWeightEntry? {
        val fields = split(WEIGHT_SEPARATOR, limit = 5)
        if (fields.size != 5) return null
        return BodyWeightEntry(
            id = fields[0],
            measuredAt = fields[1].toLongOrNull()?.let(Instant::ofEpochMilli) ?: return null,
            weight = fields[2].toLongOrNull()?.let(Weight::fromGrams) ?: return null,
            bodyFatPercent = fields[3].toDoubleOrNull(),
            source = BodyWeightSource.entries.firstOrNull { it.name == fields[4] }
                ?: BodyWeightSource.MANUAL,
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

    private fun profileDirectory(): File = File(context.filesDir, PROFILE_DIRECTORY)

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
        val PROFILE_PHOTO_PATH = stringPreferencesKey("profile_photo_path")
        val BODY_WEIGHT_HISTORY = stringSetPreferencesKey("profile_body_weight_history")
        const val PROGRESS_PHOTO_DIRECTORY = "progress_photos"
        const val PROFILE_DIRECTORY = "profile"
        const val PHOTO_SEPARATOR = '\t'
        const val WEIGHT_SEPARATOR = '\t'
        const val AVATAR_OUTPUT_SIZE = 1024
        const val MAX_SOURCE_EDGE = 4096
        const val AVATAR_JPEG_QUALITY = 90
        const val MIN_AVATAR_ZOOM = 1f
        const val MAX_AVATAR_ZOOM = 4f
    }
}
