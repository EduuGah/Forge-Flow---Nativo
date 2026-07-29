package com.forgeflow.core.data.exercise

import com.forgeflow.core.database.exercise.ExerciseDao
import com.forgeflow.core.database.exercise.ExerciseEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class RoomExerciseLocalDataSource @Inject constructor(
    private val exerciseDao: ExerciseDao,
) : ExerciseLocalDataSource {
    override fun observeExercises(): Flow<List<ExerciseEntity>> = exerciseDao.observeAll()

    override suspend fun insertExercises(exercises: List<ExerciseEntity>): Int =
        exerciseDao.insertAll(exercises).count { rowId -> rowId != INSERT_IGNORED }

    override suspend fun getExercise(id: String): ExerciseEntity? = exerciseDao.getById(id)

    override suspend fun upsertExercise(exercise: ExerciseEntity) = exerciseDao.upsert(exercise)

    override suspend fun deleteCustomExercise(id: String): Boolean =
        exerciseDao.deleteCustom(id) > 0

    private companion object {
        const val INSERT_IGNORED = -1L
    }
}
