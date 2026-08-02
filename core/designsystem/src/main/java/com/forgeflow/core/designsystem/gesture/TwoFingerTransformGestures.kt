package com.forgeflow.core.designsystem.gesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope

suspend fun PointerInputScope.detectTwoFingerTransformGestures(
    onGesture: (
        centroid: Offset,
        pan: Offset,
        zoom: Float,
        rotation: Float,
    ) -> Unit,
) {
    awaitEachGesture {
        do {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val activePointers = event.changes.count { it.pressed }
            if (activePointers >= 2) {
                onGesture(
                    event.calculateCentroid(useCurrent = true),
                    event.calculatePan(),
                    event.calculateZoom(),
                    event.calculateRotation(),
                )
                event.changes.forEach { it.consume() }
            }
        } while (event.changes.any { it.pressed })
    }
}
