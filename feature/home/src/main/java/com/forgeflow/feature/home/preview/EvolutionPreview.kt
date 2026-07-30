package com.forgeflow.feature.home.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.forgeflow.core.designsystem.theme.ForgeFlowTheme
import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.feature.home.presentation.EvolutionChartPointUiModel
import com.forgeflow.feature.home.presentation.EvolutionExerciseUiModel
import com.forgeflow.feature.home.presentation.EvolutionFrequencyUiModel
import com.forgeflow.feature.home.presentation.EvolutionMuscleUiModel
import com.forgeflow.feature.home.presentation.EvolutionScreen
import com.forgeflow.feature.home.presentation.EvolutionUiState

@Preview(showBackground = true)
@Composable
private fun EvolutionScreenPreview() {
    ForgeFlowTheme {
        EvolutionScreen(
            state = EvolutionUiState(
                isLoading = false,
                weightUnit = WeightUnit.KILOGRAM,
                workoutCount = 12,
                completedSets = 146,
                totalVolume = 48_320.0,
                durationMinutes = 720,
                personalRecordCount = 6,
                workoutChangePercent = 20,
                volumeChangePercent = 14,
                averageWorkoutsPerWeek = 2.8,
                averageDurationMinutes = 60,
                activeDays = 12,
                volumeChart = listOf(
                    EvolutionChartPointUiModel("01/07", 6_200.0),
                    EvolutionChartPointUiModel("06/07", 8_450.0),
                    EvolutionChartPointUiModel("11/07", 7_900.0),
                    EvolutionChartPointUiModel("16/07", 9_300.0),
                    EvolutionChartPointUiModel("21/07", 8_100.0),
                    EvolutionChartPointUiModel("26/07", 8_370.0),
                ),
                frequency = listOf("SEG", "TER", "QUA", "QUI", "SEX", "SÁB", "DOM")
                    .mapIndexed { index, label ->
                        EvolutionFrequencyUiModel(
                            label = label,
                            workoutCount = listOf(3, 1, 2, 1, 3, 2, 0)[index],
                            share = listOf(1f, .33f, .66f, .33f, 1f, .66f, 0f)[index],
                        )
                    },
                muscleDistribution = listOf(
                    EvolutionMuscleUiModel(MuscleGroup.CHEST, 38, .31f),
                    EvolutionMuscleUiModel(MuscleGroup.BACK, 34, .28f),
                    EvolutionMuscleUiModel(MuscleGroup.QUADRICEPS, 26, .21f),
                ),
                topExercises = listOf(
                    EvolutionExerciseUiModel("1", "Supino reto", 6, 22, 12_400.0),
                    EvolutionExerciseUiModel("2", "Remada curvada", 5, 19, 10_850.0),
                    EvolutionExerciseUiModel("3", "Agachamento livre", 4, 16, 14_200.0),
                ),
            ),
            onAction = {},
            onBack = {},
            onOpenExercise = {},
        )
    }
}
