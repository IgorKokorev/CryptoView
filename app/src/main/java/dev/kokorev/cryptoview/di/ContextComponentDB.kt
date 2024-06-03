package dev.kokorev.cryptoview.di

import android.app.Application
import android.content.Context
import dagger.BindsInstance
import dagger.Component
import dev.kokorev.room_db.core_api.db.ContextProviderDB
import javax.inject.Singleton

// Component to provide context to room db module
@Singleton
@Component
interface ContextComponentDB: ContextProviderDB {
    companion object {
        private var contextComponent: ContextProviderDB? = null
        fun create(application: Application): ContextProviderDB {
            return contextComponent ?: DaggerContextComponentDB
                .builder()
                .application(application.applicationContext)
                .build().also {
                    contextComponent = it
                }
        }
    }

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(context: Context): Builder
        fun build(): ContextComponentDB
    }
}