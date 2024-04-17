package dev.kokorev.cryptoview

import android.app.Application
import android.util.Log
import dev.kokorev.cryptoview.di.AppComponent

class App : Application() {
    lateinit var dagger: AppComponent

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Creating dagger component
/*
        dagger = DaggerAppComponent.builder()
            .remoteProvider(DaggerRemoteComponent.create())
            .databaseModule(DatabaseModule())
            .domainModule(DomainModule(this))
            .build()
*/

    }

    companion object {
        lateinit var instance: App
            private set
    }
}
