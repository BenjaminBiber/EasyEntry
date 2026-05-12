package com.easyentry.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.easyentry.app.domain.model.Device
import com.easyentry.app.domain.model.DeviceGroup
import com.easyentry.app.domain.model.DeviceStatus
import sh.calvin.reorderable.ReorderableColumn
import sh.calvin.reorderable.ReorderableItem

@Composable
fun DeviceGroupSection(
    group: DeviceGroup,
    isExpanded: Boolean,
    deviceOnlineStatus: Map<Int, Boolean>,
    loadingDeviceActions: Set<Pair<Int, DeviceStatus>>,
    onToggle: () -> Unit,
    onControl: (deviceId: Int, status: DeviceStatus) -> Unit,
    onMoveToGroup: (deviceId: Int) -> Unit,
    onReorder: (groupId: Int, deviceIds: List<Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    var items by remember(group.devices) { mutableStateOf(group.devices) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = group.groupName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Einklappen" else "Ausklappen",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            ReorderableColumn(
                list = items,
                onSettle = { fromIndex, toIndex ->
                    val newItems = items.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
                    items = newItems
                    onReorder(group.id, newItems.map { it.id })
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) { _, device: Device, _ ->
                key(device.id) {
                    ReorderableItem {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DragHandle,
                                contentDescription = "Reihenfolge ändern",
                                modifier = Modifier
                                    .size(24.dp)
                                    .draggableHandle(),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            DeviceCard(
                                device = device,
                                isOnline = deviceOnlineStatus[device.id] ?: false,
                                loadingActions = loadingDeviceActions.filter { it.first == device.id }.map { it.second }.toSet(),
                                onControl = { status -> onControl(device.id, status) },
                                onMoveToGroup = { onMoveToGroup(device.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
