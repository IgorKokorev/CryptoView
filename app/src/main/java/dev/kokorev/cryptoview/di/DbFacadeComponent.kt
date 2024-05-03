package dev.kokorev.cryptoview.di

import android.app.Application
import dagger.Component
import dev.kokorev.cryptoview.App
import dev.kokorev.room_db.core.CoreProvidersFactory
import dev.kokorev.room_db.core_api.db.ContextProvider
import dev.kokorev.room_db.core_api.db.DbProvider

// Facade component for room db module
@Component(
    dependencies = [ContextProvider::class, DbProvider::class]
)
interface DbFacadeComponent {

    companion object {
        fun init(application: Application): DbFacadeComponent =
            DaggerDbFacadeComponent.builder()
                .contextProvider(ContextComponent.create(application))
                .dbProvider(CoreProvidersFactory.createDatabaseBuilder(ContextComponent.create(application)))
                .build()
    }

    fun inject(app: App)
}