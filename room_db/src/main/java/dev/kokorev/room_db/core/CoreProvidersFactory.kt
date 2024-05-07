package dev.kokorev.room_db.core

import dev.kokorev.room_db.core_api.db.ContextProvider
import dev.kokorev.room_db.core_api.db.DbProvider
import dev.kokorev.room_db.core_impl.DaggerDatabaseComponent

object CoreProvidersFactory {
    fun createDatabaseBuilder(contextProvider: ContextProvider): DbProvider {
        return DaggerDatabaseComponent.builder()
            .contextProvider(contextProvider)
            .build()
    }
}