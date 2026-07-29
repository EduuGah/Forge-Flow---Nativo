package com.forgeflow.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
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
)

@Composable
fun ForgeFlowLocationMap(
    points: List<ForgeFlowMapPoint>,
    modifier: Modifier = Modifier,
) {
    if (points.isEmpty()) return
    val context = LocalContext.current
    val viewport = remember(points) { MapViewport.from(points) }
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
            .background(Color(0xFFE8ECEF)),
    ) {
        val mapOffsetX = maxWidth / 2 - tileSize * (1 + centerFractionX.toFloat())
        val mapOffsetY = maxHeight / 2 - tileSize * (1 + centerFractionY.toFloat())
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
            val pinX = mapOffsetX +
                tileSize * (tileX - centerTileX + 1).toFloat() -
                14.dp
            val pinY = mapOffsetY +
                tileSize * (tileY - centerTileY + 1).toFloat() -
                28.dp
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = point.label,
                modifier = Modifier
                    .offset(pinX, pinY)
                    .size(32.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
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
