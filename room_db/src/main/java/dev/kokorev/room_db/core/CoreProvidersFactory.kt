package dev.kokorev.room_db.core

import dev.kokorev.room_db.core_api.db.ContextProviderDB
import dev.kokorev.room_db.core_api.db.DbProvider
import dev.kokorev.room_db.core_impl.DaggerDatabaseComponent

object CoreProvidersFactory {
    fun createDatabaseBuilder(contextProviderDB: ContextProviderDB): DbProvider {
        return DaggerDatabaseComponent.builder()
            .contextProviderDB(contextProviderDB)
            .build()
    }
}