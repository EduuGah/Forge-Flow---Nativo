package com.forgeflow.feature.home.presentation

import kotlin.math.roundToInt

internal fun percentageChange(current: Double, previous: Double): Int? {
    if (previous == 0.0) return null
    return ((current - previous) / previous * 100.0).roundToInt()
}

internal fun nearestEvolutionChartPointIndex(
    tapX: Float,
    chartWidth: Float,
    pointCount: Int,
): Int? {
    if (pointCount <= 0 || chartWidth <= 0f) return null
    if (pointCount == 1) return 0
    return (tapX / chartWidth * (pointCount - 1))
        .roundToInt()
        .coerceIn(0, pointCount - 1)
}
