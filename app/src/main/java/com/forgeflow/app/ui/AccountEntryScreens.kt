package com.forgeflow.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.forgeflow.app.R
import com.forgeflow.core.designsystem.component.ForgeFlowButton
import com.forgeflow.core.designsystem.component.ForgeFlowOutlinedButton
import com.forgeflow.core.designsystem.component.ForgeFlowTextField
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.ExperienceLevel
import com.forgeflow.core.model.TrainingGoal
import com.forgeflow.core.model.WeightUnit

internal enum class AccountEntryMode {
    SIGN_IN,
    CREATE_ACCOUNT,
}

@Composable
internal fun ForgeFlowLoadingScreen(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AccountEntryScreen(
    state: AppAuthUiState,
    onGoogleSignIn: () -> Unit,
    onEmailSignIn: (String, String) -> Unit,
    onCreateAccount: (String, String) -> Unit,
    onPasswordReset: (String) -> Unit,
    onDismissFeedback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var mode by rememberSaveable { mutableStateOf(AccountEntryMode.SIGN_IN) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmationVisible by rememberSaveable { mutableStateOf(false) }
    var validationFailed by rememberSaveable { mutableStateOf(false) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(mode) {
        validationFailed = false
        password = ""
        confirmation = ""
        onDismissFeedback()
    }
    LaunchedEffect(state.notice) {
        if (state.notice == AppAuthNotice.PASSWORD_RESET_SENT) {
            showResetDialog = false
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            item { AccountEntryHeader() }
            item {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    AccountEntryMode.entries.forEachIndexed { index, entryMode ->
                        SegmentedButton(
                            selected = mode == entryMode,
                            onClick = { mode = entryMode },
                            shape = SegmentedButtonDefaults.itemShape(
                                index,
                                AccountEntryMode.entries.size,
                            ),
                            label = {
                                Text(
                                    stringResource(
                                        if (entryMode == AccountEntryMode.SIGN_IN) {
                                            R.string.auth_mode_sign_in
                                        } else {
                                            R.string.auth_mode_create
                                        },
                                    ),
                                )
                            },
                        )
                    }
                }
            }
            item {
                ForgeFlowOutlinedButton(
                    text = stringResource(R.string.auth_google_action),
                    onClick = onGoogleSignIn,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.isConfigured && !state.isWorking,
                    icon = Icons.Outlined.AccountCircle,
                    iconContentDescription = null,
                )
            }
            item { AuthDivider() }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ForgeFlowTextField(
                        value = email,
                        onValueChange = {
                            email = it.take(160)
                            validationFailed = false
                            onDismissFeedback()
                        },
                        label = stringResource(R.string.auth_email),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Outlined.AlternateEmail, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                    PasswordField(
                        value = password,
                        onValueChange = {
                            password = it.take(72)
                            validationFailed = false
                            onDismissFeedback()
                        },
                        visible = passwordVisible,
                        onVisibilityChanged = { passwordVisible = !passwordVisible },
                        label = stringResource(R.string.auth_password),
                    )
                    if (mode == AccountEntryMode.CREATE_ACCOUNT) {
                        PasswordField(
                            value = confirmation,
                            onValueChange = {
                                confirmation = it.take(72)
                                validationFailed = false
                                onDismissFeedback()
                            },
                            visible = confirmationVisible,
                            onVisibilityChanged = {
                                confirmationVisible = !confirmationVisible
                            },
                            label = stringResource(R.string.auth_password_confirmation),
                        )
                    }
                    if (mode == AccountEntryMode.SIGN_IN) {
                        TextButton(
                            onClick = { showResetDialog = true },
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text(stringResource(R.string.auth_forgot_password))
                        }
                    }
                    if (!state.isConfigured) {
                        AuthMessage(
                            text = stringResource(R.string.auth_not_configured),
                            isError = true,
                        )
                    }
                    if (validationFailed) {
                        AuthMessage(
                            text = stringResource(R.string.auth_validation_error),
                            isError = true,
                        )
                    }
                    if (state.operationFailed) {
                        AuthMessage(
                            text = stringResource(R.string.auth_operation_error),
                            isError = true,
                        )
                    }
                    if (state.notice == AppAuthNotice.PASSWORD_RESET_SENT) {
                        AuthMessage(
                            text = stringResource(R.string.auth_reset_sent),
                            isError = false,
                        )
                    }
                    ForgeFlowButton(
                        text = stringResource(
                            if (mode == AccountEntryMode.SIGN_IN) {
                                R.string.auth_sign_in_action
                            } else {
                                R.string.auth_create_action
                            },
                        ),
                        onClick = {
                            if (!isValidAuthForm(mode, email, password, confirmation)) {
                                validationFailed = true
                            } else if (mode == AccountEntryMode.SIGN_IN) {
                                onEmailSignIn(email.trim(), password)
                            } else {
                                onCreateAccount(email.trim(), password)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.isConfigured && !state.isWorking,
                        icon = if (state.isWorking) null else Icons.Outlined.CheckCircle,
                        iconContentDescription = null,
                    )
                    if (state.isWorking) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.CenterHorizontally),
                            strokeWidth = 2.dp,
                        )
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        PasswordResetDialog(
            initialEmail = email,
            isWorking = state.isWorking,
            operationFailed = state.operationFailed,
            onDismiss = { showResetDialog = false },
            onSubmit = { resetEmail ->
                email = resetEmail
                onPasswordReset(resetEmail)
            },
        )
    }
}

@Composable
private fun AccountEntryHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                color = Color(0xFF090A0C),
                shape = RoundedCornerShape(14.dp),
            ) {
                Image(
                    painter = painterResource(R.mipmap.ic_launcher_foreground),
                    contentDescription = stringResource(R.string.auth_app_icon),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(3.dp),
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.auth_brand),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = stringResource(R.string.auth_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }
        Text(
            text = stringResource(R.string.auth_description),
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onVisibilityChanged: () -> Unit,
    label: String,
) {
    ForgeFlowTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onVisibilityChanged) {
                Icon(
                    imageVector = if (visible) {
                        Icons.Outlined.VisibilityOff
                    } else {
                        Icons.Outlined.Visibility
                    },
                    contentDescription = stringResource(
                        if (visible) R.string.auth_hide_password else R.string.auth_show_password,
                    ),
                )
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (visible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
    )
}

@Composable
private fun AuthDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.auth_or),
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.labelSmall,
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun AuthMessage(text: String, isError: Boolean) {
    Text(
        text = text,
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun PasswordResetDialog(
    initialEmail: String,
    isWorking: Boolean,
    operationFailed: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    var email by remember(initialEmail) { mutableStateOf(initialEmail) }
    var validationFailed by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.auth_reset_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.auth_reset_description),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
                ForgeFlowTextField(
                    value = email,
                    onValueChange = {
                        email = it.take(160)
                        validationFailed = false
                    },
                    label = stringResource(R.string.auth_email),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                if (validationFailed) {
                    AuthMessage(stringResource(R.string.auth_invalid_email), true)
                }
                if (operationFailed) {
                    AuthMessage(stringResource(R.string.auth_operation_error), true)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (looksLikeEmail(email)) onSubmit(email.trim())
                    else validationFailed = true
                },
                enabled = !isWorking,
            ) {
                Text(stringResource(R.string.auth_send_reset))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isWorking) {
                Text(stringResource(R.string.auth_cancel))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileSetupScreen(
    state: AppProfileSetupUiState,
    weightUnit: WeightUnit,
    onComplete: (ProfileSetupSubmission) -> Unit,
    modifier: Modifier = Modifier,
) {
    var displayName by rememberSaveable(state.userId, state.displayName) {
        mutableStateOf(state.displayName)
    }
    var birthYear by rememberSaveable(state.userId, state.birthYear) {
        mutableStateOf(state.birthYear?.toString().orEmpty())
    }
    var height by rememberSaveable(state.userId, state.heightCentimeters) {
        mutableStateOf(state.heightCentimeters?.toString().orEmpty())
    }
    var weight by rememberSaveable(state.userId, state.bodyWeight) {
        mutableStateOf(state.bodyWeight?.toInputValue().orEmpty())
    }
    var goal by rememberSaveable(state.userId, state.trainingGoal) {
        mutableStateOf(state.trainingGoal)
    }
    var experience by rememberSaveable(state.userId, state.experienceLevel) {
        mutableStateOf(state.experienceLevel)
    }
    var validationFailed by rememberSaveable(state.userId) { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.profile_setup_eyebrow),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        text = stringResource(R.string.profile_setup_title),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = stringResource(R.string.profile_setup_description),
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    state.email?.let { accountEmail ->
                        Text(
                            text = accountEmail,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ForgeFlowTextField(
                        value = displayName,
                        onValueChange = {
                            displayName = it.take(70)
                            validationFailed = false
                        },
                        label = stringResource(R.string.profile_setup_name),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ForgeFlowTextField(
                            value = birthYear,
                            onValueChange = {
                                birthYear = it.filter(Char::isDigit).take(4)
                                validationFailed = false
                            },
                            label = stringResource(R.string.profile_setup_birth_year),
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                        ForgeFlowTextField(
                            value = height,
                            onValueChange = {
                                height = it.filter(Char::isDigit).take(3)
                                validationFailed = false
                            },
                            label = stringResource(R.string.profile_setup_height),
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                    }
                    ForgeFlowTextField(
                        value = weight,
                        onValueChange = {
                            weight = it.toDecimalInput()
                            validationFailed = false
                        },
                        label = stringResource(
                            R.string.profile_setup_weight,
                            if (weightUnit == WeightUnit.KILOGRAM) "kg" else "lb",
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                }
            }
            item {
                ProfileSetupSelector(
                    title = stringResource(R.string.profile_setup_goal),
                    options = TrainingGoal.entries,
                    selected = goal,
                    label = {
                        stringResource(
                            when (it) {
                                TrainingGoal.STRENGTH -> R.string.profile_setup_goal_strength
                                TrainingGoal.HYPERTROPHY -> R.string.profile_setup_goal_hypertrophy
                                TrainingGoal.GENERAL_FITNESS -> R.string.profile_setup_goal_fitness
                            },
                        )
                    },
                    onSelected = { goal = it },
                )
            }
            item {
                ProfileSetupSelector(
                    title = stringResource(R.string.profile_setup_experience),
                    options = ExperienceLevel.entries,
                    selected = experience,
                    label = {
                        stringResource(
                            when (it) {
                                ExperienceLevel.BEGINNER -> R.string.profile_setup_beginner
                                ExperienceLevel.INTERMEDIATE -> R.string.profile_setup_intermediate
                                ExperienceLevel.ADVANCED -> R.string.profile_setup_advanced
                            },
                        )
                    },
                    onSelected = { experience = it },
                )
            }
            if (validationFailed || state.saveFailed) {
                item {
                    AuthMessage(
                        text = stringResource(
                            if (validationFailed) {
                                R.string.profile_setup_validation
                            } else {
                                R.string.profile_setup_save_error
                            },
                        ),
                        isError = true,
                    )
                }
            }
            item {
                ForgeFlowButton(
                    text = if (state.isSaving) {
                        stringResource(R.string.profile_setup_saving)
                    } else {
                        stringResource(R.string.profile_setup_continue)
                    },
                    onClick = {
                        val submission = ProfileSetupSubmission(
                            displayName = displayName,
                            birthYear = birthYear.toIntOrNull() ?: 0,
                            heightCentimeters = height.toIntOrNull() ?: 0,
                            bodyWeight = weight.replace(',', '.').toDoubleOrNull() ?: 0.0,
                            trainingGoal = goal,
                            experienceLevel = experience,
                        )
                        if (submission.isValid()) onComplete(submission)
                        else validationFailed = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSaving,
                    icon = Icons.Outlined.CheckCircle,
                    iconContentDescription = null,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> ProfileSetupSelector(
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelected: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = selected == option,
                    onClick = { onSelected(option) },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                    label = {
                        Text(
                            text = label(option),
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                )
            }
        }
    }
}

internal fun isValidAuthForm(
    mode: AccountEntryMode,
    email: String,
    password: String,
    confirmation: String,
): Boolean = looksLikeEmail(email) &&
    password.length >= 8 &&
    (mode == AccountEntryMode.SIGN_IN || password == confirmation)

private fun looksLikeEmail(value: String): Boolean {
    val trimmed = value.trim()
    val separator = trimmed.indexOf('@')
    return separator > 0 && separator < trimmed.lastIndex - 2 &&
        '.' in trimmed.substring(separator + 1)
}

private fun String.toDecimalInput(): String {
    val normalized = replace(',', '.')
    val filtered = buildString {
        var separatorAdded = false
        normalized.forEach { character ->
            when {
                character.isDigit() -> append(character)
                character == '.' && !separatorAdded -> {
                    append(character)
                    separatorAdded = true
                }
            }
        }
    }
    return filtered.take(7)
}

private fun Double.toInputValue(): String =
    if (this % 1.0 == 0.0) toInt().toString() else toString().replace('.', ',')
