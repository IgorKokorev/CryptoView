package dev.kokorev.cryptoview.di

import android.app.Application
import android.content.Context
import dagger.BindsInstance
import dagger.Component
import dev.kokorev.room_db.core_api.db.ContextProvider
import javax.inject.Singleton

// Component to provide context to room db module
@Singleton
@Component
interface ContextComponent: ContextProvider {
    companion object {
        private var contextComponent: ContextProvider? = null
        fun create(application: Application): ContextProvider {
            return contextComponent ?: DaggerContextComponent
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
        fun build(): ContextComponent
    }
}