package com.forgeflow.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign

@Composable
internal fun GuidedWorkoutTutorial(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var routineName by rememberSaveable { mutableStateOf("Upper demonstrativo") }
    var weight by rememberSaveable { mutableStateOf("40") }
    var repetitions by rememberSaveable { mutableStateOf("10") }
    var includeLocation by rememberSaveable { mutableStateOf(false) }
    val copy = guidedStepCopy(step)

    BackHandler {
        if (step > 0) step -= 1 else onDismiss()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
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
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TREINO GUIADO",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        text = if (step < GUIDED_STEP_COUNT - 1) {
                            "Passo ${step + 1} de ${GUIDED_STEP_COUNT - 1}"
                        } else {
                            "Demonstração concluída"
                        },
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Fechar treino guiado",
                    )
                }
            }
            LinearProgressIndicator(
                progress = { ((step + 1f) / GUIDED_STEP_COUNT).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
            GuidedCoachPanel(
                title = copy.first,
                description = copy.second,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            ) {
                when (step) {
                    0 -> GuidedRoutinesHome(onCreateRoutine = { step = 1 })
                    1 -> GuidedRoutineName(
                        name = routineName,
                        onNameChanged = { routineName = it.take(48) },
                        onSave = { step = 2 },
                    )
                    2 -> GuidedExerciseSelection(onSelect = { step = 3 })
                    3 -> GuidedRoutineReady(
                        routineName = routineName,
                        onStart = { step = 4 },
                    )
                    in 4..6 -> GuidedActiveWorkout(
                        routineName = routineName,
                        step = step,
                        weight = weight,
                        repetitions = repetitions,
                        onWeightChanged = { weight = it.filter(Char::isDigit).take(4) },
                        onRepetitionsChanged = {
                            repetitions = it.filter(Char::isDigit).take(3)
                        },
                        onAddSet = { step = 5 },
                        onCompleteSet = { step = 6 },
                        onFinish = { step = 7 },
                    )
                    7 -> GuidedFinishWorkout(
                        includeLocation = includeLocation,
                        onIncludeLocationChanged = { includeLocation = it },
                        onFinish = { step = 8 },
                    )
                    else -> GuidedTutorialComplete(
                        includedLocation = includeLocation,
                        onDismiss = onDismiss,
                    )
                }
            }
            if (step in 1..7) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { step -= 1 }) {
                        Text("Voltar")
                    }
                    Text(
                        text = "Os dados desta tela são temporários",
                        color = ForgeFlowDesign.colors.textSecondary,
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun GuidedCoachPanel(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = description,
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun GuidedRoutinesHome(onCreateRoutine: () -> Unit) {
    GuidedPage {
        GuidedPageTitle("MEUS TREINOS", "Organize rotinas e pastas por academia ou objetivo.")
        GuidedTarget(
            label = "NOVA ROTINA",
            icon = Icons.Outlined.Add,
            onClick = onCreateRoutine,
        )
        GuidedMutedCard(
            title = "Força",
            description = "Pasta demonstrativa • nenhuma rotina real será criada",
        )
    }
}

@Composable
private fun GuidedRoutineName(
    name: String,
    onNameChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    GuidedPage {
        GuidedPageTitle("CRIAR ROTINA", "Dê um nome fácil de reconhecer no dia do treino.")
        OutlinedTextField(
            value = name,
            onValueChange = onNameChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nome do treino") },
            singleLine = true,
        )
        GuidedTarget(
            label = "SALVAR E ESCOLHER EXERCÍCIOS",
            icon = Icons.Outlined.Check,
            enabled = name.isNotBlank(),
            onClick = onSave,
        )
    }
}

@Composable
private fun GuidedExerciseSelection(onSelect: () -> Unit) {
    GuidedPage {
        GuidedPageTitle("ADICIONAR EXERCÍCIO", "Toque no exercício destacado para incluí-lo.")
        GuidedTarget(
            label = "SUPINO RETO COM BARRA",
            supportingText = "Peitoral • Barra",
            icon = Icons.Outlined.FitnessCenter,
            onClick = onSelect,
        )
        GuidedMutedCard("Remada baixa", "Costas • Cabo")
        GuidedMutedCard("Elevação lateral", "Ombros • Halteres")
    }
}

@Composable
private fun GuidedRoutineReady(routineName: String, onStart: () -> Unit) {
    GuidedPage {
        GuidedPageTitle("ROTINA PRONTA", "A rotina falsa já tem um exercício para a demonstração.")
        GuidedMutedCard(routineName, "1 exercício • Supino reto com barra")
        GuidedTarget(
            label = "INICIAR TREINO",
            icon = Icons.Outlined.PlayArrow,
            onClick = onStart,
        )
    }
}

@Composable
private fun GuidedActiveWorkout(
    routineName: String,
    step: Int,
    weight: String,
    repetitions: String,
    onWeightChanged: (String) -> Unit,
    onRepetitionsChanged: (String) -> Unit,
    onAddSet: () -> Unit,
    onCompleteSet: () -> Unit,
    onFinish: () -> Unit,
) {
    GuidedPage {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("TREINO ATIVO", color = MaterialTheme.colorScheme.primary)
                Text(routineName, style = MaterialTheme.typography.titleLarge)
            }
            if (step == 6) {
                GuidedCompactTarget(label = "FINALIZAR", onClick = onFinish)
            } else {
                Text("00:03", color = ForgeFlowDesign.colors.textSecondary)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GuidedMetric("VOLUME", if (step >= 6) "$weight kg" else "0 kg", Modifier.weight(1f))
            GuidedMetric("SÉRIES", if (step >= 6) "1 / 1" else "0 / 0", Modifier.weight(1f))
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.FitnessCenter, contentDescription = null)
                        }
                    }
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text("Supino reto com barra", fontWeight = FontWeight.Bold)
                        Text(
                            "Último: ainda sem histórico",
                            color = ForgeFlowDesign.colors.textSecondary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                if (step >= 5) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = weight,
                            onValueChange = onWeightChanged,
                            modifier = Modifier.weight(1f),
                            label = { Text("KG") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = repetitions,
                            onValueChange = onRepetitionsChanged,
                            modifier = Modifier.weight(1f),
                            label = { Text("REPS") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                        )
                        if (step == 5) {
                            GuidedIconTarget(
                                icon = Icons.Outlined.Check,
                                description = "Concluir série",
                                onClick = onCompleteSet,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "Série concluída",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
                if (step == 4) {
                    GuidedTarget(
                        label = "ADICIONAR SÉRIE",
                        icon = Icons.Outlined.Add,
                        onClick = onAddSet,
                    )
                }
            }
        }
    }
}

@Composable
private fun GuidedFinishWorkout(
    includeLocation: Boolean,
    onIncludeLocationChanged: (Boolean) -> Unit,
    onFinish: () -> Unit,
) {
    GuidedPage {
        GuidedPageTitle("REVISAR FINALIZAÇÃO", "Escolha se este treino teria ou não um local salvo.")
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null)
                Column(modifier = Modifier.weight(1f)) {
                    Text("SALVAR LOCALIZAÇÃO", fontWeight = FontWeight.Bold)
                    Text(
                        if (includeLocation) "Com local ao finalizar" else "Finalizar sem local",
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Switch(
                    checked = includeLocation,
                    onCheckedChange = onIncludeLocationChanged,
                )
            }
        }
        GuidedTarget(
            label = "CONCLUIR DEMONSTRAÇÃO",
            icon = Icons.Outlined.CheckCircle,
            onClick = onFinish,
        )
    }
}

@Composable
private fun GuidedTutorialComplete(includedLocation: Boolean, onDismiss: () -> Unit) {
    GuidedPage(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(28.dp))
        Surface(
            modifier = Modifier.size(82.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Text(
            text = "TREINO DEMONSTRATIVO CONCLUÍDO",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = if (includedLocation) {
                "Você praticou a finalização com local. Nenhum local foi realmente capturado."
            } else {
                "Você praticou a finalização sem local. Nenhuma permissão foi solicitada."
            },
            textAlign = TextAlign.Center,
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodyLarge,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(
                text = "A rotina, a série e o treino eram temporários e não foram enviados ao histórico.",
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
        }
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("ENTRAR NO FORGEFLOW")
        }
    }
}

@Composable
private fun GuidedPage(
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content,
    )
}

@Composable
private fun GuidedPageTitle(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineSmall)
        Text(
            description,
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun GuidedTarget(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    supportingText: String? = null,
    enabled: Boolean = true,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.Black)
                supportingText?.let {
                    Text(
                        it,
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun GuidedCompactTarget(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primary,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun GuidedIconTarget(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .size(52.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.onPrimary),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = description, tint = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun GuidedMutedCard(title: String, description: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(
                description,
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun GuidedMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, color = ForgeFlowDesign.colors.textSecondary)
            Text(value, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun guidedStepCopy(step: Int): Pair<String, String> = when (step) {
    0 -> "NOVA ROTINA" to "O destaque mostra exatamente onde começa a criação de um treino."
    1 -> "NOME DO TREINO" to "Edite o nome falso e avance para montar a rotina."
    2 -> "ADICIONAR EXERCÍCIO" to "Escolha o supino destacado para esta prática guiada."
    3 -> "INICIAR TREINO" to "A rotina está pronta. Agora abra a sessão ativa."
    4 -> "ADICIONAR SÉRIE" to "Inclua uma série no exercício sem tocar no histórico real."
    5 -> "CONCLUIR SÉRIE" to "Ajuste peso e repetições e toque no botão de confirmação."
    6 -> "FINALIZAR" to "Com uma série válida, o treino já pode ser finalizado."
    7 -> "COM OU SEM LOCAL" to "Teste o seletor e conclua. Nenhuma localização será capturada."
    else -> "PRONTO PARA TREINAR" to "A demonstração foi descartada com segurança."
}

private const val GUIDED_STEP_COUNT = 9
