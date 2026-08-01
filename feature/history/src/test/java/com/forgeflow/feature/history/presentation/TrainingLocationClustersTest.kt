package com.forgeflow.feature.history.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class TrainingLocationClustersTest {
    @Test
    fun nearbyWorkoutCoordinatesBecomeOnePlace() {
        val clusters = clusterTrainingLocations(
            listOf(
                TrainingLocationSample(
                    workoutId = "first",
                    label = "Forge Gym",
                    latitude = -23.5505,
                    longitude = -46.6333,
                ),
                TrainingLocationSample(
                    workoutId = "second",
                    label = "Forge Gym",
                    latitude = -23.5507,
                    longitude = -46.6334,
                ),
            ),
        )

        assertEquals(1, clusters.size)
        assertEquals(2, clusters.single().samples.size)
        assertEquals("Forge Gym", clusters.single().label)
    }

    @Test
    fun distantWorkoutCoordinatesRemainSeparatePlaces() {
        val clusters = clusterTrainingLocations(
            listOf(
                TrainingLocationSample(
                    workoutId = "center",
                    label = "Centro",
                    latitude = -23.5505,
                    longitude = -46.6333,
                ),
                TrainingLocationSample(
                    workoutId = "south",
                    label = "Zona Sul",
                    latitude = -23.6100,
                    longitude = -46.6700,
                ),
            ),
        )

        assertEquals(2, clusters.size)
    }

    @Test
    fun coordinatesWithinTwentyMetersBecomeOnePlaceEvenWithDifferentLabels() {
        val clusters = clusterTrainingLocations(
            listOf(
                TrainingLocationSample("first", "Academia", -23.550500, -46.633300),
                TrainingLocationSample("second", "Unidade Centro", -23.550590, -46.633300),
            ),
        )

        assertEquals(1, clusters.size)
    }

    @Test
    fun equalNamesBecomeOnePlaceEvenWhenCoordinatesDiffer() {
        val clusters = clusterTrainingLocations(
            listOf(
                TrainingLocationSample("first", "Forge Gym", -23.5505, -46.6333),
                TrainingLocationSample("second", " forge gym ", -23.6505, -46.7333),
            ),
        )

        assertEquals(1, clusters.size)
        assertEquals(2, clusters.single().samples.size)
    }
}
