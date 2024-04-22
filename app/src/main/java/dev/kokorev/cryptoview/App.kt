package dev.kokorev.cryptoview

import android.app.Application
import android.util.Log
import dev.kokorev.binance_api.DaggerBinanceComponent
import dev.kokorev.cryptoview.di.AppComponent
import dev.kokorev.cryptoview.di.DaggerAppComponent
import dev.kokorev.cryptoview.di.modules.DomainModule

class App : Application() {
    lateinit var dagger: AppComponent

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Creating dagger component
        dagger = DaggerAppComponent.builder()
            .binanceProvider(DaggerBinanceComponent.create())
            .domainModule(DomainModule(this))
            .build()

    }

    companion object {
        lateinit var instance: App
            private set
    }
}
