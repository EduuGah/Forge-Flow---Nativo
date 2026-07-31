package com.forgeflow.core.database.routine

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routine_folders ORDER BY position, name")
    fun observeFolders(): Flow<List<RoutineFolderEntity>>

    @Transaction
    @Query(
        """
        SELECT * FROM routines
        WHERE archived_at_epoch_millis IS NULL
        ORDER BY folder_id, position, updated_at_epoch_millis DESC
        """,
    )
    fun observeActive(): Flow<List<RoutineRecord>>

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): RoutineRecord?

    @Transaction
    @Query(
        """
        SELECT * FROM routines
        WHERE archived_at_epoch_millis IS NULL
          AND ((:folderId IS NULL AND folder_id IS NULL) OR folder_id = :folderId)
        ORDER BY position, updated_at_epoch_millis DESC
        """,
    )
    suspend fun getActiveInFolder(folderId: String?): List<RoutineRecord>

    @Query(
        """
        SELECT COALESCE(MAX(position), -1) FROM routines
        WHERE archived_at_epoch_millis IS NULL
          AND ((:folderId IS NULL AND folder_id IS NULL) OR folder_id = :folderId)
        """,
    )
    suspend fun getLastRoutinePosition(folderId: String?): Int

    @Query("SELECT * FROM routine_folders WHERE id = :id LIMIT 1")
    suspend fun getFolderById(id: String): RoutineFolderEntity?

    @Query("SELECT COUNT(*) FROM routine_folders")
    suspend fun getFolderCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFolder(folder: RoutineFolderEntity)

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

    @Query("UPDATE routines SET folder_id = NULL WHERE folder_id = :folderId")
    suspend fun clearFolder(folderId: String)

    @Query("DELETE FROM routine_folders WHERE id = :folderId")
    suspend fun deleteFolderEntity(folderId: String)

    @Query("UPDATE routine_folders SET position = :position WHERE id = :folderId")
    suspend fun updateFolderPosition(folderId: String, position: Int)

    @Query("UPDATE routines SET position = :position WHERE id = :routineId")
    suspend fun updateRoutinePosition(routineId: String, position: Int)

    @Query(
        """
        UPDATE routine_exercises SET notes = :notes
        WHERE routine_id = :routineId AND exercise_id = :exerciseId
        """,
    )
    suspend fun updateExerciseNotes(routineId: String, exerciseId: String, notes: String)

    @Transaction
    suspend fun deleteFolder(folderId: String) {
        clearFolder(folderId)
        deleteFolderEntity(folderId)
    }

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
