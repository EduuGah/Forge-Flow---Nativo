package com.forgeflow.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.forgeflow.core.designsystem.component.ForgeFlowPageHeader
import com.forgeflow.core.designsystem.component.ForgeFlowScaffold
import com.forgeflow.core.designsystem.component.ForgeFlowTopAppBar
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.feature.settings.R

@Composable
fun ProgressPhotosScreen(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    onBack: () -> Unit,
    onAddProgressPhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingPhotoDeletion by remember { mutableStateOf<String?>(null) }
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = ForgeFlowDesign.spacing.screenHorizontal,
                top = innerPadding.calculateTopPadding() + ForgeFlowDesign.spacing.large,
                end = ForgeFlowDesign.spacing.screenHorizontal,
                bottom = innerPadding.calculateBottomPadding() + ForgeFlowDesign.spacing.extraLarge,
            ),
            verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.section),
        ) {
            item {
                ForgeFlowPageHeader(
                    eyebrow = stringResource(R.string.progress_photos_eyebrow),
                    title = stringResource(R.string.progress_photos_page_title),
                    description = stringResource(R.string.progress_photos_page_description),
                )
            }
            item {
                ProgressPhotosSection(
                    state = state,
                    onAddPhoto = onAddProgressPhoto,
                    onDeletePhoto = { pendingPhotoDeletion = it },
                )
            }
        }
    }
    pendingPhotoDeletion?.let { photoId ->
        DeleteProgressPhotoDialog(
            onConfirm = {
                pendingPhotoDeletion = null
                onAction(SettingsAction.DeleteProgressPhoto(photoId))
            },
            onDismiss = { pendingPhotoDeletion = null },
        )
    }
}
