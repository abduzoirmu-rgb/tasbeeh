package com.tasbeeh.app.presentation.dhikr

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tasbeeh.app.domain.model.Dhikr
import com.tasbeeh.app.presentation.localization.LocalStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DhikrListScreen(
    selectedDhikrId: Long?,
    onSelectDhikr: (Dhikr) -> Unit,
    viewModel: DhikrViewModel = hiltViewModel()
) {
    val dhikrs by viewModel.dhikrs.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    DhikrListContent(
        dhikrs = dhikrs,
        selectedDhikrId = selectedDhikrId,
        onSelectDhikr = onSelectDhikr,
        onAddClick = { showAddDialog = true },
        onDeleteDhikr = { viewModel.deleteDhikr(it) }
    )

    if (showAddDialog) {
        AddDhikrDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, arabic, target ->
                viewModel.saveDhikr(name, arabic, target)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DhikrListContent(
    dhikrs: List<Dhikr>,
    selectedDhikrId: Long?,
    onSelectDhikr: (Dhikr) -> Unit,
    onAddClick: () -> Unit,
    onDeleteDhikr: (Long) -> Unit
) {
    val strings = LocalStrings.current
    val presetDhikrs = dhikrs.filter { !it.isCustom }
    val customDhikrs = dhikrs.filter { it.isCustom }

    Scaffold(
        topBar = { TopAppBar(title = { Text(strings.dhikrsTitle) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = strings.addDhikr)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            items(presetDhikrs, key = { it.id }) { dhikr ->
                DhikrCard(
                    dhikr = dhikr,
                    isSelected = dhikr.id == selectedDhikrId,
                    onSelect = { onSelectDhikr(dhikr) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (customDhikrs.isNotEmpty()) {
                item {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = strings.myDhikrs,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(customDhikrs, key = { it.id }) { dhikr ->
                    val dismissState = rememberDismissState(
                        confirmValueChange = { value ->
                            if (value == DismissValue.DismissedToStart) {
                                onDeleteDhikr(dhikr.id); true
                            } else false
                        }
                    )
                    SwipeToDismiss(
                        state = dismissState,
                        directions = setOf(DismissDirection.EndToStart),
                        background = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .padding(end = 16.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = strings.delete,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        },
                        dismissContent = {
                            DhikrCard(
                                dhikr = dhikr,
                                isSelected = dhikr.id == selectedDhikrId,
                                onSelect = { onSelectDhikr(dhikr) }
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun DhikrCard(dhikr: Dhikr, isSelected: Boolean, onSelect: () -> Unit) {
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = dhikr.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "×${dhikr.targetCount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            dhikr.arabicText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun AddDhikrDialog(onDismiss: () -> Unit, onConfirm: (String, String?, Int) -> Unit) {
    val strings = LocalStrings.current
    var name by remember { mutableStateOf("") }
    var arabicText by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("33") }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.newDhikr) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text(strings.dhikrNameLabel) },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text(strings.dhikrNameRequired) }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = arabicText,
                    onValueChange = { arabicText = it },
                    label = { Text(strings.arabicTextLabel) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it.filter { c -> c.isDigit() } },
                    label = { Text(strings.goalCountLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank()) {
                    nameError = true
                } else {
                    onConfirm(name.trim(), arabicText.takeIf { it.isNotBlank() }, target.toIntOrNull() ?: 33)
                }
            }) { Text(strings.add) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}
