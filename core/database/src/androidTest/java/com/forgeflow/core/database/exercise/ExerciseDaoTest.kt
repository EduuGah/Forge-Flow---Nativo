package com.forgeflow.core.database.exercise

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.forgeflow.core.database.ForgeFlowDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExerciseDaoTest {
    private lateinit var database: ForgeFlowDatabase
    private lateinit var dao: ExerciseDao

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            ForgeFlowDatabase::class.java,
        ).build()
        dao = database.exerciseDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun observeAll_returnsExercisesOrderedByName() = runTest {
        dao.insertAll(
            listOf(
                entity(id = "2", name = "Remada com barra"),
                entity(id = "1", name = "Agachamento livre"),
            ),
        )

        val names = dao.observeAll().first().map(ExerciseEntity::name)

        assertEquals(listOf("Agachamento livre", "Remada com barra"), names)
    }

    private fun entity(id: String, name: String) = ExerciseEntity(
        id = id,
        name = name,
        primaryMuscleGroup = "BACK",
        secondaryMuscleGroups = "",
        equipment = "BARBELL",
        instructions = "",
        isCustom = false,
        createdAtEpochMillis = 0,
        updatedAtEpochMillis = 0,
    )
}
