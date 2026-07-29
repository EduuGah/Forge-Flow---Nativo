package com.forgeflow.core.database.exercise

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises ORDER BY name COLLATE NOCASE")
    suspend fun getAll(): List<ExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(exercises: List<ExerciseEntity>): List<Long>

    @Update
    suspend fun updateAll(exercises: List<ExerciseEntity>)

    @Transaction
    suspend fun seedBuiltIns(exercises: List<ExerciseEntity>): List<Long> {
        val insertedRows = insertAll(exercises)
        updateAll(exercises)
        return insertedRows
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(exercise: ExerciseEntity)

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ExerciseEntity?

    @Query("DELETE FROM exercises WHERE id = :id AND is_custom = 1")
    suspend fun deleteCustom(id: String): Int

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int
}
