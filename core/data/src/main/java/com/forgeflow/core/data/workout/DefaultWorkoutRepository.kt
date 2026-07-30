package com.forgeflow.core.data.workout

import com.forgeflow.core.common.result.AppError
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.common.time.AppClock
import com.forgeflow.core.database.routine.RoutineDao
import com.forgeflow.core.database.workout.WorkoutDao
import com.forgeflow.core.database.workout.WorkoutSessionEntity
import com.forgeflow.core.database.workout.WorkoutSessionExerciseEntity
import com.forgeflow.core.database.workout.WorkoutSetEntity
import com.forgeflow.core.database.workout.asExternalModel
import com.forgeflow.core.model.Repetitions
import com.forgeflow.core.model.RoutineId
import com.forgeflow.core.model.SessionExerciseId
import com.forgeflow.core.model.Weight
import com.forgeflow.core.model.WorkoutDetails
import com.forgeflow.core.model.WorkoutLocation
import com.forgeflow.core.model.WorkoutSessionId
import com.forgeflow.core.model.WorkoutSessionStatus
import com.forgeflow.core.model.WorkoutSetId
import com.forgeflow.core.model.WorkoutSetType
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DefaultWorkoutRepository @Inject constructor(
    private val routineDao: RoutineDao,
    private val workoutDao: WorkoutDao,
    private val clock: AppClock,
) : WorkoutRepository {
    override fun observeActiveWorkout(): Flow<DataResult<WorkoutDetails?>> =
        workoutDao.observeActive()
            .map { record ->
                DataResult.Success(record?.asExternalModel()) as DataResult<WorkoutDetails?>
            }
            .catch {
                emit(DataResult.Failure(AppError.LocalDataUnavailable))
            }

    override fun observeHistory(): Flow<DataResult<List<WorkoutDetails>>> =
        workoutDao.observeHistory()
            .map { records ->
                DataResult.Success(records.map { it.asExternalModel() }) as DataResult<List<WorkoutDetails>>
            }
            .catch {
                emit(DataResult.Failure(AppError.LocalDataUnavailable))
            }

    override suspend fun startRoutine(
        routineId: RoutineId,
    ): DataResult<WorkoutSessionId> = runCatching {
        workoutDao.getActive()?.let { return@runCatching WorkoutSessionId(it.session.id) }
        val routine = requireNotNull(routineDao.getById(routineId.value))
        val timestamp = clock.now().toEpochMilli()
        val sessionId = WorkoutSessionId.create()
        val session = WorkoutSessionEntity(
            id = sessionId.value,
            routineId = routine.routine.id,
            name = routine.routine.name,
            startedAtEpochMillis = timestamp,
            finishedAtEpochMillis = null,
            status = WorkoutSessionStatus.ACTIVE.name,
            notes = "",
            createdAtEpochMillis = timestamp,
            updatedAtEpochMillis = timestamp,
        )
        val sessionExercises = mutableListOf<WorkoutSessionExerciseEntity>()
        val sets = mutableListOf<WorkoutSetEntity>()
        routine.exercises
            .sortedBy { it.item.position }
            .forEachIndexed { position, routineExercise ->
                val sessionExerciseId = SessionExerciseId.create()
                sessionExercises += WorkoutSessionExerciseEntity(
                    id = sessionExerciseId.value,
                    sessionId = sessionId.value,
                    exerciseId = routineExercise.exercise.id,
                    exerciseNameSnapshot = routineExercise.exercise.name,
                    muscleGroupSnapshot = routineExercise.exercise.primaryMuscleGroup,
                    mediaUriSnapshot = routineExercise.exercise.mediaUri,
                    mediaTypeSnapshot = routineExercise.exercise.mediaType,
                    mediaThumbnailUriSnapshot = routineExercise.exercise.mediaThumbnailUri,
                    position = position,
                    notes = routineExercise.item.notes,
                )
                repeat(routineExercise.item.plannedSets.coerceIn(1, 12)) { setPosition ->
                    sets += WorkoutSetEntity(
                        id = WorkoutSetId.create().value,
                        sessionExerciseId = sessionExerciseId.value,
                        position = setPosition,
                        setType = WorkoutSetType.NORMAL.name,
                        weightGrams = 0,
                        repetitions = 0,
                        rpe = null,
                        isCompleted = false,
                        completedAtEpochMillis = null,
                        createdAtEpochMillis = timestamp,
                        updatedAtEpochMillis = timestamp,
                    )
                }
            }
        workoutDao.replaceActive(timestamp, session, sessionExercises, sets)
        sessionId
    }.fold(
        onSuccess = { DataResult.Success(it) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun updateSet(
        setId: WorkoutSetId,
        weight: Weight,
        repetitions: Repetitions,
        completed: Boolean,
    ): DataResult<Unit> = runCatching {
        requireNotNull(workoutDao.getSet(setId.value))
        val now = clock.now().toEpochMilli()
        workoutDao.updateSet(
            setId = setId.value,
            weightGrams = weight.grams,
            repetitions = repetitions.count,
            isCompleted = completed,
            completedAt = if (completed) now else null,
            updatedAt = now,
        )
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun addSet(
        sessionExerciseId: SessionExerciseId,
    ): DataResult<Unit> = runCatching {
        val previous = workoutDao.getLastSet(sessionExerciseId.value)
        val timestamp = clock.now().toEpochMilli()
        workoutDao.insertSet(
            WorkoutSetEntity(
                id = WorkoutSetId.create().value,
                sessionExerciseId = sessionExerciseId.value,
                position = workoutDao.getLastSetPosition(sessionExerciseId.value) + 1,
                setType = WorkoutSetType.NORMAL.name,
                weightGrams = previous?.weightGrams ?: 0,
                repetitions = previous?.repetitions ?: 0,
                rpe = null,
                isCompleted = false,
                completedAtEpochMillis = null,
                createdAtEpochMillis = timestamp,
                updatedAtEpochMillis = timestamp,
            ),
        )
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun updateSetType(
        setId: WorkoutSetId,
        type: WorkoutSetType,
    ): DataResult<Unit> = runCatching {
        requireNotNull(workoutDao.getSet(setId.value))
        workoutDao.updateSetType(
            setId = setId.value,
            setType = type.name,
            updatedAt = clock.now().toEpochMilli(),
        )
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun deleteSet(setId: WorkoutSetId): DataResult<Unit> = runCatching {
        requireNotNull(workoutDao.getSet(setId.value))
        workoutDao.deleteSet(setId.value)
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun finishWorkout(
        sessionId: WorkoutSessionId,
        location: WorkoutLocation?,
    ): DataResult<Unit> = runCatching {
        val activeWorkout = requireNotNull(workoutDao.getActive()).asExternalModel()
        check(activeWorkout.session.id == sessionId)
        check(activeWorkout.completedSetCount > 0)
        check(
            workoutDao.finish(
                sessionId = sessionId.value,
                timestamp = clock.now().toEpochMilli(),
                locationLatitude = location?.latitude,
                locationLongitude = location?.longitude,
                locationAccuracyMeters = location?.accuracyMeters,
                locationCapturedAt = location?.capturedAt?.toEpochMilli(),
                locationLabel = location?.label,
            ) > 0,
        )
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun deleteCompletedWorkout(
        sessionId: WorkoutSessionId,
    ): DataResult<Unit> = runCatching {
        check(workoutDao.deleteCompletedWorkout(sessionId.value) > 0)
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun discardActiveWorkout(): DataResult<Unit> = runCatching {
        workoutDao.discardActive(clock.now().toEpochMilli())
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )
}
