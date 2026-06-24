package com.tasbeeh.app.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tasbeeh.app.domain.model.Settings
import com.tasbeeh.app.presentation.localization.LocalStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsState()

    SettingsContent(
        settings = settings,
        onVibrationToggle = viewModel::updateVibration,
        onSoundToggle = viewModel::updateClickSound,
        onThemeToggle = viewModel::updateTheme,
        onLanguageSelect = viewModel::updateLanguage,
        onClearHistory = viewModel::clearHistory
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    settings: Settings,
    onVibrationToggle: (Boolean) -> Unit,
    onSoundToggle: (Boolean) -> Unit,
    onThemeToggle: (Boolean) -> Unit,
    onLanguageSelect: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    val strings = LocalStrings.current
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(strings.settingsTitle) }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            SettingsGroupHeader(strings.soundVibrationGroup)

            SettingsToggleItem(
                title = strings.vibrationTitle,
                subtitle = strings.vibrationSubtitle,
                checked = settings.vibrationEnabled,
                onCheckedChange = onVibrationToggle
            )
            SettingsToggleItem(
                title = strings.clickSoundTitle,
                subtitle = strings.clickSoundSubtitle,
                checked = settings.clickSoundEnabled,
                onCheckedChange = onSoundToggle
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsGroupHeader(strings.appearanceGroup)

            SettingsToggleItem(
                title = strings.darkThemeTitle,
                subtitle = strings.darkThemeSubtitle,
                checked = settings.isDarkTheme,
                onCheckedChange = onThemeToggle
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsGroupHeader(strings.languageGroup)

            // Language selector chips
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                listOf("ru" to strings.langRu, "en" to strings.langEn, "ar" to strings.langAr)
                    .forEach { (code, label) ->
                        FilterChip(
                            selected = settings.language == code,
                            onClick = { onLanguageSelect(code) },
                            label = { Text(label) },
                            modifier = Modifier.padding(end = 8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsGroupHeader(strings.dataGroup)

            TextButton(
                onClick = { showClearDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.clearHistory, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = strings.appVersion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(strings.clearHistoryConfirmTitle) },
            text = { Text(strings.clearHistoryConfirmBody) },
            confirmButton = {
                TextButton(onClick = { onClearHistory(); showClearDialog = false }) {
                    Text(strings.delete, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text(strings.cancel) }
            }
        )
    }
}

@Composable
fun SettingsGroupHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
