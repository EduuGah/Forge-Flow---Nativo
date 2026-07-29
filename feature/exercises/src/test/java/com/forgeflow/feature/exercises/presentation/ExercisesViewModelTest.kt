package com.forgeflow.feature.exercises.presentation

import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.core.testing.FakeExerciseRepository
import com.forgeflow.core.testing.MainDispatcherRule
import com.forgeflow.core.testing.exerciseTestData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExercisesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun searchChanged_filtersExercisesReactively() = runTest {
        val repository = FakeExerciseRepository(
            exercises = listOf(
                exerciseTestData(),
                exerciseTestData(
                    id = "42e026cb-c0a2-442c-8d46-a5c2ae5e4293",
                    name = "Agachamento livre",
                    primaryMuscleGroup = MuscleGroup.QUADRICEPS,
                ),
            ),
        )
        val viewModel = ExercisesViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        viewModel.onAction(ExercisesAction.SearchChanged("agacha"))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("agacha", viewModel.uiState.value.query)
        assertEquals(
            listOf("Agachamento livre"),
            viewModel.uiState.value.exercises.map(ExerciseUiModel::name),
        )
    }
}
