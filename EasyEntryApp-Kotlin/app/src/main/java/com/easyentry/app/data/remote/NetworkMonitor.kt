package com.easyentry.app.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Meldet Wechsel des Standard-Netzwerks, etwa von Mobilfunk ins Heim-WLAN.
 *
 * Die ESPs sind nur im Heimnetz erreichbar. Wird die App geoeffnet, bevor das Telefon ins WLAN
 * gewechselt hat, sind alle Geraete zu Recht nicht erreichbar -- ohne diesen Ausloeser blieben
 * sie es aber auch nach dem Wechsel, bis jemand von Hand neu laedt.
 */
@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

    /** Emittiert bei jedem Wechsel, nicht aber fuer das beim Start bereits aktive Netz. */
    val defaultNetworkChanges: Flow<Unit> = callbackFlow<Network?> {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(network)
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }
        // Der Callback meldet das aktive Netz direkt nach der Registrierung. Mit diesem Netz als
        // Startwert fallen beide Meldungen durch distinctUntilChanged und drop(1) weg. Ohne Netz
        // ist der Startwert null, das erste WLAN zaehlt dann als Wechsel.
        .onStart { emit(connectivityManager.activeNetwork) }
        .distinctUntilChanged()
        .drop(1)
        .map { }
}
