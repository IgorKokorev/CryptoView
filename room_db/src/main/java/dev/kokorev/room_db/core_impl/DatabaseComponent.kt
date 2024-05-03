package dev.kokorev.room_db.core_impl

import dagger.Component
import dev.kokorev.room_db.core_api.db.ContextProvider
import dev.kokorev.room_db.core_api.db.DbProvider
import javax.inject.Singleton

@Singleton
@Component(
    dependencies = [ContextProvider::class],
    modules = [DatabaseModule::class]
)
interface DatabaseComponent : DbProvider