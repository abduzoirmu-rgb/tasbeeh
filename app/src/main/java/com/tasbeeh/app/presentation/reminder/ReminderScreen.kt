package com.tasbeeh.app.presentation.reminder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tasbeeh.app.domain.model.Reminder
import com.tasbeeh.app.presentation.localization.AppStrings
import com.tasbeeh.app.presentation.localization.LocalStrings
import com.tasbeeh.app.presentation.localization.localizedDaysLabel

private val DarkBackground = Color(0xFF0D1B15)
private val DarkSurface    = Color(0xFF112218)
private val PrimaryTeal    = Color(0xFF1D9A6C)
private val OnDarkMuted    = Color(0xFF7A9D8C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(viewModel: ReminderViewModel = hiltViewModel()) {
    val state   by viewModel.uiState.collectAsState()
    val strings = LocalStrings.current

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text(strings.remindersTitle, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { viewModel.showAddDialog() },
                containerColor = PrimaryTeal,
                contentColor   = Color.White,
                shape          = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.newReminder)
            }
        }
    ) { innerPadding ->
        if (state.reminders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Alarm, contentDescription = null, tint = OnDarkMuted.copy(alpha = 0.5f), modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(strings.noReminders, color = OnDarkMuted, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    Text(strings.tapPlusToAdd, color = OnDarkMuted.copy(alpha = 0.6f), fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(Modifier.height(4.dp)) }
                items(state.reminders, key = { it.id }) { reminder ->
                    ReminderCard(
                        reminder  = reminder,
                        strings   = strings,
                        onToggle  = { viewModel.toggleEnabled(reminder.id, it) },
                        onDelete  = { viewModel.deleteReminder(reminder.id) },
                        onEdit    = { viewModel.showAddDialog(reminder) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (state.showAddDialog) {
        AddReminderDialog(
            existing  = state.editingReminder,
            strings   = strings,
            onDismiss = { viewModel.dismissDialog() },
            onSave    = { viewModel.saveReminder(it) }
        )
    }
}

@Composable
fun ReminderCard(
    reminder: Reminder,
    strings: AppStrings,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onEdit() },
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier         = Modifier.size(40.dp).clip(CircleShape)
                        .background(PrimaryTeal.copy(alpha = if (reminder.isEnabled) 0.2f else 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Alarm, contentDescription = null, tint = if (reminder.isEnabled) PrimaryTeal else OnDarkMuted, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text(text = reminder.title, color = if (reminder.isEnabled) Color.White else OnDarkMuted, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    Spacer(Modifier.height(3.dp))
                    Text(text = "${reminder.formattedTime()} · ${reminder.localizedDaysLabel(strings)}", color = OnDarkMuted, fontSize = 12.sp)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = strings.delete, tint = OnDarkMuted.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                }
                Switch(
                    checked         = reminder.isEnabled,
                    onCheckedChange = onToggle,
                    colors          = SwitchDefaults.colors(
                        checkedThumbColor   = Color.White,
                        checkedTrackColor   = PrimaryTeal,
                        uncheckedThumbColor = OnDarkMuted,
                        uncheckedTrackColor = DarkBackground
                    )
                )
            }
        }
    }
}

@Composable
fun AddReminderDialog(
    existing: Reminder?,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit
) {
    val allDays = listOf(
        1 to strings.dayMon, 2 to strings.dayTue, 3 to strings.dayWed,
        4 to strings.dayThu, 5 to strings.dayFri, 6 to strings.daySat, 7 to strings.daySun
    )

    var title        by remember { mutableStateOf(existing?.title ?: "") }
    var hour         by remember { mutableIntStateOf(existing?.timeHour ?: 6) }
    var minute       by remember { mutableIntStateOf(existing?.timeMinute ?: 0) }
    var selectedDays by remember {
        mutableStateOf(
            existing?.daysOfWeek?.split(",")?.mapNotNull { it.trim().toIntOrNull() }?.toSet()
                ?: (1..7).toSet()
        )
    }
    var hourDropdown   by remember { mutableStateOf(false) }
    var minuteDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = DarkSurface,
        title = {
            Text(
                text       = if (existing == null) strings.newReminder else strings.editReminder,
                color      = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = title,
                    onValueChange = { title = it },
                    label         = { Text(strings.reminderTitleLabel, color = OnDarkMuted) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = PrimaryTeal,
                        unfocusedBorderColor = OnDarkMuted,
                        focusedTextColor     = Color.White,
                        unfocusedTextColor   = Color.White
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.time, color = OnDarkMuted, fontSize = 14.sp)
                    Box {
                        TextButton(onClick = { hourDropdown = true }) {
                            Text("%02d".format(hour), color = PrimaryTeal, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        DropdownMenu(expanded = hourDropdown, onDismissRequest = { hourDropdown = false }, modifier = Modifier.background(DarkSurface)) {
                            (0..23).forEach { h ->
                                DropdownMenuItem(text = { Text("%02d".format(h), color = Color.White) }, onClick = { hour = h; hourDropdown = false })
                            }
                        }
                    }
                    Text(":", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Box {
                        TextButton(onClick = { minuteDropdown = true }) {
                            Text("%02d".format(minute), color = PrimaryTeal, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        DropdownMenu(expanded = minuteDropdown, onDismissRequest = { minuteDropdown = false }, modifier = Modifier.background(DarkSurface)) {
                            listOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55).forEach { m ->
                                DropdownMenuItem(text = { Text("%02d".format(m), color = Color.White) }, onClick = { minute = m; minuteDropdown = false })
                            }
                        }
                    }
                }

                Text(strings.repeatDays, color = OnDarkMuted, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    allDays.forEach { (dayNum, dayLabel) ->
                        val selected = dayNum in selectedDays
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (selected) PrimaryTeal else DarkBackground)
                                .clickable { selectedDays = if (selected) selectedDays - dayNum else selectedDays + dayNum },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = dayLabel, color = if (selected) Color.White else OnDarkMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(Reminder(
                            id         = existing?.id ?: 0,
                            title      = title.trim(),
                            timeHour   = hour,
                            timeMinute = minute,
                            daysOfWeek = selectedDays.sorted().joinToString(","),
                            isEnabled  = existing?.isEnabled ?: true
                        ))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
            ) {
                Text(strings.save, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel, color = OnDarkMuted) }
        }
    )
}
