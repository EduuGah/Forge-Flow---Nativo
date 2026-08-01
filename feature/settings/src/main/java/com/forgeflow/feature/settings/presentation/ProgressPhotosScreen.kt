package com.forgeflow.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Compare
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.FilterChip
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.forgeflow.core.designsystem.component.ForgeFlowButton
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowPageHeader
import com.forgeflow.core.designsystem.component.ForgeFlowScaffold
import com.forgeflow.core.designsystem.component.ForgeFlowTopAppBar
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.feature.settings.R
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProgressPhotosScreen(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    onBack: () -> Unit,
    onAddProgressPhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingPhotoDeletion by remember { mutableStateOf<String?>(null) }
    var selectedIds by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var filter by rememberSaveable { mutableStateOf(ProgressPhotoDateFilter.ALL) }
    var showComparison by rememberSaveable { mutableStateOf(false) }
    var openedPhoto by remember { mutableStateOf<ProgressPhotoUiModel?>(null) }
    var showDateRangePicker by rememberSaveable { mutableStateOf(false) }
    var customStartMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var customEndMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    val cutoff = filter.days?.let { days ->
        Instant.now().minus(days, ChronoUnit.DAYS).toEpochMilli()
    }
    val photos = state.profile.photos
        .asReversed()
        .filter { photo ->
            when (filter) {
                ProgressPhotoDateFilter.CUSTOM -> {
                    val start = customStartMillis
                    val end = customEndMillis?.plus(MILLIS_PER_DAY - 1)
                    start != null && end != null && photo.capturedAtEpochMillis in start..end
                }
                else -> cutoff == null || photo.capturedAtEpochMillis >= cutoff
            }
        }
    val selectedPhotos = selectedIds.mapNotNull { id ->
        state.profile.photos.firstOrNull { it.id == id }
    }

    ForgeFlowScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ForgeFlowTopAppBar(
                title = stringResource(R.string.progress_photos_top_bar),
                onBack = onBack,
                backContentDescription = stringResource(R.string.progress_photos_back),
            )
        },
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 148.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = ForgeFlowDesign.spacing.screenHorizontal,
                top = innerPadding.calculateTopPadding() + ForgeFlowDesign.spacing.large,
                end = ForgeFlowDesign.spacing.screenHorizontal,
                bottom = innerPadding.calculateBottomPadding() + ForgeFlowDesign.spacing.extraLarge,
            ),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
            verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ForgeFlowPageHeader(
                    eyebrow = stringResource(R.string.progress_photos_eyebrow),
                    title = stringResource(R.string.progress_photos_page_title),
                    description = stringResource(R.string.progress_photos_gallery_description),
                    modifier = Modifier.padding(bottom = ForgeFlowDesign.spacing.medium),
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                PhotoGalleryControls(
                    selectedFilter = filter,
                    selectedCount = selectedIds.size,
                    customRangeLabel = customStartMillis?.let { start ->
                        customEndMillis?.let { end ->
                            "${start.asShortDate()} – ${end.asShortDate()}"
                        }
                    },
                    onFilterSelected = { filter = it },
                    onSelectCustomRange = { showDateRangePicker = true },
                    onClearSelection = { selectedIds = emptyList() },
                    onCompare = { showComparison = selectedIds.size == 2 },
                    modifier = Modifier.padding(bottom = ForgeFlowDesign.spacing.medium),
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                ForgeFlowButton(
                    text = stringResource(R.string.progress_photos_add),
                    onClick = onAddProgressPhoto,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = ForgeFlowDesign.spacing.medium),
                    icon = Icons.Outlined.AddAPhoto,
                    iconContentDescription = null,
                )
            }
            if (photos.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = stringResource(R.string.progress_photos_gallery_empty_title),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = stringResource(R.string.progress_photos_gallery_empty_message),
                            color = ForgeFlowDesign.colors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else {
                items(photos, key = ProgressPhotoUiModel::id) { photo ->
                    ProgressPhotoGalleryItem(
                        photo = photo,
                        selectionIndex = selectedIds.indexOf(photo.id).takeIf { it >= 0 },
                        onClick = { openedPhoto = photo },
                        onLongClick = {
                            selectedIds = when {
                                photo.id in selectedIds -> selectedIds - photo.id
                                selectedIds.size < 2 -> selectedIds + photo.id
                                else -> selectedIds.drop(1) + photo.id
                            }
                        },
                        onDelete = { pendingPhotoDeletion = photo.id },
                    )
                }
            }
        }
    }

    pendingPhotoDeletion?.let { photoId ->
        DeleteProgressPhotoDialog(
            onConfirm = {
                pendingPhotoDeletion = null
                selectedIds = selectedIds - photoId
                onAction(SettingsAction.DeleteProgressPhoto(photoId))
            },
            onDismiss = { pendingPhotoDeletion = null },
        )
    }
    if (showComparison && selectedPhotos.size == 2) {
        ProgressPhotoComparisonDialog(
            photos = selectedPhotos.sortedBy(ProgressPhotoUiModel::capturedAtEpochMillis),
            onDismiss = { showComparison = false },
        )
    }
    openedPhoto?.let { photo ->
        ProgressPhotoDetailsDialog(
            photo = photo,
            selected = photo.id in selectedIds,
            onToggleSelection = {
                selectedIds = when {
                    photo.id in selectedIds -> selectedIds - photo.id
                    selectedIds.size < 2 -> selectedIds + photo.id
                    else -> selectedIds.drop(1) + photo.id
                }
            },
            onDismiss = { openedPhoto = null },
        )
    }
    if (showDateRangePicker) {
        val rangeState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = customStartMillis,
            initialSelectedEndDateMillis = customEndMillis,
        )
        Dialog(
            onDismissRequest = { showDateRangePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.94f)
                    .padding(horizontal = 12.dp),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
            ) {
                Column {
                    DateRangePicker(
                        state = rangeState,
                        modifier = Modifier.weight(1f),
                        title = {
                            Text(
                                text = stringResource(R.string.progress_photos_exact_period),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.titleLarge,
                            )
                        },
                        headline = {
                            val start = rangeState.selectedStartDateMillis
                            val end = rangeState.selectedEndDateMillis
                            Text(
                                text = when {
                                    start == null -> {
                                        stringResource(R.string.progress_photos_select_start)
                                    }
                                    end == null -> {
                                        stringResource(
                                            R.string.progress_photos_select_end,
                                            start.asShortDate(),
                                        )
                                    }
                                    else -> {
                                        stringResource(
                                            R.string.progress_photos_selected_range,
                                            start.asShortDate(),
                                            end.asShortDate(),
                                        )
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 16.dp),
                                maxLines = 2,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        },
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { showDateRangePicker = false }) {
                            Text(stringResource(R.string.profile_cancel))
                        }
                        TextButton(
                            onClick = {
                                customStartMillis = rangeState.selectedStartDateMillis
                                customEndMillis = rangeState.selectedEndDateMillis
                                    ?: rangeState.selectedStartDateMillis
                                if (customStartMillis != null && customEndMillis != null) {
                                    filter = ProgressPhotoDateFilter.CUSTOM
                                }
                                showDateRangePicker = false
                            },
                            enabled = rangeState.selectedStartDateMillis != null,
                        ) {
                            Text(stringResource(R.string.progress_photos_apply_filter))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoGalleryControls(
    selectedFilter: ProgressPhotoDateFilter,
    selectedCount: Int,
    customRangeLabel: String?,
    onFilterSelected: (ProgressPhotoDateFilter) -> Unit,
    onSelectCustomRange: () -> Unit,
    onClearSelection: () -> Unit,
    onCompare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
    ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small)) {
            items(ProgressPhotoDateFilter.entries.size) { index ->
                val item = ProgressPhotoDateFilter.entries[index]
                FilterChip(
                    selected = selectedFilter == item,
                    onClick = {
                        if (item == ProgressPhotoDateFilter.CUSTOM) {
                            onSelectCustomRange()
                        } else {
                            onFilterSelected(item)
                        }
                    },
                    leadingIcon = if (item == ProgressPhotoDateFilter.CUSTOM) {
                        { Icon(Icons.Outlined.DateRange, contentDescription = null) }
                    } else {
                        null
                    },
                    label = {
                        Text(
                            text = if (
                                item == ProgressPhotoDateFilter.CUSTOM && customRangeLabel != null
                            ) {
                                customRangeLabel
                            } else {
                                stringResource(item.labelResource)
                            },
                        )
                    },
                )
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = if (selectedCount > 0) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (selectedCount > 0) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            shape = RoundedCornerShape(8.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(imageVector = Icons.Outlined.Compare, contentDescription = null)
                Text(
                    text = stringResource(
                        R.string.progress_photos_selection_count,
                        selectedCount,
                    ),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (selectedCount > 0) {
                    IconButton(onClick = onClearSelection, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.progress_photos_clear),
                        )
                    }
                }
                ForgeFlowButton(
                    text = stringResource(R.string.progress_photos_compare),
                    onClick = onCompare,
                    enabled = selectedCount == 2,
                    icon = Icons.Outlined.Compare,
                    iconContentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun ProgressPhotoGalleryItem(
    photo: ProgressPhotoUiModel,
    selectionIndex: Int?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        border = selectionIndex?.let {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        },
    ) {
        Column {
            Box {
                AsyncImage(
                    model = File(photo.filePath),
                    contentDescription = stringResource(
                        R.string.progress_photo_item_description,
                        photo.dateLabel,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.78f)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                )
                selectionIndex?.let { index ->
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .size(32.dp),
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = CircleShape,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                )
                                Text(
                                    text = (index + 1).toString(),
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.62f), CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.progress_photos_delete),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            Text(
                text = photo.dateLabel,
                modifier = Modifier.padding(12.dp),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun ProgressPhotoComparisonDialog(
    photos: List<ProgressPhotoUiModel>,
    onDismiss: () -> Unit,
) {
    var revealFraction by remember { mutableStateOf(0.5f) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(8.dp),
        ) {
            Column(
                modifier = Modifier.padding(ForgeFlowDesign.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.progress_photos_comparison_title),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = stringResource(R.string.progress_photos_comparison_subtitle),
                            color = ForgeFlowDesign.colors.textSecondary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.progress_photos_close),
                        )
                    }
                }
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.72f)
                        .clip(RoundedCornerShape(8.dp))
                        .clipToBounds()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    val comparisonWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
                    val handleHalfWidthPx = with(LocalDensity.current) { 22.dp.roundToPx() }
                    AsyncImage(
                        model = File(photos[1].filePath),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    AsyncImage(
                        model = File(photos[0].filePath),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                val contentScope = this
                                clipRect(right = size.width * revealFraction) {
                                    contentScope.drawContent()
                                }
                            },
                        contentScale = ContentScale.Crop,
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset {
                                IntOffset(
                                    (comparisonWidthPx * revealFraction).roundToInt() -
                                        handleHalfWidthPx,
                                    0,
                                )
                            }
                            .size(width = 44.dp, height = 320.dp)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    revealFraction = (
                                        revealFraction + dragAmount.x / comparisonWidthPx
                                        ).coerceIn(0.05f, 0.95f)
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .size(width = 2.dp, height = 320.dp)
                                .background(MaterialTheme.colorScheme.onSurface),
                        )
                        Surface(
                            modifier = Modifier.size(38.dp),
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = CircleShape,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Compare, contentDescription = null)
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(photos[0].dateLabel, fontWeight = FontWeight.Bold)
                    Text(photos[1].dateLabel, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ProgressPhotoDetailsDialog(
    photo: ProgressPhotoUiModel,
    selected: Boolean,
    onToggleSelection: () -> Unit,
    onDismiss: () -> Unit,
) {
    val file = remember(photo.filePath) { File(photo.filePath) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(8.dp),
        ) {
            Column(
                modifier = Modifier.padding(ForgeFlowDesign.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.progress_photo_details_title),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = photo.dateLabel,
                            color = ForgeFlowDesign.colors.textSecondary,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, contentDescription = null)
                    }
                }
                AsyncImage(
                    model = file,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.78f)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )
                Text(
                    text = stringResource(
                        R.string.progress_photo_file_size,
                        (file.length() / 1024L).coerceAtLeast(1),
                    ),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
                ForgeFlowButton(
                    text = stringResource(
                        if (selected) {
                            R.string.progress_photo_remove_comparison
                        } else {
                            R.string.progress_photo_add_comparison
                        },
                    ),
                    onClick = onToggleSelection,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Outlined.Compare,
                    iconContentDescription = null,
                )
            }
        }
    }
}

private enum class ProgressPhotoDateFilter(
    val days: Long?,
    val labelResource: Int,
) {
    ALL(null, R.string.progress_photos_filter_all),
    THIRTY_DAYS(30, R.string.progress_photos_filter_30_days),
    THREE_MONTHS(90, R.string.progress_photos_filter_3_months),
    ONE_YEAR(365, R.string.progress_photos_filter_one_year),
    CUSTOM(null, R.string.progress_photos_filter_custom),
}

private fun Long.asShortDate(): String = PHOTO_FILTER_FORMATTER.format(
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate(),
)

private val PHOTO_FILTER_FORMATTER = DateTimeFormatter.ofPattern(
    "dd/MM/yyyy",
    Locale.forLanguageTag("pt-BR"),
)

private const val MILLIS_PER_DAY = 86_400_000L
