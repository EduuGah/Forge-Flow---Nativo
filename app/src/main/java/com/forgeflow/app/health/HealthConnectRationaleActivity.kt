package com.forgeflow.app.health

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.forgeflow.app.R
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowPageHeader
import com.forgeflow.core.designsystem.component.ForgeFlowScaffold
import com.forgeflow.core.designsystem.component.ForgeFlowTopAppBar
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.designsystem.theme.ForgeFlowTheme

class HealthConnectRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ForgeFlowTheme(darkTheme = isSystemInDarkTheme()) {
                HealthConnectRationaleScreen(onBack = ::finish)
            }
        }
    }
}

@Composable
private fun HealthConnectRationaleScreen(onBack: () -> Unit) {
    ForgeFlowScaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ForgeFlowTopAppBar(
                title = stringResource(R.string.health_privacy_top_bar),
                onBack = onBack,
                backContentDescription = stringResource(R.string.health_privacy_close),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = ForgeFlowDesign.spacing.screenHorizontal,
                top = innerPadding.calculateTopPadding() + ForgeFlowDesign.spacing.medium,
                end = ForgeFlowDesign.spacing.screenHorizontal,
                bottom = innerPadding.calculateBottomPadding() +
                    ForgeFlowDesign.spacing.extraLarge,
            ),
            verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.section),
        ) {
            item {
                ForgeFlowPageHeader(
                    eyebrow = stringResource(R.string.health_privacy_eyebrow),
                    title = stringResource(R.string.health_privacy_title),
                    description = stringResource(R.string.health_privacy_description),
                )
            }
            item {
                ForgeFlowCard {
                    PrivacyItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                            )
                        },
                        title = stringResource(R.string.health_privacy_write_title),
                        description = stringResource(R.string.health_privacy_write_description),
                    )
                    PrivacyItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.HealthAndSafety,
                                contentDescription = null,
                            )
                        },
                        title = stringResource(R.string.health_privacy_control_title),
                        description = stringResource(R.string.health_privacy_control_description),
                    )
                    PrivacyItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                            )
                        },
                        title = stringResource(R.string.health_privacy_security_title),
                        description = stringResource(R.string.health_privacy_security_description),
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyItem(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
) {
    Row(
        modifier = Modifier.padding(vertical = ForgeFlowDesign.spacing.small),
        horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
        verticalAlignment = Alignment.Top,
    ) {
        icon()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.extraSmall),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = description,
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
