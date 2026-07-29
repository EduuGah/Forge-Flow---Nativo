package com.forgeflow.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign

@Composable
fun ForgeFlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
) {
    Button(
        modifier = modifier.heightIn(min = 48.dp),
        onClick = onClick,
        enabled = enabled,
        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            contentColor = androidx.compose.ui.graphics.Color.White,
        ),
    ) {
        ButtonContent(text, icon, iconContentDescription)
    }
}

@Composable
fun ForgeFlowOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
) {
    OutlinedButton(
        modifier = modifier.heightIn(min = 48.dp),
        onClick = onClick,
        enabled = enabled,
        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
    ) {
        ButtonContent(text, icon, iconContentDescription)
    }
}

@Composable
private fun ButtonContent(
    text: String,
    icon: ImageVector?,
    iconContentDescription: String?,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = iconContentDescription,
            )
        }
        Text(text = text)
    }
}
