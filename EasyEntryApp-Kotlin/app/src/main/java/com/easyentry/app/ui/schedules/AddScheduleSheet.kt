package com.easyentry.app.ui.schedules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.easyentry.app.data.local.db.ScheduledActionWithDeviceInfo
import com.easyentry.app.domain.model.Device
import com.easyentry.app.domain.model.DeviceStatus

private val DAY_LABELS = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleSheet(
    availableDevices: List<Device>,
    editingAction: ScheduledActionWithDeviceInfo?,
    onSave: (deviceIds: List<Int>, actionStatusValue: Int, isRecurring: Boolean, dayOfWeekBitmask: Int, hourOfDay: Int, minuteOfHour: Int, delayMinutes: Int, label: String) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val initialAction = editingAction?.action
    val initialDeviceIds = remember {
        if (initialAction?.deviceIds?.isNotBlank() == true) {
            initialAction.deviceIds.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        } else if (initialAction != null) {
            setOf(initialAction.deviceId)
        } else {
            availableDevices.firstOrNull()?.let { setOf(it.id) } ?: emptySet()
        }
    }
    var selectedDeviceIds by remember { mutableStateOf(initialDeviceIds) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var actionStatusValue by remember {
        mutableIntStateOf(initialAction?.actionStatusValue ?: DeviceStatus.OPENED.value)
    }
    var isRecurring by remember {
        mutableStateOf(initialAction?.isRecurring ?: true)
    }
    var dayMask by remember {
        mutableIntStateOf(initialAction?.dayOfWeekBitmask ?: 0b0011111)
    }
    val timePickerState = rememberTimePickerState(
        initialHour = initialAction?.hourOfDay ?: 7,
        initialMinute = initialAction?.minuteOfHour ?: 0,
        is24Hour = true
    )
    var showTimeDialog by remember { mutableStateOf(false) }
    var delayMinutesText by remember {
        mutableStateOf(initialAction?.delayMinutes?.toString() ?: "10")
    }
    var label by remember {
        mutableStateOf(initialAction?.label ?: "")
    }

    if (showTimeDialog) {
        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            confirmButton = {
                TextButton(onClick = { showTimeDialog = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimeDialog = false }) { Text("Abbrechen") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp)
                .imePadding()
                .navigationBarsPadding()
        ) {
            Text(
                text = if (editingAction == null) "Zeitplan hinzufügen" else "Zeitplan bearbeiten",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- Gerät (Multiselect Dropdown) ---
            Text(
                text = "Gerät",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (availableDevices.isNotEmpty()) {
                val selectedNames = availableDevices
                    .filter { it.id in selectedDeviceIds }
                    .joinToString(", ") { it.name }
                    .ifEmpty { "Kein Gerät ausgewählt" }

                Box {
                    OutlinedTextField(
                        value = selectedNames,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { dropdownExpanded = true },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    // Invisible click overlay to open dropdown
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { dropdownExpanded = true }
                    )
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        properties = PopupProperties(focusable = true),
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        availableDevices.forEach { device ->
                            val isChecked = device.id in selectedDeviceIds
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(device.name)
                                    }
                                },
                                onClick = {
                                    selectedDeviceIds = if (isChecked) {
                                        selectedDeviceIds - device.id
                                    } else {
                                        selectedDeviceIds + device.id
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Keine Geräte vorhanden",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Aktion ---
            Text(
                text = "Aktion",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            val actions = listOf(
                DeviceStatus.OPENED.value to "Öffnen",
                DeviceStatus.CLOSED.value to "Schließen",
                DeviceStatus.NEUTRAL.value to "Stopp"
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                actions.forEachIndexed { index, (value, actionLabel) ->
                    SegmentedButton(
                        selected = actionStatusValue == value,
                        onClick = { actionStatusValue = value },
                        shape = SegmentedButtonDefaults.itemShape(index, actions.size)
                    ) {
                        Text(actionLabel)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Typ ---
            Text(
                text = "Typ",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = isRecurring,
                    onClick = { isRecurring = true },
                    shape = SegmentedButtonDefaults.itemShape(0, 2)
                ) { Text("Wiederkehrend") }
                SegmentedButton(
                    selected = !isRecurring,
                    onClick = { isRecurring = false },
                    shape = SegmentedButtonDefaults.itemShape(1, 2)
                ) { Text("Einmalig") }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isRecurring) {
                // --- Wochentage (Kreise) ---
                Text(
                    text = "Wochentage",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DAY_LABELS.forEachIndexed { index, day ->
                        val isSelected = dayMask and (1 shl index) != 0
                        DayToggleButton(
                            label = day,
                            selected = isSelected,
                            onClick = {
                                dayMask = if (isSelected) {
                                    dayMask and (1 shl index).inv()
                                } else {
                                    dayMask or (1 shl index)
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- Uhrzeit (kompakte Karte) ---
                Text(
                    text = "Uhrzeit",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimeDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "%02d:%02d".format(timePickerState.hour, timePickerState.minute),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Uhrzeit ändern",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ändern",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = delayMinutesText,
                    onValueChange = { delayMinutesText = it.filter { c -> c.isDigit() } },
                    label = { Text("In wie vielen Minuten?") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Bezeichnung (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                if (editingAction != null && onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Löschen", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Abbrechen")
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (availableDevices.isEmpty() || selectedDeviceIds.isEmpty()) return@Button
                        val ids = selectedDeviceIds.toList()
                        val delayMinutes = delayMinutesText.toIntOrNull() ?: 0
                        onSave(
                            ids,
                            actionStatusValue,
                            isRecurring,
                            if (isRecurring) dayMask else 0,
                            if (isRecurring) timePickerState.hour else 0,
                            if (isRecurring) timePickerState.minute else 0,
                            if (!isRecurring) delayMinutes else 0,
                            label
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Speichern")
                }
            }
        }
    }
}

@Composable
private fun DayToggleButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .then(
                if (selected) {
                    Modifier.background(colorScheme.primary)
                } else {
                    Modifier.border(1.5.dp, colorScheme.outlineVariant, CircleShape)
                }
            )
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) colorScheme.onPrimary else colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}
