package dev.kokorev.cryptoview.di

import android.app.Application
import dagger.Component
import dev.kokorev.cryptoview.App
import dev.kokorev.room_db.core.CoreProvidersFactory
import dev.kokorev.room_db.core_api.db.ContextProviderDB
import dev.kokorev.room_db.core_api.db.DbProvider

// Facade component for room db module
@Component(
    dependencies = [ContextProviderDB::class, DbProvider::class]
)
interface DbFacadeComponent {

    companion object {
        fun init(application: Application): DbFacadeComponent =
            DaggerDbFacadeComponent.builder()
                .contextProviderDB(ContextComponentDB.create(application))
                .dbProvider(CoreProvidersFactory.createDatabaseBuilder(ContextComponentDB.create(application)))
                .build()
    }

    fun inject(app: App)
}