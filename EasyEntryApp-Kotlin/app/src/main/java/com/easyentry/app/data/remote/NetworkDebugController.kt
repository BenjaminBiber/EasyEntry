package com.easyentry.app.data.remote

import com.easyentry.app.BuildConfig
import com.easyentry.app.data.repository.SettingRepository
import com.easyentry.app.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schaltet das HTTP-Logging zur Laufzeit um.
 *
 * Der OkHttpClient ist ein Singleton und kann nicht neu gebaut werden, aber
 * [HttpLoggingInterceptor.level] ist eine var. Damit laesst sich die Diagnose auch in einem
 * Release-Build einschalten, ohne die App neu zu starten.
 *
 * Level bewusst HEADERS und nicht BODY: Statuscodes und der Connection-Header genuegen, um
 * veraltete Keep-Alive-Verbindungen zu erkennen, und es landen keine Payloads im Log.
 */
@Singleton
class NetworkDebugController @Inject constructor(
    private val logging: HttpLoggingInterceptor,
    settingRepository: SettingRepository,
    @ApplicationScope scope: CoroutineScope,
) {
    init {
        settingRepository.networkDebugLog
            .onEach { enabled ->
                logging.level = when {
                    enabled -> HttpLoggingInterceptor.Level.HEADERS
                    // Ausgeschaltet heisst: zurueck auf den Build-Standard, damit ein
                    // Debug-Build sein BODY-Logging behaelt.
                    BuildConfig.DEBUG -> HttpLoggingInterceptor.Level.BODY
                    else -> HttpLoggingInterceptor.Level.NONE
                }
            }
            .launchIn(scope)
    }
}
