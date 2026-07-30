package com.forgeflow.feature.exercises.presentation

import kotlin.math.roundToInt

internal fun nearestChartPointIndex(
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
