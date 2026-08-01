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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import com.forgeflow.core.designsystem.R
import kotlin.math.PI
import kotlin.math.ceil
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
    val density = LocalDensity.current
    val initialViewport = remember(points) { MapViewport.from(points) }
    var mapZoom by remember(initialViewport) { mutableIntStateOf(initialViewport.zoom) }
    var centerTileX by remember(initialViewport) {
        mutableDoubleStateOf(initialViewport.centerX)
    }
    var centerTileY by remember(initialViewport) {
        mutableDoubleStateOf(initialViewport.centerY)
    }
    var visualScale by remember(initialViewport) { mutableFloatStateOf(1f) }
    val tileSize = 192.dp
    val tileSizePx = with(density) { tileSize.toPx() }
    val headers = remember {
        NetworkHeaders.Builder()
            .set("User-Agent", "ForgeFlow/1.0 (Android; com.forgeflow.app)")
            .build()
    }

    fun resetCamera() {
        mapZoom = initialViewport.zoom
        centerTileX = initialViewport.centerX
        centerTileY = initialViewport.centerY
        visualScale = 1f
    }

    fun changeZoom(targetZoom: Int) {
        val nextZoom = targetZoom.coerceIn(MIN_MAP_ZOOM, MAX_MAP_ZOOM)
        if (nextZoom == mapZoom) return
        val factor = 1 shl kotlin.math.abs(nextZoom - mapZoom)
        if (nextZoom > mapZoom) {
            centerTileX *= factor
            centerTileY *= factor
        } else {
            centerTileX /= factor
            centerTileY /= factor
        }
        mapZoom = nextZoom
        visualScale = 1f
    }

    BoxWithConstraints(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(Color(0xFFE8ECEF))
            .pointerInput(initialViewport) {
                detectTransformGestures { _, pan, zoomChange, _ ->
                    val tileCount = (1 shl mapZoom).toDouble()
                    centerTileX = wrapTileX(
                        centerTileX - pan.x / (tileSizePx * visualScale),
                        tileCount,
                    )
                    centerTileY = (
                        centerTileY - pan.y / (tileSizePx * visualScale)
                        ).coerceIn(0.0, tileCount)
                    val nextScale = visualScale * zoomChange
                    when {
                        nextScale >= ZOOM_IN_THRESHOLD && mapZoom < MAX_MAP_ZOOM -> {
                            centerTileX *= 2.0
                            centerTileY *= 2.0
                            mapZoom += 1
                            visualScale = (nextScale / 2f).coerceAtLeast(MIN_VISUAL_SCALE)
                        }
                        nextScale <= ZOOM_OUT_THRESHOLD && mapZoom > MIN_MAP_ZOOM -> {
                            centerTileX /= 2.0
                            centerTileY /= 2.0
                            mapZoom -= 1
                            visualScale = (nextScale * 2f).coerceAtMost(MAX_VISUAL_SCALE)
                        }
                        else -> visualScale = nextScale.coerceIn(
                            MIN_VISUAL_SCALE,
                            MAX_VISUAL_SCALE,
                        )
                    }
                }
            },
    ) {
        val viewportWidth = maxWidth
        val viewportHeight = maxHeight
        val tileCount = 1 shl mapZoom
        val baseTileX = floor(centerTileX).toInt()
        val baseTileY = floor(centerTileY).toInt()
        val horizontalRadius = ceil(viewportWidth.value / tileSize.value / 2f).toInt() + 2
        val verticalRadius = ceil(viewportHeight.value / tileSize.value / 2f).toInt() + 2

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = visualScale, scaleY = visualScale),
        ) {
            for (rawY in (baseTileY - verticalRadius)..(baseTileY + verticalRadius)) {
                if (rawY !in 0 until tileCount) continue
                for (rawX in (baseTileX - horizontalRadius)..(baseTileX + horizontalRadius)) {
                    val tileX = rawX.floorMod(tileCount)
                    val model = remember(tileX, rawY, mapZoom) {
                        ImageRequest.Builder(context)
                            .data("https://tile.openstreetmap.org/$mapZoom/$tileX/$rawY.png")
                            .httpHeaders(headers)
                            .build()
                    }
                    AsyncImage(
                        model = model,
                        contentDescription = null,
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = (
                                        viewportWidth / 2 +
                                            tileSize * (rawX - centerTileX).toFloat()
                                        ).roundToPx(),
                                    y = (
                                        viewportHeight / 2 +
                                            tileSize * (rawY - centerTileY).toFloat()
                                        ).roundToPx(),
                                )
                            }
                            .size(tileSize),
                        contentScale = ContentScale.FillBounds,
                    )
                }
            }
            points.forEach { point ->
                val pointTileX = longitudeToTileX(point.longitude, mapZoom)
                val pointTileY = latitudeToTileY(point.latitude, mapZoom)
                val horizontalDelta = shortestWrappedDelta(
                    pointTileX - centerTileX,
                    tileCount.toDouble(),
                )
                val pinX = viewportWidth / 2 + tileSize * horizontalDelta.toFloat()
                val pinY = viewportHeight / 2 +
                    tileSize * (pointTileY - centerTileY).toFloat()
                val selected = point.id != null && point.id == selectedPointId
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (pinX - 24.dp).roundToPx(),
                                y = (pinY - 48.dp).roundToPx(),
                            )
                        }
                        .size(48.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(
                            if (selected) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent,
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
                    onClick = { changeZoom(mapZoom - 1) },
                    enabled = mapZoom > MIN_MAP_ZOOM,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Remove,
                        contentDescription = stringResource(R.string.map_zoom_out),
                    )
                }
                IconButton(
                    onClick = { changeZoom(mapZoom + 1) },
                    enabled = mapZoom < MAX_MAP_ZOOM,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.map_zoom_in),
                    )
                }
                IconButton(onClick = ::resetCamera) {
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
                text = "\u00A9 OpenStreetMap contributors",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

private const val MIN_MAP_ZOOM = 2
private const val MAX_MAP_ZOOM = 18
private const val MIN_VISUAL_SCALE = 0.72f
private const val MAX_VISUAL_SCALE = 1.4f
private const val ZOOM_IN_THRESHOLD = 1.35f
private const val ZOOM_OUT_THRESHOLD = 0.76f

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
    return (1.0 - ln(tan(radians) + 1.0 / cos(radians)) / PI) / 2.0 * tiles
}

private fun wrapTileX(value: Double, tileCount: Double): Double =
    ((value % tileCount) + tileCount) % tileCount

private fun shortestWrappedDelta(delta: Double, tileCount: Double): Double = when {
    delta > tileCount / 2 -> delta - tileCount
    delta < -tileCount / 2 -> delta + tileCount
    else -> delta
}

private fun Int.floorMod(modulus: Int): Int = ((this % modulus) + modulus) % modulus

private operator fun Dp.times(value: Int): Dp = this * value.toFloat()
