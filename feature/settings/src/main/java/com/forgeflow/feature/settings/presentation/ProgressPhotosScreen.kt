package com.forgeflow.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
import java.time.temporal.ChronoUnit

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
    val cutoff = filter.days?.let { days ->
        Instant.now().minus(days, ChronoUnit.DAYS).toEpochMilli()
    }
    val photos = state.profile.photos
        .asReversed()
        .filter { photo -> cutoff == null || photo.capturedAtEpochMillis >= cutoff }
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
                    onFilterSelected = { filter = it },
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
                        onClick = {
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
}

@Composable
private fun PhotoGalleryControls(
    selectedFilter: ProgressPhotoDateFilter,
    selectedCount: Int,
    onFilterSelected: (ProgressPhotoDateFilter) -> Unit,
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
                    onClick = { onFilterSelected(item) },
                    label = { Text(stringResource(item.labelResource)) },
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
    onDelete: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    photos.forEachIndexed { index, photo ->
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = stringResource(
                                    if (index == 0) {
                                        R.string.progress_photos_before
                                    } else {
                                        R.string.progress_photos_after
                                    },
                                ).uppercase(),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.labelSmall,
                            )
                            AsyncImage(
                                model = File(photo.filePath),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.72f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentScale = ContentScale.Crop,
                            )
                            Text(
                                text = photo.dateLabel,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
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
}
