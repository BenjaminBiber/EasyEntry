package com.easyentry.app.ui.home

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easyentry.app.R
import com.easyentry.app.domain.model.Device
import com.easyentry.app.domain.model.DeviceStatus
import com.easyentry.app.ui.theme.DoorClose
import com.easyentry.app.ui.theme.DoorOpen
import com.easyentry.app.ui.theme.DoorStop
import com.easyentry.app.ui.theme.Offline
import com.easyentry.app.ui.theme.Online

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceCard(
    device: Device,
    isOnline: Boolean,
    loadingStatus: DeviceStatus?,
    onControl: (DeviceStatus) -> Unit,
    onMoveToGroup: () -> Unit,
    onDeleteDevice: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> onMoveToGroup()
                SwipeToDismissBoxValue.EndToStart -> onDeleteDevice?.invoke()
                else -> Unit
            }
            false
        },
        positionalThreshold = { it * 0.4f }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = onDeleteDevice != null,
        backgroundContent = {
            val swipeTarget = dismissState.targetValue
            val isMoveRevealing = swipeTarget == SwipeToDismissBoxValue.StartToEnd
            val isDeleteRevealing = swipeTarget == SwipeToDismissBoxValue.EndToStart
            val bgColor by animateColorAsState(
                targetValue = when {
                    isMoveRevealing -> MaterialTheme.colorScheme.tertiaryContainer
                    isDeleteRevealing -> MaterialTheme.colorScheme.errorContainer
                    else -> Color.Transparent
                },
                label = "swipe_bg"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(bgColor)
            ) {
                if (isMoveRevealing) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = stringResource(R.string.move_to_group),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                } else if (isDeleteRevealing) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.delete),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isOnline) Online else Offline
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isOnline) {
                                if (device.isOpened) stringResource(R.string.status_open)
                                else stringResource(R.string.status_closed)
                            } else {
                                stringResource(R.string.device_unreachable)
                            },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = device.deviceUrl,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ControlButton(
                            icon = { Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Öffnen", modifier = Modifier.size(22.dp)) },
                            color = DoorOpen,
                            isLoading = loadingStatus == DeviceStatus.OPENED,
                            enabled = isOnline && loadingStatus == null,
                            onClick = { onControl(DeviceStatus.OPENED) }
                        )
                        ControlButton(
                            icon = { Icon(Icons.Default.Stop, contentDescription = "Stopp", modifier = Modifier.size(22.dp)) },
                            color = DoorStop,
                            isLoading = loadingStatus == DeviceStatus.NEUTRAL,
                            enabled = isOnline && loadingStatus == null,
                            onClick = { onControl(DeviceStatus.NEUTRAL) }
                        )
                        ControlButton(
                            icon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Schließen", modifier = Modifier.size(22.dp)) },
                            color = DoorClose,
                            isLoading = loadingStatus == DeviceStatus.CLOSED,
                            enabled = isOnline && loadingStatus == null,
                            onClick = { onControl(DeviceStatus.CLOSED) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlButton(
    icon: @Composable () -> Unit,
    color: Color,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = Modifier.size(44.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                icon()
            }
        }
    }
}
