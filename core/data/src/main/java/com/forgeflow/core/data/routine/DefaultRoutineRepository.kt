package com.forgeflow.core.data.routine

import com.forgeflow.core.common.result.AppError
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.common.time.AppClock
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DefaultRoutineRepository @Inject constructor(
    private val routineDao: RoutineDao,
    private val clock: AppClock,
) : RoutineRepository {
    override fun observeRoutines(): Flow<DataResult<List<RoutineDetails>>> =
        routineDao.observeActive()
            .map { records ->
                DataResult.Success(records.map { it.asExternalModel() }) as DataResult<List<RoutineDetails>>
            }
            .catch {
                emit(DataResult.Failure(AppError.LocalDataUnavailable))
            }

    override fun observeFolders(): Flow<DataResult<List<RoutineFolder>>> =
        routineDao.observeFolders()
            .map { folders ->
                DataResult.Success(folders.map { it.asExternalModel() }) as
                    DataResult<List<RoutineFolder>>
            }
            .catch {
                emit(DataResult.Failure(AppError.LocalDataUnavailable))
            }

    override suspend fun getRoutine(id: RoutineId): DataResult<RoutineDetails> = runCatching {
        requireNotNull(routineDao.getById(id.value)).asExternalModel()
    }.fold(
        onSuccess = { DataResult.Success(it) },
        onFailure = { DataResult.Failure(AppError.LocalDataUnavailable) },
    )

    override suspend fun saveRoutine(draft: RoutineDraft): DataResult<RoutineId> = runCatching {
        require(draft.name.isNotBlank())
        require(draft.exercises.isNotEmpty())
        val id = draft.id ?: RoutineId.create()
        val existing = draft.id?.let { routineDao.getById(it.value) }
        val now = clock.now().toEpochMilli()
        val routine = RoutineEntity(
            id = id.value,
            folderId = draft.folderId?.value,
            name = draft.name.trim(),
            description = draft.description.trim(),
            position = existing?.routine?.position
                ?: routineDao.getLastRoutinePosition(draft.folderId?.value) + 1,
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
        routineDao.archive(id.value, clock.now().toEpochMilli())
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun saveFolder(
        id: RoutineFolderId?,
        name: String,
    ): DataResult<RoutineFolderId> = runCatching {
        require(name.isNotBlank())
        val folderId = id ?: RoutineFolderId.create()
        val existing = id?.let { routineDao.getFolderById(it.value) }
        val now = clock.now().toEpochMilli()
        val position = existing?.position ?: routineDao.getFolderCount()
        routineDao.upsertFolder(
            RoutineFolderEntity(
                id = folderId.value,
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
        routineDao.deleteFolder(id.value)
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
        val source = requireNotNull(routineDao.getFolderById(id.value))
        val target = when (val result = saveFolder(null, "${source.name} - cópia")) {
            is DataResult.Success -> result.value
            is DataResult.Failure -> error("Unable to copy folder")
        }
        routineDao.getActiveInFolder(id.value).forEach { record ->
            check(copyRoutine(RoutineId(record.routine.id), target) is DataResult.Success)
        }
        target
    }.fold(
        onSuccess = { DataResult.Success(it) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun reorderFolders(
        orderedIds: List<RoutineFolderId>,
    ): DataResult<Unit> = runCatching {
        orderedIds.forEachIndexed { position, id ->
            routineDao.updateFolderPosition(id.value, position)
        }
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    override suspend fun reorderRoutines(
        folderId: RoutineFolderId?,
        orderedIds: List<RoutineId>,
    ): DataResult<Unit> = runCatching {
        val allowed = routineDao.getActiveInFolder(folderId?.value).map { it.routine.id }.toSet()
        require(orderedIds.all { it.value in allowed })
        orderedIds.forEachIndexed { position, id ->
            routineDao.updateRoutinePosition(id.value, position)
        }
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )
}
