package com.easyentry.app.ui.groupdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.easyentry.app.R
import com.easyentry.app.domain.model.DeviceStatus
import com.easyentry.app.ui.common.ConfirmationDialog
import com.easyentry.app.ui.home.DeviceCard
import com.easyentry.app.ui.home.MoveToGroupSheet
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    onBack: () -> Unit,
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.snackbarShown()
        }
    }

    val group = uiState.group
    val groupColor = group?.let { Color(it.color) } ?: MaterialTheme.colorScheme.primary
    val batchEnabled = uiState.loadingButtons.isEmpty() && uiState.batchLoadingAction == null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(group?.groupName ?: "")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onBatchControl(DeviceStatus.OPENED) },
                        enabled = batchEnabled
                    ) {
                        if (uiState.batchLoadingAction == DeviceStatus.OPENED) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Alle öffnen")
                        }
                    }
                    IconButton(
                        onClick = { viewModel.onBatchControl(DeviceStatus.NEUTRAL) },
                        enabled = batchEnabled
                    ) {
                        if (uiState.batchLoadingAction == DeviceStatus.NEUTRAL) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Stop, contentDescription = "Alle stoppen")
                        }
                    }
                    IconButton(
                        onClick = { viewModel.onBatchControl(DeviceStatus.CLOSED) },
                        enabled = batchEnabled
                    ) {
                        if (uiState.batchLoadingAction == DeviceStatus.CLOSED) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Alle schließen")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = groupColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        val devices = group?.devices.orEmpty()
        var localDevices by remember(devices) { mutableStateOf(devices) }
        val lazyListState = rememberLazyListState()
        val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
            localDevices = localDevices.toMutableList().apply { add(to.index - 1, removeAt(from.index - 1)) }
        }

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.reload() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (devices.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.group_detail_no_devices),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { }
                    items(localDevices, key = { it.id }) { device ->
                        ReorderableItem(reorderState, key = device.id) { _ ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DragHandle,
                                    contentDescription = "Reihenfolge ändern",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .draggableHandle(
                                            onDragStopped = {
                                                viewModel.reorderDevices(localDevices.map { it.id })
                                            }
                                        ),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                DeviceCard(
                                    device = device,
                                    isOnline = uiState.deviceOnlineStatus[device.id] ?: false,
                                    loadingActions = setOfNotNull(uiState.loadingButtons[device.id]),
                                    enabled = uiState.batchLoadingAction == null,
                                    onControl = { status: DeviceStatus -> viewModel.onControlButton(device, status) },
                                    onMoveToGroup = { viewModel.showMoveDeviceSheet(device.id) },
                                    onDeleteDevice = { viewModel.showDeleteDeviceDialog(device.id) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp)
                                )
                            }
                        }
                    }
                    item { }
                }

                if (uiState.showMoveSheet) {
                    MoveToGroupSheet(
                        groups = uiState.allGroups,
                        currentGroupId = uiState.group?.id ?: -1,
                        onSelect = { groupId ->
                            uiState.moveDeviceId?.let { viewModel.moveDevice(it, groupId) }
                        },
                        onDismiss = { viewModel.hideMoveDeviceSheet() }
                    )
                }
            }
        }
    }

    if (uiState.showDeleteDeviceDialog) {
        ConfirmationDialog(
            message = stringResource(R.string.confirm_delete_device),
            onConfirm = {
                uiState.deleteDeviceId?.let { viewModel.deleteDevice(it) }
            },
            onDismiss = { viewModel.hideDeleteDeviceDialog() }
        )
    }
}
