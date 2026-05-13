package com.easyentry.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentWidth
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.easyentry.app.worker.computeDelayMillis
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.firstOrNull

class ScheduleWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ScheduleWidgetEntryPoint::class.java
        )
        val dao       = entryPoint.scheduledActionDao()
        val deviceDao = entryPoint.deviceDao()
        val allSchedules = dao.getAllWithDeviceInfo().firstOrNull() ?: emptyList()

        val rows = allSchedules
            .map { item ->
                val delayMs = if (item.action.isEnabled) {
                    computeDelayMillis(
                        item.action.hourOfDay,
                        item.action.minuteOfHour,
                        item.action.dayOfWeekBitmask,
                        item.action.isRecurring,
                        item.action.delayMinutes
                    )
                } else Long.MAX_VALUE

                val label = if (item.action.label.isNotBlank()) {
                    item.action.label
                } else {
                    val ids = item.action.deviceIds
                        .split(",")
                        .mapNotNull { it.trim().toIntOrNull() }
                        .filter { it > 0 }
                        .takeIf { it.size > 1 }
                    val deviceNames = if (ids != null) {
                        ids.mapNotNull { deviceDao.getById(it)?.name }.joinToString(", ")
                    } else {
                        item.deviceName
                    }
                    "$deviceNames · ${actionLabel(item.action.actionStatusValue)}"
                }

                ScheduleRow(
                    id = item.action.id,
                    label = label,
                    isEnabled = item.action.isEnabled,
                    delayMs = delayMs
                )
            }
            .sortedWith(compareBy({ !it.isEnabled }, { it.delayMs }))
            .take(3)

        provideContent {
            GlanceTheme {
                ScheduleWidgetContent(rows)
            }
        }
    }
}

private data class ScheduleRow(
    val id: Int,
    val label: String,
    val isEnabled: Boolean,
    val delayMs: Long
)

private fun actionLabel(value: Int) = when (value) {
    1    -> "Öffnen"
    2    -> "Schließen"
    else -> "Stopp"
}

private fun formatDelay(delayMs: Long): String {
    val totalMinutes = delayMs / 60_000L
    val hours   = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "in ${hours}h ${minutes}min"
        hours > 0                -> "in ${hours}h"
        else                     -> "in ${minutes}min"
    }
}

@Composable
private fun ScheduleWidgetContent(rows: List<ScheduleRow>) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(GlanceTheme.colors.widgetBackground)
            .padding(12.dp)
    ) {
        Text(
            text = "Zeitpläne",
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = GlanceModifier.height(8.dp))

        if (rows.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Keine Zeitpläne",
                    style = TextStyle(
                        color = GlanceTheme.colors.secondary,
                        fontSize = 12.sp
                    )
                )
            }
        } else {
            rows.forEachIndexed { index, row ->
                if (index > 0) Spacer(modifier = GlanceModifier.height(6.dp))
                ScheduleRowItem(row)
            }
        }
    }
}

@Composable
private fun ScheduleRowItem(row: ScheduleRow) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Text(
            text = row.label,
            style = TextStyle(
                color = if (row.isEnabled) GlanceTheme.colors.onSurface
                        else GlanceTheme.colors.secondary,
                fontSize = 12.sp
            ),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight()
        )
        Spacer(modifier = GlanceModifier.width(6.dp))
        Text(
            text = if (row.isEnabled) formatDelay(row.delayMs) else "Pausiert",
            style = TextStyle(
                color = GlanceTheme.colors.secondary,
                fontSize = 11.sp
            ),
            modifier = GlanceModifier.wrapContentWidth()
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Box(
            modifier = GlanceModifier
                .wrapContentWidth()
                .height(32.dp)
                .background(
                    ColorProvider(
                        if (row.isEnabled) Color(0xFF00C853) else Color(0xFF808080)
                    )
                )
                .cornerRadius(8.dp)
                .padding(horizontal = 12.dp)
                .clickable(
                    actionRunCallback<ScheduleWidgetToggleCallback>(
                        actionParametersOf(
                            ScheduleWidgetToggleCallback.KEY_SCHEDULE_ID to row.id,
                            ScheduleWidgetToggleCallback.KEY_NEW_ENABLED  to !row.isEnabled
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (row.isEnabled) "Aktiv" else "Aus",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
