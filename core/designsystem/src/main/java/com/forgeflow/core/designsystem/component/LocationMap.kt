package com.forgeflow.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import com.forgeflow.core.designsystem.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.tan

@Immutable
data class ForgeFlowMapPoint(
    val latitude: Double,
    val longitude: Double,
    val label: String? = null,
    val id: String? = null,
)

@Composable
fun ForgeFlowLocationMap(
    points: List<ForgeFlowMapPoint>,
    modifier: Modifier = Modifier,
    selectedPointId: String? = null,
    onPointClick: ((ForgeFlowMapPoint) -> Unit)? = null,
) {
    if (points.isEmpty()) return
    val context = LocalContext.current
    val viewport = remember(points) { MapViewport.from(points) }
    var gestureScale by remember(viewport) { mutableFloatStateOf(1f) }
    var mapPan by remember(viewport) { mutableStateOf(Offset.Zero) }
    val tileSize = 180.dp
    val centerTileX = floor(viewport.centerX).toInt()
    val centerTileY = floor(viewport.centerY).toInt()
    val centerFractionX = viewport.centerX - floor(viewport.centerX)
    val centerFractionY = viewport.centerY - floor(viewport.centerY)
    val headers = remember {
        NetworkHeaders.Builder()
            .set("User-Agent", "ForgeFlow/1.0 (Android; com.forgeflow.app)")
            .build()
    }

    BoxWithConstraints(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(Color(0xFFE8ECEF))
            .pointerInput(viewport) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val nextScale = (gestureScale * zoom).coerceIn(1f, MAX_GESTURE_SCALE)
                    val horizontalLimit = size.width * (nextScale - 1f) / 2f +
                        tileSize.toPx() * 0.75f
                    val verticalLimit = size.height * (nextScale - 1f) / 2f +
                        tileSize.toPx() * 0.75f
                    gestureScale = nextScale
                    mapPan = Offset(
                        x = (mapPan.x + pan.x).coerceIn(-horizontalLimit, horizontalLimit),
                        y = (mapPan.y + pan.y).coerceIn(-verticalLimit, verticalLimit),
                    )
                }
            },
    ) {
        val mapOffsetX = maxWidth / 2 - tileSize * (1 + centerFractionX.toFloat())
        val mapOffsetY = maxHeight / 2 - tileSize * (1 + centerFractionY.toFloat())
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = gestureScale,
                    scaleY = gestureScale,
                    translationX = mapPan.x,
                    translationY = mapPan.y,
                ),
        ) {
            Box(
                modifier = Modifier
                    .offset(mapOffsetX, mapOffsetY)
                    .size(tileSize * 3),
            ) {
                (-1..1).forEach { deltaY ->
                    (-1..1).forEach { deltaX ->
                        val x = centerTileX + deltaX
                        val y = centerTileY + deltaY
                        val model = remember(x, y, viewport.zoom) {
                            ImageRequest.Builder(context)
                                .data(
                                    "https://tile.openstreetmap.org/" +
                                        "${viewport.zoom}/$x/$y.png",
                                )
                                .httpHeaders(headers)
                                .build()
                        }
                        AsyncImage(
                            model = model,
                            contentDescription = null,
                            modifier = Modifier
                                .offset(
                                    x = tileSize * (deltaX + 1),
                                    y = tileSize * (deltaY + 1),
                                )
                                .size(tileSize),
                            contentScale = ContentScale.FillBounds,
                        )
                    }
                }
            }
            points.forEach { point ->
                val tileX = longitudeToTileX(point.longitude, viewport.zoom)
                val tileY = latitudeToTileY(point.latitude, viewport.zoom)
                val pinX = mapOffsetX + tileSize * (tileX - centerTileX + 1).toFloat()
                val pinY = mapOffsetY + tileSize * (tileY - centerTileY + 1).toFloat()
                val selected = point.id != null && point.id == selectedPointId
                Box(
                    modifier = Modifier
                        .offset(pinX - 24.dp, pinY - 48.dp)
                        .size(48.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                Color.Transparent
                            },
                        )
                        .then(
                            if (onPointClick != null) {
                                Modifier.clickable { onPointClick(point) }
                            } else {
                                Modifier
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = point.label,
                        modifier = Modifier.size(if (selected) 40.dp else 34.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            shape = MaterialTheme.shapes.small,
        ) {
            Row {
                IconButton(
                    onClick = {
                        gestureScale = (gestureScale / MAP_ZOOM_STEP).coerceAtLeast(1f)
                        if (gestureScale == 1f) mapPan = Offset.Zero
                    },
                    enabled = gestureScale > 1f,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Remove,
                        contentDescription = stringResource(R.string.map_zoom_out),
                    )
                }
                IconButton(
                    onClick = {
                        gestureScale = (gestureScale * MAP_ZOOM_STEP)
                            .coerceAtMost(MAX_GESTURE_SCALE)
                    },
                    enabled = gestureScale < MAX_GESTURE_SCALE,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.map_zoom_in),
                    )
                }
                IconButton(
                    onClick = {
                        gestureScale = 1f
                        mapPan = Offset.Zero
                    },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = stringResource(R.string.map_reset),
                    )
                }
            }
        }
        Surface(
            modifier = Modifier.align(Alignment.BottomEnd),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
            shape = MaterialTheme.shapes.extraSmall,
        ) {
            Text(
                text = "© OpenStreetMap contributors",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

private const val MAX_GESTURE_SCALE = 3f
private const val MAP_ZOOM_STEP = 1.35f

private data class MapViewport(
    val zoom: Int,
    val centerX: Double,
    val centerY: Double,
) {
    companion object {
        fun from(points: List<ForgeFlowMapPoint>): MapViewport {
            val minLatitude = points.minOf(ForgeFlowMapPoint::latitude)
            val maxLatitude = points.maxOf(ForgeFlowMapPoint::latitude)
            val minLongitude = points.minOf(ForgeFlowMapPoint::longitude)
            val maxLongitude = points.maxOf(ForgeFlowMapPoint::longitude)
            val span = max(maxLatitude - minLatitude, maxLongitude - minLongitude)
            val zoom = when {
                points.size == 1 -> 15
                span > 30 -> 3
                span > 10 -> 4
                span > 3 -> 6
                span > 1 -> 7
                span > 0.25 -> 9
                span > 0.06 -> 11
                span > 0.015 -> 13
                else -> 15
            }
            val centerLatitude = (minLatitude + maxLatitude) / 2
            val centerLongitude = (minLongitude + maxLongitude) / 2
            return MapViewport(
                zoom = zoom,
                centerX = longitudeToTileX(centerLongitude, zoom),
                centerY = latitudeToTileY(centerLatitude, zoom),
            )
        }
    }
}

private fun longitudeToTileX(longitude: Double, zoom: Int): Double {
    val tiles = (1 shl zoom).toDouble()
    return (longitude + 180.0) / 360.0 * tiles
}

private fun latitudeToTileY(latitude: Double, zoom: Int): Double {
    val tiles = (1 shl zoom).toDouble()
    val radians = latitude.coerceIn(-85.0511, 85.0511) * PI / 180.0
    return (
        1.0 -
            ln(tan(radians) + 1.0 / cos(radians)) / PI
        ) / 2.0 * tiles
}

private operator fun Dp.times(value: Int): Dp = this * value.toFloat()
