package com.easyentry.app

import android.app.Application
import com.easyentry.app.data.remote.NetworkDebugController
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class EasyEntryApplication : Application() {

    /**
     * Wird nur injiziert, damit der Controller existiert und den Debug-Flag beobachtet.
     * Ohne diese Referenz wuerde Hilt ihn nie erzeugen.
     */
    @Inject
    lateinit var networkDebugController: NetworkDebugController
}
