package com.forgeflow.feature.history.presentation

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal data class TrainingLocationSample(
    val workoutId: String,
    val label: String?,
    val latitude: Double,
    val longitude: Double,
)

internal data class TrainingLocationCluster(
    val id: String,
    val label: String?,
    val latitude: Double,
    val longitude: Double,
    val samples: List<TrainingLocationSample>,
)

internal fun clusterTrainingLocations(
    samples: List<TrainingLocationSample>,
    radiusMeters: Double = DEFAULT_CLUSTER_RADIUS_METERS,
): List<TrainingLocationCluster> {
    val clusters = mutableListOf<MutableTrainingLocationCluster>()
    samples.forEach { sample ->
        val target = clusters.firstOrNull { cluster ->
            cluster.hasLabel(sample.label) ||
                distanceMeters(
                    firstLatitude = cluster.latitude,
                    firstLongitude = cluster.longitude,
                    secondLatitude = sample.latitude,
                    secondLongitude = sample.longitude,
                ) <= radiusMeters
        }
        if (target == null) {
            clusters += MutableTrainingLocationCluster(sample)
        } else {
            target.add(sample)
        }
    }
    return clusters.map(MutableTrainingLocationCluster::toImmutable)
}

private class MutableTrainingLocationCluster(first: TrainingLocationSample) {
    private val items = mutableListOf(first)

    val latitude: Double
        get() = items.map(TrainingLocationSample::latitude).average()

    val longitude: Double
        get() = items.map(TrainingLocationSample::longitude).average()

    fun add(sample: TrainingLocationSample) {
        items += sample
    }

    fun hasLabel(label: String?): Boolean {
        val normalized = label.normalizedLocationLabel() ?: return false
        return items.any { it.label.normalizedLocationLabel() == normalized }
    }

    fun toImmutable(): TrainingLocationCluster {
        val label = items
            .mapNotNull { it.label?.trim()?.takeIf(String::isNotEmpty) }
            .groupingBy(String::lowercase)
            .eachCount()
            .maxByOrNull(Map.Entry<String, Int>::value)
            ?.key
            ?.let { normalized ->
                items.firstNotNullOfOrNull { item ->
                    item.label?.takeIf { it.trim().lowercase() == normalized }
                }
            }
        return TrainingLocationCluster(
            id = items.first().workoutId,
            label = label,
            latitude = latitude,
            longitude = longitude,
            samples = items.toList(),
        )
    }
}

private fun distanceMeters(
    firstLatitude: Double,
    firstLongitude: Double,
    secondLatitude: Double,
    secondLongitude: Double,
): Double {
    val latitudeDelta = (secondLatitude - firstLatitude).toRadians()
    val longitudeDelta = (secondLongitude - firstLongitude).toRadians()
    val firstLatitudeRadians = firstLatitude.toRadians()
    val secondLatitudeRadians = secondLatitude.toRadians()
    val haversine = sin(latitudeDelta / 2) * sin(latitudeDelta / 2) +
        cos(firstLatitudeRadians) * cos(secondLatitudeRadians) *
        sin(longitudeDelta / 2) * sin(longitudeDelta / 2)
    return EARTH_RADIUS_METERS * 2 * asin(sqrt(haversine))
}

private fun Double.toRadians(): Double = this * PI / 180.0

private fun String?.normalizedLocationLabel(): String? = this
    ?.trim()
    ?.takeIf(String::isNotEmpty)
    ?.lowercase()

private const val DEFAULT_CLUSTER_RADIUS_METERS = 20.0
private const val EARTH_RADIUS_METERS = 6_371_000.0
