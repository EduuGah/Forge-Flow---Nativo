package com.forgeflow.feature.history.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.RotateRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.feature.history.R
import kotlinx.coroutines.launch

@Composable
internal fun WorkoutStoryEditor(
    workout: HistoryWorkoutUiModel,
    weightUnit: WeightUnit,
    backgroundUri: String?,
    onChoosePhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onShare: (ImageBitmap) -> Unit,
    onDismiss: () -> Unit,
) {
    var theme by remember { mutableStateOf(WorkoutStoryTheme.FORGE) }
    var message by remember { mutableStateOf("") }
    var showDate by remember { mutableStateOf(true) }
    var showLocation by remember { mutableStateOf(workout.hasLocation) }
    var showMetrics by remember { mutableStateOf(true) }
    var showExercises by remember { mutableStateOf(true) }
    var showProgress by remember { mutableStateOf(true) }
    var centered by remember { mutableStateOf(false) }
    var photoFit by remember { mutableStateOf(true) }
    var contentScale by remember { mutableFloatStateOf(1f) }
    var panelOpacity by remember { mutableFloatStateOf(0.9f) }
    var storyAccent by remember { mutableStateOf(StoryAccent.THEME) }
    var photoScale by remember { mutableFloatStateOf(1f) }
    var photoRotation by remember { mutableFloatStateOf(0f) }
    var photoPan by remember { mutableStateOf(Offset.Zero) }
    var contentPan by remember { mutableStateOf(Offset.Zero) }
    var isExporting by remember { mutableStateOf(false) }
    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()

    LaunchedEffect(backgroundUri) {
        photoScale = 1f
        photoRotation = 0f
        photoPan = Offset.Zero
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.story_close),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.story_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(R.string.story_subtitle),
                            color = ForgeFlowDesign.colors.textSecondary,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    TextButton(
                        enabled = !isExporting,
                        onClick = {
                            isExporting = true
                            scope.launch {
                                onShare(graphicsLayer.toImageBitmap())
                                isExporting = false
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = stringResource(R.string.story_share),
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }
                HorizontalDivider(color = ForgeFlowDesign.colors.divider)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    item {
                        WorkoutStoryCanvas(
                            workout = workout,
                            weightUnit = weightUnit,
                            backgroundUri = backgroundUri,
                            theme = theme,
                            message = message,
                            showDate = showDate,
                            showLocation = showLocation,
                            showMetrics = showMetrics,
                            showExercises = showExercises,
                            showProgress = showProgress,
                            centered = centered,
                            photoFit = photoFit,
                            contentScale = contentScale,
                            panelOpacity = panelOpacity,
                            storyAccent = storyAccent,
                            photoScale = photoScale,
                            photoRotation = photoRotation,
                            photoPan = photoPan,
                            contentPan = contentPan,
                            onTransformPhoto = { pan, zoom, rotation ->
                                photoScale = (photoScale * zoom).coerceIn(1f, 5f)
                                photoRotation = normalizeStoryRotation(photoRotation + rotation)
                                photoPan = Offset(
                                    (photoPan.x + pan.x).coerceIn(-420f, 420f),
                                    (photoPan.y + pan.y).coerceIn(-720f, 720f),
                                )
                            },
                            onMoveContent = { drag ->
                                contentPan = Offset(
                                    (contentPan.x + drag.x).coerceIn(-180f, 180f),
                                    (contentPan.y + drag.y).coerceIn(-320f, 320f),
                                )
                            },
                            modifier = Modifier
                                .padding(top = 18.dp, start = 18.dp, end = 18.dp)
                                .widthIn(max = 360.dp)
                                .fillMaxWidth()
                                .aspectRatio(9f / 16f)
                                .clip(RoundedCornerShape(8.dp))
                                .drawWithContent {
                                    graphicsLayer.record {
                                        this@drawWithContent.drawContent()
                                    }
                                    drawLayer(graphicsLayer)
                                },
                        )
                    }
                    item {
                        StoryControls(
                            theme = theme,
                            onThemeChanged = { theme = it },
                            message = message,
                            onMessageChanged = { message = it.take(MAX_STORY_MESSAGE_LENGTH) },
                            hasPhoto = backgroundUri != null,
                            onChoosePhoto = onChoosePhoto,
                            onRemovePhoto = onRemovePhoto,
                            onRotatePhoto = {
                                photoRotation = normalizeStoryRotation(photoRotation + 90f)
                            },
                            onResetPhoto = {
                                photoScale = 1f
                                photoRotation = 0f
                                photoPan = Offset.Zero
                            },
                            photoFit = photoFit,
                            onPhotoFitChanged = { photoFit = it },
                            contentScale = contentScale,
                            onContentScaleChanged = { contentScale = it },
                            panelOpacity = panelOpacity,
                            onPanelOpacityChanged = { panelOpacity = it },
                            storyAccent = storyAccent,
                            onStoryAccentChanged = { storyAccent = it },
                            showDate = showDate,
                            onShowDateChanged = { showDate = it },
                            showLocation = showLocation,
                            onShowLocationChanged = { showLocation = it },
                            locationAvailable = workout.hasLocation,
                            showMetrics = showMetrics,
                            onShowMetricsChanged = { showMetrics = it },
                            showExercises = showExercises,
                            onShowExercisesChanged = { showExercises = it },
                            showProgress = showProgress,
                            onShowProgressChanged = { showProgress = it },
                            centered = centered,
                            onCenteredChanged = { centered = it },
                            onResetContent = { contentPan = Offset.Zero },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun WorkoutStoryCanvas(
    workout: HistoryWorkoutUiModel,
    weightUnit: WeightUnit,
    backgroundUri: String?,
    theme: WorkoutStoryTheme,
    message: String,
    showDate: Boolean,
    showLocation: Boolean,
    showMetrics: Boolean,
    showExercises: Boolean,
    showProgress: Boolean,
    centered: Boolean,
    photoFit: Boolean,
    contentScale: Float,
    panelOpacity: Float,
    storyAccent: StoryAccent,
    photoScale: Float,
    photoRotation: Float,
    photoPan: Offset,
    contentPan: Offset,
    onTransformPhoto: (Offset, Float, Float) -> Unit,
    onMoveContent: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val basePalette = theme.palette()
    val palette = basePalette.copy(
        panel = basePalette.panel.copy(alpha = panelOpacity),
        accent = storyAccent.color ?: basePalette.accent,
    )
    val personalRecords = workout.exercises.sumOf(HistoryExerciseUiModel::personalRecordCount)
    Box(
        modifier = modifier.background(palette.background),
    ) {
        if (backgroundUri != null) {
            AsyncImage(
                model = backgroundUri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = photoScale,
                        scaleY = photoScale,
                        rotationZ = photoRotation,
                        translationX = photoPan.x,
                        translationY = photoPan.y,
                    ),
                contentScale = if (photoFit) ContentScale.Fit else ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(palette.photoScrim)
                    .pointerInput(backgroundUri) {
                        detectTransformGestures { _, pan, zoom, rotation ->
                            onTransformPhoto(pan, zoom, rotation)
                        }
                    },
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "FORGEFLOW",
                    color = palette.accent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = "●  TREINO CONCLUÍDO",
                    color = palette.secondaryText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(
                        translationX = contentPan.x,
                        translationY = contentPan.y,
                        scaleX = contentScale,
                        scaleY = contentScale,
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.panel)
                    .pointerInput(workout.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onMoveContent(dragAmount)
                        }
                    }
                    .padding(18.dp),
                horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = workout.name,
                    modifier = Modifier.fillMaxWidth(),
                    color = palette.text,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 30.sp,
                    textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                if (showDate || (showLocation && workout.hasLocation)) {
                    Text(
                        text = buildList {
                            if (showDate) add("${workout.day} ${workout.month} • ${workout.time}")
                            if (showLocation && workout.hasLocation) {
                                add(workout.locationLabel ?: "Local registrado")
                            }
                        }.joinToString("  |  "),
                        modifier = Modifier.fillMaxWidth(),
                        color = palette.secondaryText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (message.isNotBlank()) {
                    Text(
                        text = message,
                        modifier = Modifier.fillMaxWidth(),
                        color = palette.text,
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                        textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                    )
                }
                if (showMetrics) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        StoryMetric(workout.durationMinutes.asDuration(), "TEMPO", palette)
                        StoryMetric(
                            "${workout.volume.asDisplayValue()} ${weightUnit.symbol}",
                            "VOLUME",
                            palette,
                        )
                        StoryMetric(workout.completedSets.toString(), "SÉRIES", palette)
                    }
                }
                if (showExercises && workout.exercises.isNotEmpty()) {
                    HorizontalDivider(color = palette.divider)
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        workout.exercises.take(MAX_STORY_EXERCISES).forEachIndexed { index, exercise ->
                            Text(
                                text = "%02d  %s  •  %d séries".format(
                                    index + 1,
                                    exercise.name,
                                    exercise.completedSets,
                                ),
                                color = palette.text,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (workout.exercises.size > MAX_STORY_EXERCISES) {
                            Text(
                                text = "+${workout.exercises.size - MAX_STORY_EXERCISES} exercícios",
                                color = palette.secondaryText,
                                fontSize = 9.sp,
                            )
                        }
                    }
                }
                if (showProgress) {
                    HorizontalDivider(color = palette.divider)
                    Text(
                        text = if (personalRecords > 0) {
                            "PROGRESSO  •  $personalRecords ${if (personalRecords == 1) "novo PR" else "novos PRs"}"
                        } else {
                            "PROGRESSO  •  CONSISTÊNCIA REGISTRADA"
                        },
                        color = palette.accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "SEU TREINO. SUA EVOLUÇÃO.",
                    color = palette.secondaryText,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "FORGEFLOW",
                    color = palette.accent,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}

@Composable
private fun StoryMetric(value: String, label: String, palette: StoryPalette) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = value,
            color = palette.text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
        )
        Text(
            text = label,
            color = palette.secondaryText,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun StoryControls(
    theme: WorkoutStoryTheme,
    onThemeChanged: (WorkoutStoryTheme) -> Unit,
    message: String,
    onMessageChanged: (String) -> Unit,
    hasPhoto: Boolean,
    onChoosePhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onRotatePhoto: () -> Unit,
    onResetPhoto: () -> Unit,
    photoFit: Boolean,
    onPhotoFitChanged: (Boolean) -> Unit,
    contentScale: Float,
    onContentScaleChanged: (Float) -> Unit,
    panelOpacity: Float,
    onPanelOpacityChanged: (Float) -> Unit,
    storyAccent: StoryAccent,
    onStoryAccentChanged: (StoryAccent) -> Unit,
    showDate: Boolean,
    onShowDateChanged: (Boolean) -> Unit,
    showLocation: Boolean,
    onShowLocationChanged: (Boolean) -> Unit,
    locationAvailable: Boolean,
    showMetrics: Boolean,
    onShowMetricsChanged: (Boolean) -> Unit,
    showExercises: Boolean,
    onShowExercisesChanged: (Boolean) -> Unit,
    showProgress: Boolean,
    onShowProgressChanged: (Boolean) -> Unit,
    centered: Boolean,
    onCenteredChanged: (Boolean) -> Unit,
    onResetContent: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StoryControlTitle(
            title = stringResource(R.string.story_style),
            description = stringResource(R.string.story_style_description),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WorkoutStoryTheme.entries.forEach { option ->
                FilterChip(
                    selected = option == theme,
                    onClick = { onThemeChanged(option) },
                    label = { Text(stringResource(option.labelResource)) },
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = onChoosePhoto, modifier = Modifier.weight(1f)) {
                Icon(Icons.Outlined.Image, contentDescription = null)
                Text(
                    text = stringResource(
                        if (hasPhoto) R.string.story_change_photo else R.string.story_choose_photo,
                    ),
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
            if (hasPhoto) {
                IconButton(onClick = onRotatePhoto) {
                    Icon(
                        Icons.AutoMirrored.Outlined.RotateRight,
                        contentDescription = stringResource(R.string.story_rotate_photo),
                    )
                }
                IconButton(onClick = onResetPhoto) {
                    Icon(
                        Icons.Outlined.Refresh,
                        contentDescription = stringResource(R.string.story_reset_photo),
                    )
                }
                IconButton(onClick = onRemovePhoto) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = stringResource(R.string.story_remove_photo),
                    )
                }
            }
        }
        if (hasPhoto) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = photoFit,
                    onClick = { onPhotoFitChanged(true) },
                    label = { Text(stringResource(R.string.story_photo_fit)) },
                )
                FilterChip(
                    selected = !photoFit,
                    onClick = { onPhotoFitChanged(false) },
                    label = { Text(stringResource(R.string.story_photo_fill)) },
                )
            }
            Text(
                text = stringResource(R.string.story_photo_gesture_hint),
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        StoryControlTitle(
            title = stringResource(R.string.story_composition),
            description = stringResource(R.string.story_composition_description),
        )
        Text(
            text = stringResource(R.string.story_content_size),
            style = MaterialTheme.typography.labelLarge,
        )
        Slider(
            value = contentScale,
            onValueChange = onContentScaleChanged,
            valueRange = 0.8f..1.18f,
        )
        Text(
            text = stringResource(R.string.story_panel_opacity),
            style = MaterialTheme.typography.labelLarge,
        )
        Slider(
            value = panelOpacity,
            onValueChange = onPanelOpacityChanged,
            valueRange = 0.35f..1f,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StoryAccent.entries.forEach { option ->
                FilterChip(
                    selected = option == storyAccent,
                    onClick = { onStoryAccentChanged(option) },
                    label = { Text(stringResource(option.labelResource)) },
                )
            }
        }
        OutlinedTextField(
            value = message,
            onValueChange = onMessageChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.story_message)) },
            placeholder = { Text(stringResource(R.string.story_message_hint)) },
            minLines = 2,
            maxLines = 4,
            supportingText = {
                Text("${message.length}/$MAX_STORY_MESSAGE_LENGTH")
            },
        )
        StoryControlTitle(
            title = stringResource(R.string.story_information),
            description = stringResource(R.string.story_information_description),
        )
        StorySwitchRow(
            label = stringResource(R.string.story_show_date),
            checked = showDate,
            onCheckedChange = onShowDateChanged,
        )
        StorySwitchRow(
            label = stringResource(R.string.story_show_location),
            checked = showLocation,
            onCheckedChange = onShowLocationChanged,
            enabled = locationAvailable,
        )
        StorySwitchRow(
            label = stringResource(R.string.story_show_metrics),
            checked = showMetrics,
            onCheckedChange = onShowMetricsChanged,
        )
        StorySwitchRow(
            label = stringResource(R.string.story_show_exercises),
            checked = showExercises,
            onCheckedChange = onShowExercisesChanged,
        )
        StorySwitchRow(
            label = stringResource(R.string.story_show_progress),
            checked = showProgress,
            onCheckedChange = onShowProgressChanged,
        )
        StorySwitchRow(
            label = stringResource(R.string.story_center_content),
            checked = centered,
            onCheckedChange = onCenteredChanged,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onResetContent) {
                Text(stringResource(R.string.story_reset_content))
            }
        }
        Text(
            text = stringResource(R.string.story_drag_hint),
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun StoryControlTitle(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        Text(
            text = description,
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun StorySwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                ForgeFlowDesign.colors.textSecondary
            },
            style = MaterialTheme.typography.bodyMedium,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}

private enum class WorkoutStoryTheme(val labelResource: Int) {
    FORGE(R.string.story_theme_forge),
    PULSE(R.string.story_theme_pulse),
    MINIMAL(R.string.story_theme_minimal),
}

private enum class StoryAccent(val labelResource: Int, val color: Color?) {
    THEME(R.string.story_color_theme, null),
    CORAL(R.string.story_color_coral, Color(0xFFFF5A44)),
    GREEN(R.string.story_color_green, Color(0xFF48D59B)),
    BLUE(R.string.story_color_blue, Color(0xFF4BA3FF)),
    PURPLE(R.string.story_color_purple, Color(0xFFA98BFF)),
    WHITE(R.string.story_color_white, Color.White),
}

private data class StoryPalette(
    val background: Color,
    val panel: Color,
    val photoScrim: Color,
    val text: Color,
    val secondaryText: Color,
    val accent: Color,
    val divider: Color,
)

private fun WorkoutStoryTheme.palette(): StoryPalette = when (this) {
    WorkoutStoryTheme.FORGE -> StoryPalette(
        background = Color(0xFF090A0C),
        panel = Color(0xE6121418),
        photoScrim = Color(0x66000000),
        text = Color.White,
        secondaryText = Color(0xFFB5BAC2),
        accent = Color(0xFFFF5A44),
        divider = Color(0x44FFFFFF),
    )
    WorkoutStoryTheme.PULSE -> StoryPalette(
        background = Color(0xFF07110E),
        panel = Color(0xE60D1715),
        photoScrim = Color(0x70020A08),
        text = Color(0xFFF8FAF9),
        secondaryText = Color(0xFFA8BDB6),
        accent = Color(0xFF48D59B),
        divider = Color(0x4448D59B),
    )
    WorkoutStoryTheme.MINIMAL -> StoryPalette(
        background = Color(0xFFF1F3F5),
        panel = Color(0xEBFFFFFF),
        photoScrim = Color(0x44FFFFFF),
        text = Color(0xFF111318),
        secondaryText = Color(0xFF555B65),
        accent = Color(0xFFDB3E31),
        divider = Color(0x33111318),
    )
}

private fun normalizeStoryRotation(value: Float): Float =
    ((value + 180f) % 360f + 360f) % 360f - 180f

private const val MAX_STORY_MESSAGE_LENGTH = 180
private const val MAX_STORY_EXERCISES = 6
