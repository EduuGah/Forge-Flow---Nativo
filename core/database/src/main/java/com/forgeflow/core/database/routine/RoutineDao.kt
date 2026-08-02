package com.forgeflow.core.database.routine

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query(
        "SELECT * FROM routine_folders WHERE owner_user_id = :ownerUserId " +
            "ORDER BY position, name",
    )
    fun observeFolders(ownerUserId: String): Flow<List<RoutineFolderEntity>>

    @Transaction
    @Query(
        """
        SELECT * FROM routines
        WHERE owner_user_id = :ownerUserId
          AND archived_at_epoch_millis IS NULL
        ORDER BY folder_id, position, updated_at_epoch_millis DESC
        """,
    )
    fun observeActive(ownerUserId: String): Flow<List<RoutineRecord>>

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :id AND owner_user_id = :ownerUserId LIMIT 1")
    suspend fun getById(id: String, ownerUserId: String): RoutineRecord?

    @Transaction
    @Query(
        """
        SELECT * FROM routines
        WHERE owner_user_id = :ownerUserId
          AND archived_at_epoch_millis IS NULL
          AND ((:folderId IS NULL AND folder_id IS NULL) OR folder_id = :folderId)
        ORDER BY position, updated_at_epoch_millis DESC
        """,
    )
    suspend fun getActiveInFolder(
        folderId: String?,
        ownerUserId: String,
    ): List<RoutineRecord>

    @Query(
        """
        SELECT COALESCE(MAX(position), -1) FROM routines
        WHERE owner_user_id = :ownerUserId
          AND archived_at_epoch_millis IS NULL
          AND ((:folderId IS NULL AND folder_id IS NULL) OR folder_id = :folderId)
        """,
    )
    suspend fun getLastRoutinePosition(folderId: String?, ownerUserId: String): Int

    @Query(
        "SELECT * FROM routine_folders WHERE id = :id AND owner_user_id = :ownerUserId LIMIT 1",
    )
    suspend fun getFolderById(id: String, ownerUserId: String): RoutineFolderEntity?

    @Query("SELECT COUNT(*) FROM routine_folders WHERE owner_user_id = :ownerUserId")
    suspend fun getFolderCount(ownerUserId: String): Int

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
        WHERE id = :routineId AND owner_user_id = :ownerUserId
        """,
    )
    suspend fun archive(routineId: String, ownerUserId: String, archivedAt: Long)

    @Query(
        "UPDATE routines SET folder_id = NULL " +
            "WHERE folder_id = :folderId AND owner_user_id = :ownerUserId",
    )
    suspend fun clearFolder(folderId: String, ownerUserId: String)

    @Query(
        "DELETE FROM routine_folders WHERE id = :folderId AND owner_user_id = :ownerUserId",
    )
    suspend fun deleteFolderEntity(folderId: String, ownerUserId: String)

    @Query(
        "UPDATE routine_folders SET position = :position " +
            "WHERE id = :folderId AND owner_user_id = :ownerUserId",
    )
    suspend fun updateFolderPosition(folderId: String, ownerUserId: String, position: Int)

    @Query(
        "UPDATE routines SET position = :position " +
            "WHERE id = :routineId AND owner_user_id = :ownerUserId",
    )
    suspend fun updateRoutinePosition(routineId: String, ownerUserId: String, position: Int)

    @Query("UPDATE routine_folders SET owner_user_id = :ownerUserId WHERE owner_user_id = ''")
    suspend fun claimUnownedFolders(ownerUserId: String)

    @Query("UPDATE routines SET owner_user_id = :ownerUserId WHERE owner_user_id = ''")
    suspend fun claimUnownedRoutines(ownerUserId: String)

    @Query(
        """
        UPDATE routine_exercises SET notes = :notes
        WHERE routine_id = :routineId AND exercise_id = :exerciseId
        """,
    )
    suspend fun updateExerciseNotes(routineId: String, exerciseId: String, notes: String)

    @Transaction
    suspend fun deleteFolder(folderId: String, ownerUserId: String) {
        clearFolder(folderId, ownerUserId)
        deleteFolderEntity(folderId, ownerUserId)
    }

    @Transaction
    suspend fun claimUnownedData(ownerUserId: String) {
        claimUnownedFolders(ownerUserId)
        claimUnownedRoutines(ownerUserId)
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

    @Transaction
    suspend fun insertFolderCopy(
        folder: RoutineFolderEntity,
        routines: List<RoutineEntity>,
        exercises: List<RoutineExerciseEntity>,
    ) {
        upsertFolder(folder)
        routines.forEach { upsertRoutine(it) }
        if (exercises.isNotEmpty()) insertExercises(exercises)
    }
}
