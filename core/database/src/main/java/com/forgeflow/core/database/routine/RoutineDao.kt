package com.forgeflow.core.database.routine

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Transaction
    @Query(
        """
        SELECT * FROM routines
        WHERE archived_at_epoch_millis IS NULL
        ORDER BY updated_at_epoch_millis DESC
        """,
    )
    fun observeActive(): Flow<List<RoutineRecord>>

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): RoutineRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRoutine(routine: RoutineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<RoutineExerciseEntity>)

    @Query("DELETE FROM routine_exercises WHERE routine_id = :routineId")
    suspend fun deleteExercises(routineId: String)

    @Query(
        """
        UPDATE routines
        SET archived_at_epoch_millis = :archivedAt,
            updated_at_epoch_millis = :archivedAt
        WHERE id = :routineId
        """,
    )
    suspend fun archive(routineId: String, archivedAt: Long)

    @Transaction
    suspend fun replace(
        routine: RoutineEntity,
        exercises: List<RoutineExerciseEntity>,
    ) {
        upsertRoutine(routine)
        deleteExercises(routine.id)
        insertExercises(exercises)
    }
}
