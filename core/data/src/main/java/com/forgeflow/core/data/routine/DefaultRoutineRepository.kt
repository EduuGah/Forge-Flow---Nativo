package com.forgeflow.core.data.routine

import com.forgeflow.core.common.result.AppError
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.common.time.AppClock
import com.forgeflow.core.data.auth.AuthRepository
import com.forgeflow.core.data.auth.observeCurrentUserId
import com.forgeflow.core.data.auth.requireCurrentUserId
import com.forgeflow.core.database.routine.RoutineDao
import com.forgeflow.core.database.routine.RoutineEntity
import com.forgeflow.core.database.routine.RoutineExerciseEntity
import com.forgeflow.core.database.routine.RoutineFolderEntity
import com.forgeflow.core.database.routine.asExternalModel
import com.forgeflow.core.model.RoutineDetails
import com.forgeflow.core.model.RoutineDraft
import com.forgeflow.core.model.RoutineExerciseId
import com.forgeflow.core.model.RoutineFolder
import com.forgeflow.core.model.RoutineFolderId
import com.forgeflow.core.model.RoutineId
import com.forgeflow.core.model.RoutineExerciseDraft
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultRoutineRepository @Inject constructor(
    private val routineDao: RoutineDao,
    private val clock: AppClock,
    private val authRepository: AuthRepository,
) : RoutineRepository {
    override fun observeRoutines(): Flow<DataResult<List<RoutineDetails>>> =
        authRepository.observeCurrentUserId()
            .flatMapLatest { ownerUserId ->
                if (ownerUserId == null) {
                    flowOf<DataResult<List<RoutineDetails>>>(DataResult.Success(emptyList()))
                } else {
                    flow {
                        routineDao.claimUnownedData(ownerUserId)
                        emitAll(
                            routineDao.observeActive(ownerUserId).map { records ->
                                DataResult.Success(records.map { it.asExternalModel() }) as
                                    DataResult<List<RoutineDetails>>
                            },
                        )
                    }
                }
            }
            .catch {
                emit(DataResult.Failure(AppError.LocalDataUnavailable))
            }

    override fun observeFolders(): Flow<DataResult<List<RoutineFolder>>> =
        authRepository.observeCurrentUserId()
            .flatMapLatest { ownerUserId ->
                if (ownerUserId == null) {
                    flowOf<DataResult<List<RoutineFolder>>>(DataResult.Success(emptyList()))
                } else {
                    flow {
                        routineDao.claimUnownedData(ownerUserId)
                        emitAll(
                            routineDao.observeFolders(ownerUserId).map { folders ->
                                DataResult.Success(folders.map { it.asExternalModel() }) as
                                    DataResult<List<RoutineFolder>>
                            },
                        )
                    }
                }
            }
            .catch {
                emit(DataResult.Failure(AppError.LocalDataUnavailable))
            }

    override suspend fun getRoutine(id: RoutineId): DataResult<RoutineDetails> = runCatching {
        val ownerUserId = authRepository.requireCurrentUserId()
        requireNotNull(routineDao.getById(id.value, ownerUserId)).asExternalModel()
    }.fold(
        onSuccess = { DataResult.Success(it) },
        onFailure = { DataResult.Failure(AppError.LocalDataUnavailable) },
    )

    override suspend fun saveRoutine(draft: RoutineDraft): DataResult<RoutineId> = runCatching {
        require(draft.name.isNotBlank())
        require(draft.exercises.isNotEmpty())
        val id = draft.id ?: RoutineId.create()
        val ownerUserId = authRepository.requireCurrentUserId()
        val existing = draft.id?.let { routineDao.getById(it.value, ownerUserId) }
        val now = clock.now().toEpochMilli()
        val routine = RoutineEntity(
            id = id.value,
            ownerUserId = ownerUserId,
            folderId = draft.folderId?.value,
            name = draft.name.trim(),
            description = draft.description.trim(),
            position = existing?.routine?.position
                ?: routineDao.getLastRoutinePosition(draft.folderId?.value, ownerUserId) + 1,
            compareHistoryWithinFolder = draft.compareHistoryWithinFolder,
            createdAtEpochMillis = existing?.routine?.createdAtEpochMillis ?: now,
            updatedAtEpochMillis = now,
            archivedAtEpochMillis = null,
        )
        val exercises = draft.exercises.mapIndexed { position, item ->
            RoutineExerciseEntity(
                id = RoutineExerciseId.create().value,
                routineId = id.value,
                exerciseId = item.exerciseId.value,
                position = position,
                notes = item.notes.trim(),
                defaultRestSeconds = item.restSeconds.coerceIn(0, 600),
                plannedRepetitionsMinimum = item.plannedRepetitions.minimum,
                plannedRepetitionsMaximum = item.plannedRepetitions.maximum,
                plannedSets = item.plannedSets.coerceIn(1, 12),
                plannedWarmUpSets = item.plannedWarmUpSets.coerceIn(0, 6),
            )
        }
        routineDao.replace(routine, exercises)
        id
    }.fold(
        onSuccess = { DataResult.Success(it) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun archiveRoutine(id: RoutineId): DataResult<Unit> = runCatching {
        routineDao.archive(
            routineId = id.value,
            ownerUserId = authRepository.requireCurrentUserId(),
            archivedAt = clock.now().toEpochMilli(),
        )
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun saveFolder(
        id: RoutineFolderId?,
        name: String,
    ): DataResult<RoutineFolderId> = runCatching {
        require(name.isNotBlank())
        val ownerUserId = authRepository.requireCurrentUserId()
        val folderId = id ?: RoutineFolderId.create()
        val existing = id?.let { routineDao.getFolderById(it.value, ownerUserId) }
        val now = clock.now().toEpochMilli()
        val position = existing?.position ?: routineDao.getFolderCount(ownerUserId)
        routineDao.upsertFolder(
            RoutineFolderEntity(
                id = folderId.value,
                ownerUserId = ownerUserId,
                name = name.trim(),
                position = position,
                createdAtEpochMillis = existing?.createdAtEpochMillis ?: now,
                updatedAtEpochMillis = now,
            ),
        )
        folderId
    }.fold(
        onSuccess = { DataResult.Success(it) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun deleteFolder(id: RoutineFolderId): DataResult<Unit> = runCatching {
        routineDao.deleteFolder(id.value, authRepository.requireCurrentUserId())
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun copyRoutine(
        id: RoutineId,
        targetFolderId: RoutineFolderId?,
    ): DataResult<RoutineId> {
        val source = when (val result = getRoutine(id)) {
            is DataResult.Success -> result.value
            is DataResult.Failure -> return result
        }
        return saveRoutine(
            RoutineDraft(
                folderId = targetFolderId ?: source.routine.folderId,
                name = "${source.routine.name} - cópia",
                description = source.routine.description,
                compareHistoryWithinFolder = source.routine.compareHistoryWithinFolder,
                exercises = source.exercises.map { details ->
                    RoutineExerciseDraft(
                        exerciseId = details.exercise.id,
                        plannedSets = details.routineExercise.plannedSets,
                        plannedWarmUpSets = details.routineExercise.plannedWarmUpSets,
                        plannedRepetitions = details.routineExercise.plannedRepetitions
                            ?: com.forgeflow.core.model.RepetitionRange(8, 12),
                        restSeconds = details.routineExercise.defaultRestSeconds,
                        notes = details.routineExercise.notes,
                    )
                },
            ),
        )
    }

    override suspend fun copyFolder(id: RoutineFolderId): DataResult<RoutineFolderId> = runCatching {
        val ownerUserId = authRepository.requireCurrentUserId()
        val source = requireNotNull(routineDao.getFolderById(id.value, ownerUserId))
        val sourceRoutines = routineDao.getActiveInFolder(id.value, ownerUserId)
        val targetId = RoutineFolderId.create()
        val now = clock.now().toEpochMilli()
        val copiedRoutines = mutableListOf<RoutineEntity>()
        val copiedExercises = mutableListOf<RoutineExerciseEntity>()
        sourceRoutines.forEachIndexed { position, record ->
            val routineId = RoutineId.create()
            copiedRoutines += record.routine.copy(
                id = routineId.value,
                folderId = targetId.value,
                name = "${record.routine.name} - cópia",
                position = position,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
                archivedAtEpochMillis = null,
            )
            record.exercises
                .sortedBy { it.item.position }
                .forEachIndexed { exercisePosition, exercise ->
                    copiedExercises += exercise.item.copy(
                        id = RoutineExerciseId.create().value,
                        routineId = routineId.value,
                        position = exercisePosition,
                    )
                }
        }
        routineDao.insertFolderCopy(
            folder = RoutineFolderEntity(
                id = targetId.value,
                ownerUserId = ownerUserId,
                name = "${source.name} - cópia",
                position = routineDao.getFolderCount(ownerUserId),
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
            routines = copiedRoutines,
            exercises = copiedExercises,
        )
        targetId
    }.fold(
        onSuccess = { DataResult.Success(it) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun reorderFolders(
        orderedIds: List<RoutineFolderId>,
    ): DataResult<Unit> = runCatching {
        val ownerUserId = authRepository.requireCurrentUserId()
        orderedIds.forEachIndexed { position, id ->
            routineDao.updateFolderPosition(id.value, ownerUserId, position)
        }
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun reorderRoutines(
        folderId: RoutineFolderId?,
        orderedIds: List<RoutineId>,
    ): DataResult<Unit> = runCatching {
        val ownerUserId = authRepository.requireCurrentUserId()
        val allowed = routineDao.getActiveInFolder(folderId?.value, ownerUserId)
            .map { it.routine.id }
            .toSet()
        require(orderedIds.all { it.value in allowed })
        orderedIds.forEachIndexed { position, id ->
            routineDao.updateRoutinePosition(id.value, ownerUserId, position)
        }
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )
}
