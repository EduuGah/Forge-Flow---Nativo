package com.forgeflow.core.data.routine

import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.model.RoutineDetails
import com.forgeflow.core.model.RoutineDraft
import com.forgeflow.core.model.RoutineId
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    fun observeRoutines(): Flow<DataResult<List<RoutineDetails>>>

    suspend fun getRoutine(id: RoutineId): DataResult<RoutineDetails>

    suspend fun saveRoutine(draft: RoutineDraft): DataResult<RoutineId>

    suspend fun archiveRoutine(id: RoutineId): DataResult<Unit>
}
