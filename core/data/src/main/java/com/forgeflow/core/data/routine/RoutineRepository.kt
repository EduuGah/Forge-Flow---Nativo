package com.forgeflow.core.data.routine

import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.model.RoutineDetails
import com.forgeflow.core.model.RoutineDraft
import com.forgeflow.core.model.RoutineFolder
import com.forgeflow.core.model.RoutineFolderId
import com.forgeflow.core.model.RoutineId
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    fun observeRoutines(): Flow<DataResult<List<RoutineDetails>>>

    fun observeFolders(): Flow<DataResult<List<RoutineFolder>>>

    suspend fun getRoutine(id: RoutineId): DataResult<RoutineDetails>

    suspend fun saveRoutine(draft: RoutineDraft): DataResult<RoutineId>

    suspend fun archiveRoutine(id: RoutineId): DataResult<Unit>

    suspend fun saveFolder(id: RoutineFolderId?, name: String): DataResult<RoutineFolderId>

    suspend fun deleteFolder(id: RoutineFolderId): DataResult<Unit>

    suspend fun copyRoutine(id: RoutineId, targetFolderId: RoutineFolderId? = null): DataResult<RoutineId>

    suspend fun copyFolder(id: RoutineFolderId): DataResult<RoutineFolderId>

    suspend fun reorderFolders(orderedIds: List<RoutineFolderId>): DataResult<Unit>

    suspend fun reorderRoutines(
        folderId: RoutineFolderId?,
        orderedIds: List<RoutineId>,
    ): DataResult<Unit>
}
