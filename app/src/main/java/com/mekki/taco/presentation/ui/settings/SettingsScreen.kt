package com.mekki.taco.presentation.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.mekki.taco.R
import com.mekki.taco.data.manager.RevertPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current

    val backupFileName = remember {
        val formatter = SimpleDateFormat("dd'_'MMM'_'yyyy'_'HH'h'mm", Locale("pt", "BR"))
        "backup_nutritaco_${formatter.format(Date())}.json"
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.onExportData(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.onImportData(it) }
    }

    if (uiState.showImportDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissImportDialog() },
            title = { Text(text = "Importar Dados") },
            text = {
                Text(
                    "Como você deseja importar os dados?\n\n" +
                            "• Mesclar: Mantém seus dados atuais e adiciona os novos dados do backup.\n" +
                            "• Sobrescrever: Apaga TODOS os dados atuais e restaura o backup."
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmImport(merge = true) }
                ) {
                    Text("Mesclar")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = { viewModel.dismissImportDialog() }
                    ) {
                        Text("Cancelar")
                    }
                    TextButton(
                        onClick = { viewModel.confirmImport(merge = false) },
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Sobrescrever")
                    }
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Preferences Section
        SettingsSection(title = "Preferências") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Modo Escuro", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Alterar aparência do aplicativo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.userProfile.isDarkMode,
                    onCheckedChange = { viewModel.toggleTheme(it) }
                )
            }
        }

        HorizontalDivider()

        // Data & Backup Section
        var showDriveHelpDialog by remember { mutableStateOf(false) }

        if (showDriveHelpDialog) {
            AlertDialog(
                onDismissRequest = { showDriveHelpDialog = false },
                icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                title = { Text("Como salvar no Google Drive") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            buildAnnotatedString {
                                append("1. Instale o ")
                                withStyle(
                                    SpanStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        textDecoration = TextDecoration.Underline
                                    )
                                ) {
                                    append("Google Drive")
                                }
                                append(" (Play Store)")
                            },
                            modifier = Modifier.clickable {
                                uriHandler.openUri("https://play.google.com/store/apps/details?id=com.google.android.apps.docs")
                            }
                        )
                        Text("2. Ao salvar, toque no menu ☰ no canto superior esquerdo")
                        Text("3. Selecione sua conta do Drive")
                        Text("4. Escolha \"Meu Drive\" ou uma pasta de sua preferência")
                        Text("5. Toque em \"Salvar\"")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDriveHelpDialog = false }) {
                        Text("Entendi")
                    }
                }
            )
        }

        SettingsSectionWithAction(
            title = "Dados & Backup",
            action = {
                IconButton(onClick = { showDriveHelpDialog = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Como salvar no Google Drive",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        ) {
            if (uiState.backupMessage != null) {
                Text(
                    text = uiState.backupMessage ?: "",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (uiState.isBackupLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Processando...", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { exportLauncher.launch(backupFileName) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Salvar Dados (Local ou Google Drive)")
                    }
                    Button(
                        onClick = { importLauncher.launch(arrayOf("application/json")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Restaurar Dados (.json)")
                    }
                }
            }
        }

        if (uiState.revertPoints.isNotEmpty()) {
            HorizontalDivider()

            SettingsSection(title = "Pontos de Restauração") {
                Text(
                    "Restaure seus dados para o estado anterior a uma importação.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                uiState.revertPoints.forEach { revertPoint ->
                    RevertPointItem(
                        revertPoint = revertPoint,
                        onRestore = { viewModel.restoreFromRevertPoint(revertPoint) }
                    )
                }
            }
        }

        HorizontalDivider()

        // Sources Section
        SettingsSection(title = "Fontes de Dados") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Tabela TACO",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Dados nutricionais baseados na Tabela Brasileira de Composição de Alimentos (TACO) - NEPA/UNICAMP.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Alertas Nutricionais",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Os alertas de \"Alto em Sódio\" e \"Alto em Gorduras Saturadas\" são baseados nos limites estabelecidos pela ANVISA na Instrução Normativa IN Nº 75/2020 (Anexo XV) para rotulagem nutricional frontal.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• Sódio: ≥ 600mg por 100g\n• Gorduras Saturadas: ≥ 6g por 100g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        HorizontalDivider()

        // About Section
        SettingsSection(title = "Sobre") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NutriTACO",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Dados nutricionais baseados na Tabela TACO - NEPA/UNICAMP",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = {
                            uriHandler.openUri("https://github.com/gustarmartins/TACO")
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_github),
                            contentDescription = "Repositório do GitHub",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Projeto Open Source",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            uriHandler.openUri("https://github.com/gustarmartins/TACO")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

@Composable
fun SettingsSectionWithAction(
    title: String,
    action: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            action()
        }
        content()
    }
}

@Composable
private fun RevertPointItem(
    revertPoint: RevertPoint,
    onRestore: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    val dateFormatter = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }
    val formattedDate = remember(revertPoint.timestamp) {
        dateFormatter.format(Date(revertPoint.timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formattedDate,
            style = MaterialTheme.typography.bodyMedium
        )
        OutlinedButton(onClick = { showConfirmDialog = true }) {
            Text("Restaurar")
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Restaurar Dados?") },
            text = {
                Text("Isso irá substituir TODOS os seus dados atuais pelos dados de $formattedDate. Esta ação não pode ser desfeita.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        onRestore()
                    }
                ) {
                    Text("Restaurar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}