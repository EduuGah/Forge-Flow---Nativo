package com.forgeflow.feature.settings.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowPageHeader
import com.forgeflow.core.designsystem.component.ForgeFlowScaffold
import com.forgeflow.core.designsystem.component.ForgeFlowTopAppBar
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.SupporterTier
import com.forgeflow.feature.settings.R

@Composable
fun SupportScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ForgeFlowScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ForgeFlowTopAppBar(
                title = stringResource(R.string.support_top_bar),
                onBack = onBack,
                backContentDescription = stringResource(R.string.support_back),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = ForgeFlowDesign.spacing.screenHorizontal,
                top = innerPadding.calculateTopPadding() + ForgeFlowDesign.spacing.medium,
                end = ForgeFlowDesign.spacing.screenHorizontal,
                bottom = innerPadding.calculateBottomPadding() + ForgeFlowDesign.spacing.extraLarge,
            ),
            verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.section),
        ) {
            item {
                ForgeFlowPageHeader(
                    eyebrow = stringResource(R.string.support_eyebrow),
                    title = stringResource(R.string.support_title),
                    description = stringResource(R.string.support_description),
                )
            }
            state.account.supporterTier?.let { tier ->
                item { ActiveSupportTier(tier) }
            }
            item { ProBenefitsCard() }
            item {
                Text(
                    text = stringResource(R.string.support_plans_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            items(SUPPORT_PLANS.size) { index ->
                SupportPlanCard(SUPPORT_PLANS[index])
            }
            item {
                Text(
                    text = stringResource(R.string.support_store_notice),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun ActiveSupportTier(tier: SupporterTier) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.WorkspacePremium,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.account_supporter_status),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = stringResource(tier.labelResource()),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun ProBenefitsCard() {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.account_pro_includes),
            style = MaterialTheme.typography.titleMedium,
        )
        PRO_BENEFITS.forEach { benefit ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(benefit),
                    modifier = Modifier.weight(1f),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun SupportPlanCard(plan: SupportPlan) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        if (plan.recommended) {
            Text(
                text = stringResource(R.string.account_donation_recommended),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(plan.title),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(plan.amount),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        HorizontalDivider(color = ForgeFlowDesign.colors.divider)
        Text(
            text = stringResource(plan.description),
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

private data class SupportPlan(
    @param:StringRes val title: Int,
    @param:StringRes val amount: Int,
    @param:StringRes val description: Int,
    val recommended: Boolean = false,
)

private val SUPPORT_PLANS = listOf(
    SupportPlan(
        title = R.string.account_pro_monthly,
        amount = R.string.account_pro_monthly_amount,
        description = R.string.account_pro_monthly_description,
    ),
    SupportPlan(
        title = R.string.account_pro_yearly,
        amount = R.string.account_pro_yearly_amount,
        description = R.string.account_pro_yearly_description,
        recommended = true,
    ),
    SupportPlan(
        title = R.string.account_pro_lifetime,
        amount = R.string.account_pro_lifetime_amount,
        description = R.string.account_pro_lifetime_description,
    ),
)

private val PRO_BENEFITS = listOf(
    R.string.account_pro_benefit_routines,
    R.string.account_pro_benefit_history,
    R.string.account_pro_benefit_exercises,
    R.string.account_pro_benefit_measurements,
    R.string.account_pro_benefit_cloud,
    R.string.account_supporter_benefit_no_ads,
    R.string.account_pro_benefit_development,
)

private fun SupporterTier.labelResource(): Int = when (this) {
    SupporterTier.ADMIN -> R.string.account_supporter_admin
    SupporterTier.SUPPORTER -> R.string.account_supporter
    SupporterTier.PRO -> R.string.account_supporter_pro
    SupporterTier.FOUNDER -> R.string.account_supporter_founder
    SupporterTier.LIFETIME -> R.string.account_supporter_lifetime
}
