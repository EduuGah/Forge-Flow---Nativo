package com.forgeflow.core.data.importer

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.forgeflow.core.common.di.IoDispatcher
import com.forgeflow.core.common.result.AppError
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.auth.AuthRepository
import com.forgeflow.core.data.auth.requireCurrentUserId
import com.forgeflow.core.data.profile.ProfileRepository
import com.forgeflow.core.database.exercise.ExerciseDao
import com.forgeflow.core.database.exercise.ExerciseEntity
import com.forgeflow.core.database.workout.ImportedWorkoutEntities
import com.forgeflow.core.database.workout.WorkoutDao
import com.forgeflow.core.database.workout.WorkoutSessionEntity
import com.forgeflow.core.database.workout.WorkoutSessionExerciseEntity
import com.forgeflow.core.database.workout.WorkoutSetEntity
import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.core.model.WorkoutSessionStatus
import com.forgeflow.core.model.WorkoutSetType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.Reader
import java.text.Normalizer
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class HevyImportRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun previewWorkouts(sourceUri: String): DataResult<HevyImportPreview> =
        withContext(ioDispatcher) {
            runCatching {
                val uri = Uri.parse(sourceUri)
                val parsed = uri.reader().use(HevyCsvParser::parseWorkouts)
                HevyImportPreview(
                    fileName = uri.displayName(),
                    type = HevyImportFileType.WORKOUTS,
                    recordCount = parsed.workouts.size,
                    skippedRowCount = parsed.skippedRows,
                    earliestAt = parsed.workouts.minOfOrNull(HevyWorkoutImport::startTime),
                    latestAt = parsed.workouts.maxOfOrNull(HevyWorkoutImport::endTime),
                )
            }.asDataResult()
        }

    suspend fun previewMeasurements(sourceUri: String): DataResult<HevyImportPreview> =
        withContext(ioDispatcher) {
            runCatching {
                val uri = Uri.parse(sourceUri)
                val parsed = uri.reader().use(HevyCsvParser::parseMeasurements)
                HevyImportPreview(
                    fileName = uri.displayName(),
                    type = HevyImportFileType.MEASUREMENTS,
                    recordCount = parsed.entries.size,
                    skippedRowCount = parsed.skippedRows,
                    earliestAt = parsed.entries.minOfOrNull { it.measuredAt },
                    latestAt = parsed.entries.maxOfOrNull { it.measuredAt },
                )
            }.asDataResult()
        }

    suspend fun import(
        workoutSourceUri: String?,
        measurementSourceUri: String?,
    ): DataResult<HevyImportResult> = withContext(ioDispatcher) {
        runCatching {
            var workoutsImported = 0
            var workoutsAlreadyImported = 0
            var measurementsImported = 0
            var measurementsAlreadyImported = 0
            var skippedRows = 0
            val ownerUserId = authRepository.requireCurrentUserId()
            workoutSourceUri?.let { source ->
                val parsed = Uri.parse(source).reader().use(HevyCsvParser::parseWorkouts)
                val exercises = exerciseDao.getAll()
                parsed.workouts.forEach { workout ->
                    if (
                        workoutDao.insertImportedWorkout(
                            workout.asEntities(exercises, ownerUserId),
                        )
                    ) {
                        workoutsImported += 1
                    } else {
                        workoutsAlreadyImported += 1
                    }
                }
                skippedRows += parsed.skippedRows
            }
            measurementSourceUri?.let { source ->
                val parsed = Uri.parse(source).reader().use(HevyCsvParser::parseMeasurements)
                when (val result = profileRepository.mergeBodyWeightEntries(parsed.entries)) {
                    is DataResult.Success -> {
                        measurementsImported = result.value
                        measurementsAlreadyImported = parsed.entries.size - result.value
                    }
                    is DataResult.Failure -> error("Unable to save body measurements")
                }
                skippedRows += parsed.skippedRows
            }
            HevyImportResult(
                workoutsImported = workoutsImported,
                workoutsAlreadyImported = workoutsAlreadyImported,
                measurementsImported = measurementsImported,
                measurementsAlreadyImported = measurementsAlreadyImported,
                skippedRows = skippedRows,
            )
        }.asDataResult()
    }

    private fun HevyWorkoutImport.asEntities(
        catalog: List<ExerciseEntity>,
        ownerUserId: String,
    ): ImportedWorkoutEntities {
        val sessionTimestamp = endTime.toEpochMilli()
        val sessionId = stableId("$ownerUserId|$id")
        val blocks = mutableListOf<MutableList<HevySetImport>>()
        rows.forEach { row ->
            val current = blocks.lastOrNull()
            if (current == null || current.last().exerciseTitle != row.exerciseTitle) {
                blocks += mutableListOf(row)
            } else {
                current += row
            }
        }
        val exerciseEntities = mutableListOf<WorkoutSessionExerciseEntity>()
        val setEntities = mutableListOf<WorkoutSetEntity>()
        blocks.forEachIndexed { exercisePosition, block ->
            val first = block.first()
            val matched = first.exerciseTitle.bestCatalogMatch(catalog)
            val sessionExerciseId = stableId(
                "$sessionId|exercise|$exercisePosition|${first.exerciseTitle}",
            )
            val cardioDetails = block.mapNotNull { row ->
                when {
                    row.distanceKilometers != null -> "${row.distanceKilometers} km"
                    row.durationSeconds != null -> "${row.durationSeconds} s"
                    else -> null
                }
            }.distinct()
            exerciseEntities += WorkoutSessionExerciseEntity(
                id = sessionExerciseId,
                sessionId = sessionId,
                exerciseId = matched?.id,
                exerciseNameSnapshot = first.exerciseTitle,
                muscleGroupSnapshot = matched?.primaryMuscleGroup ?: MuscleGroup.FULL_BODY.name,
                mediaUriSnapshot = matched?.mediaUri,
                mediaTypeSnapshot = matched?.mediaType,
                mediaThumbnailUriSnapshot = matched?.mediaThumbnailUri,
                position = exercisePosition,
                notes = listOf(first.exerciseNotes, cardioDetails.joinToString(" · "))
                    .filter(String::isNotBlank)
                    .joinToString("\n"),
            )
            block.forEachIndexed { setPosition, row ->
                setEntities += WorkoutSetEntity(
                    id = stableId("$sessionExerciseId|set|${row.sourceRow}"),
                    sessionExerciseId = sessionExerciseId,
                    position = setPosition,
                    setType = if (row.setType.equals("warmup", ignoreCase = true)) {
                        WorkoutSetType.WARM_UP.name
                    } else {
                        WorkoutSetType.NORMAL.name
                    },
                    weightGrams = row.weightGrams,
                    repetitions = row.repetitions,
                    rpe = row.rpe,
                    isCompleted = row.repetitions > 0 || row.durationSeconds != null ||
                        row.distanceKilometers != null,
                    completedAtEpochMillis = sessionTimestamp,
                    createdAtEpochMillis = sessionTimestamp,
                    updatedAtEpochMillis = sessionTimestamp,
                )
            }
        }
        return ImportedWorkoutEntities(
            session = WorkoutSessionEntity(
                id = sessionId,
                ownerUserId = ownerUserId,
                routineId = null,
                name = title,
                startedAtEpochMillis = startTime.toEpochMilli(),
                finishedAtEpochMillis = sessionTimestamp,
                status = WorkoutSessionStatus.COMPLETED.name,
                notes = description,
                createdAtEpochMillis = sessionTimestamp,
                updatedAtEpochMillis = sessionTimestamp,
            ),
            exercises = exerciseEntities,
            sets = setEntities,
        )
    }

    private fun String.bestCatalogMatch(catalog: List<ExerciseEntity>): ExerciseEntity? {
        val sourceTokens = exerciseTokens()
        if (sourceTokens.isEmpty()) return null
        return catalog.map { candidate ->
            val candidateTokens = candidate.name.exerciseTokens()
            val intersection = sourceTokens.intersect(candidateTokens).size.toDouble()
            val union = sourceTokens.union(candidateTokens).size.toDouble().coerceAtLeast(1.0)
            candidate to intersection / union
        }.maxByOrNull { it.second }
            ?.takeIf { it.second >= MIN_EXERCISE_MATCH_SCORE }
            ?.first
    }

    private fun String.exerciseTokens(): Set<String> = normalized()
        .replace("halteres", "halter")
        .replace("dumbbell", "halter")
        .replace("barbell", "barra")
        .replace("maquina", "machine")
        .split("[^a-z0-9]+".toRegex())
        .filterNotTo(linkedSetOf()) { it.isBlank() || it in STOP_WORDS }

    private fun String.normalized(): String = Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
        .lowercase(Locale.ROOT)

    private fun Uri.reader(): Reader = context.contentResolver.openInputStream(this)
        ?.bufferedReader(Charsets.UTF_8)
        ?: error("Unable to open CSV")

    private fun Uri.displayName(): String {
        return context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        } ?: lastPathSegment ?: "arquivo.csv"
    }

    private fun <T> Result<T>.asDataResult(): DataResult<T> = fold(
        onSuccess = { DataResult.Success(it) },
        onFailure = { DataResult.Failure(AppError.LocalDataUnavailable) },
    )

    private fun stableId(value: String): String = UUID.nameUUIDFromBytes(
        value.toByteArray(Charsets.UTF_8),
    ).toString()

    private companion object {
        const val MIN_EXERCISE_MATCH_SCORE = 0.6
        val STOP_WORDS = setOf("com", "de", "da", "do", "em", "no", "na", "the")
    }
}
