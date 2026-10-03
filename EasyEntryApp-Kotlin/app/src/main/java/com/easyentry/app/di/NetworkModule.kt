package com.easyentry.app.di

import com.easyentry.app.BuildConfig
import android.os.SystemClock
import com.easyentry.app.data.remote.EspNetworkConfig
import com.easyentry.app.data.remote.MonotonicClock
import com.easyentry.app.data.remote.api.EspApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideEspNetworkConfig(): EspNetworkConfig = EspNetworkConfig(
        // Deckt Aufweckzeit des ESP, ARP und TCP-Retransmits ab. Kuerzer wuerde die
        // Fehlerrate erhoehen, nicht senken.
        connectTimeoutMs = 3_000,
        // handleGet serialisiert rund 60 Byte JSON. Ein langer Read entsteht nur, weil
        // loop() gerade blockiert ist.
        readTimeoutMs = 4_000,
        writeTimeoutMs = 4_000,
        // Gesamtdeckel pro Call. Umfasst auch die Wartezeit in der Dispatcher-Queue, die
        // durch maxRequestsPerHost = 1 entstehen kann.
        callTimeoutMs = 10_000,
        probeAttempts = 3,
        backoffMs = longArrayOf(0, 300, 900),
    )

    @Provides
    @Singleton
    fun provideMonotonicClock(): MonotonicClock = MonotonicClock { SystemClock.elapsedRealtime() }

    /**
     * Eigener Provider, damit das Level zur Laufzeit umschaltbar bleibt: der OkHttpClient ist
     * ein Singleton und kann nicht neu gebaut werden, aber [HttpLoggingInterceptor.level] ist
     * eine var. Siehe NetworkDebugController.
     */
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

    @Provides
    @Singleton
    fun provideEspOkHttpClient(
        config: EspNetworkConfig,
        logging: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(config.connectTimeoutMs, TimeUnit.MILLISECONDS)
        .readTimeout(config.readTimeoutMs, TimeUnit.MILLISECONDS)
        .writeTimeout(config.writeTimeoutMs, TimeUnit.MILLISECONDS)
        .callTimeout(config.callTimeoutMs, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(true)
        // OkHttp-Default ist 5 Minuten Idle. Der ESP-WebServer verwirft eine ungenutzte
        // Keep-Alive-Verbindung aber schon nach wenigen Sekunden; ein Request auf so einem
        // halb-toten Socket laeuft dann in den Read-Timeout. 2 Sekunden sind kuerzer als
        // jedes ESP-Timeout und behalten den Vorteil innerhalb einer Probe-Runde.
        .connectionPool(ConnectionPool(5, 2, TimeUnit.SECONDS))
        // Der ESP-Sketch bedient mit handleClient() in loop() strikt eine Verbindung zur
        // Zeit. Verschiedene Geraete sind verschiedene Hosts und laufen weiter parallel.
        .dispatcher(Dispatcher().apply { maxRequestsPerHost = 1 })
        .addInterceptor(logging)
        .build()

    @Provides
    @Singleton
    fun provideEspApi(client: OkHttpClient, moshi: Moshi): EspApi =
        Retrofit.Builder()
            .baseUrl("http://localhost/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(EspApi::class.java)
}
