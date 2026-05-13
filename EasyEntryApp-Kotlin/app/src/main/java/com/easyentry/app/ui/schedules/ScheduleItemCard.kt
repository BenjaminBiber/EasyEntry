package com.easyentry.app.ui.schedules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.easyentry.app.data.local.db.ScheduledActionWithDeviceInfo
import com.easyentry.app.domain.model.Device
import com.easyentry.app.domain.model.DeviceStatus

@Composable
fun ScheduleItemCard(
    item: ScheduledActionWithDeviceInfo,
    availableDevices: List<Device>,
    onToggleEnabled: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val actionLabel = when (item.action.actionStatusValue) {
                    DeviceStatus.OPENED.value -> "Öffnen"
                    DeviceStatus.CLOSED.value -> "Schließen"
                    else -> "Stopp"
                }
                val deviceNamesText = if (item.action.deviceIds.isNotBlank()) {
                    val ids = item.action.deviceIds.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
                    availableDevices.filter { it.id in ids }.joinToString(", ") { it.name }.ifEmpty { item.deviceName }
                } else {
                    item.deviceName
                }
                val displayLabel = if (item.action.label.isNotBlank()) {
                    item.action.label
                } else {
                    "$deviceNamesText – $actionLabel"
                }
                Text(
                    text = displayLabel,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = scheduleDisplayText(item),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = item.action.isEnabled,
                onCheckedChange = onToggleEnabled
            )
        }
    }
}

internal fun scheduleDisplayText(item: ScheduledActionWithDeviceInfo): String {
    val action = item.action
    return if (action.isRecurring) {
        val dayNames = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")
        val days = dayNames.filterIndexed { i, _ -> action.dayOfWeekBitmask and (1 shl i) != 0 }
        val daysText = if (days.isEmpty()) "–" else days.joinToString(", ")
        "$daysText • %02d:%02d".format(action.hourOfDay, action.minuteOfHour)
    } else {
        "Einmalig in ${action.delayMinutes} Minuten"
    }
}
