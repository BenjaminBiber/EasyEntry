package com.easyentry.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentWidth
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.easyentry.app.R
import com.easyentry.app.domain.model.DeviceStatus
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.firstOrNull

class EasyEntryWidget : GlanceAppWidget() {

    companion object {
        val DEVICE_NAME_KEY    = stringPreferencesKey("w_device_name")
        val IS_OPENED_KEY      = booleanPreferencesKey("w_is_opened")
        val IS_LOADING_KEY     = booleanPreferencesKey("w_is_loading")
        val LOADING_STATUS_KEY = intPreferencesKey("w_loading_status")
        val LAST_ERROR_KEY     = booleanPreferencesKey("w_last_error")

        private val SIZE_WIDE    = DpSize(250.dp, 50.dp)
        private val SIZE_DEFAULT = DpSize(180.dp, 100.dp)
    }

    override val sizeMode = SizeMode.Responsive(setOf(SIZE_WIDE, SIZE_DEFAULT))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val widgetPrefs = entryPoint.widgetPreferencesDataStore()
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val deviceId = widgetPrefs.getDeviceId(appWidgetId).firstOrNull()
        val device = if (deviceId != null) entryPoint.deviceDao().getById(deviceId) else null

        provideContent {
            GlanceTheme {
                val glanceState = currentState<Preferences>()
                val wide = LocalSize.current.height < SIZE_DEFAULT.height

                val params = WidgetParams(
                    deviceId    = deviceId ?: -1,
                    deviceName  = glanceState[DEVICE_NAME_KEY] ?: device?.name ?: "Kein Gerät",
                    isOpened    = glanceState[IS_OPENED_KEY]   ?: device?.isOpened ?: false,
                    isLoading   = glanceState[IS_LOADING_KEY]  ?: false,
                    loadingStatus = glanceState[LOADING_STATUS_KEY] ?: -1,
                    lastError   = glanceState[LAST_ERROR_KEY]  ?: false
                )

                if (wide) WidgetContentWide(params) else WidgetContentDefault(params)
            }
        }
    }
}

private data class WidgetParams(
    val deviceId: Int,
    val deviceName: String,
    val isOpened: Boolean,
    val isLoading: Boolean,
    val loadingStatus: Int,
    val lastError: Boolean
) {
    val statusText get() = when {
        deviceId == -1 -> "Nicht konfiguriert"
        isLoading      -> "Wird ausgeführt…"
        lastError      -> "Fehler"
        isOpened       -> "Offen"
        else           -> "Geschlossen"
    }
    val buttonsEnabled get() = !isLoading && deviceId != -1
}

@Composable
private fun ActionButtons(
    p: WidgetParams,
    buttonHeight: Int,
    modifier: GlanceModifier = GlanceModifier.fillMaxWidth()
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .defaultWeight()
                .height(buttonHeight.dp)
                .background(ColorProvider(Color(0xFF00C853)))
                .cornerRadius(8.dp)
                .then(
                    if (p.buttonsEnabled) GlanceModifier.clickable(
                        actionRunCallback<WidgetActionCallback>(
                            actionParametersOf(
                                WidgetActionCallback.KEY_DEVICE_ID    to p.deviceId,
                                WidgetActionCallback.KEY_STATUS_VALUE to DeviceStatus.OPENED.value
                            )
                        )
                    ) else GlanceModifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_open),
                contentDescription = "Öffnen",
                modifier = GlanceModifier.size(20.dp)
            )
        }
        Spacer(modifier = GlanceModifier.width(4.dp))
        Box(
            modifier = GlanceModifier
                .defaultWeight()
                .height(buttonHeight.dp)
                .background(ColorProvider(Color(0xFFE00F03)))
                .cornerRadius(8.dp)
                .then(
                    if (p.buttonsEnabled) GlanceModifier.clickable(
                        actionRunCallback<WidgetActionCallback>(
                            actionParametersOf(
                                WidgetActionCallback.KEY_DEVICE_ID    to p.deviceId,
                                WidgetActionCallback.KEY_STATUS_VALUE to DeviceStatus.NEUTRAL.value
                            )
                        )
                    ) else GlanceModifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_stop),
                contentDescription = "Stopp",
                modifier = GlanceModifier.size(20.dp)
            )
        }
        Spacer(modifier = GlanceModifier.width(4.dp))
        Box(
            modifier = GlanceModifier
                .defaultWeight()
                .height(buttonHeight.dp)
                .background(ColorProvider(Color(0xFFDC8F00)))
                .cornerRadius(8.dp)
                .then(
                    if (p.buttonsEnabled) GlanceModifier.clickable(
                        actionRunCallback<WidgetActionCallback>(
                            actionParametersOf(
                                WidgetActionCallback.KEY_DEVICE_ID    to p.deviceId,
                                WidgetActionCallback.KEY_STATUS_VALUE to DeviceStatus.CLOSED.value
                            )
                        )
                    ) else GlanceModifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_close),
                contentDescription = "Schließen",
                modifier = GlanceModifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun WidgetContentDefault(p: WidgetParams) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(GlanceTheme.colors.widgetBackground)
            .padding(12.dp),
        verticalAlignment = Alignment.Vertical.Top,
        horizontalAlignment = Alignment.Horizontal.Start
    ) {
        Text(
            text = p.deviceName,
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
        Text(
            text = p.statusText,
            style = TextStyle(
                color = GlanceTheme.colors.secondary,
                fontSize = 12.sp
            )
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        ActionButtons(p, buttonHeight = 44)
    }
}

@Composable
private fun WidgetContentWide(p: WidgetParams) {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(GlanceTheme.colors.widgetBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.Start
    ) {
        Column(
            modifier = GlanceModifier.wrapContentWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = p.deviceName,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
            Text(
                text = p.statusText,
                style = TextStyle(
                    color = GlanceTheme.colors.secondary,
                    fontSize = 11.sp
                )
            )
        }
        Spacer(modifier = GlanceModifier.width(40.dp))
        ActionButtons(p, buttonHeight = 36, modifier = GlanceModifier.defaultWeight())
    }
}
