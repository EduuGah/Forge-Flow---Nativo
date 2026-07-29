package com.forgeflow.core.data.exercise

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.common.time.AppClock
import com.forgeflow.core.database.ForgeFlowDatabase
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExerciseSeedTest {
    private lateinit var database: ForgeFlowDatabase
    private lateinit var repository: DefaultExerciseRepository

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            ForgeFlowDatabase::class.java,
        ).build()
        repository = DefaultExerciseRepository(
            localDataSource = RoomExerciseLocalDataSource(database.exerciseDao()),
            clock = FixedClock,
        )
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun seedDefaults_doesNotDuplicateExercises() = runTest {
        val firstResult = repository.seedDefaults()
        val secondResult = repository.seedDefaults()

        assertTrue(firstResult is DataResult.Success)
        assertTrue(secondResult is DataResult.Success)
        assertEquals(5, (firstResult as DataResult.Success).value)
        assertEquals(0, (secondResult as DataResult.Success).value)
        assertEquals(5, database.exerciseDao().count())
    }

    private object FixedClock : AppClock {
        override fun now(): Instant = Instant.parse("2026-01-01T00:00:00Z")
    }
}
