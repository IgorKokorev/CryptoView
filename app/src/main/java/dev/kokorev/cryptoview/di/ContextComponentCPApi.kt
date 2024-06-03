/*
package dev.kokorev.cryptoview.di

import android.app.Application
import android.content.Context
import dagger.BindsInstance
import dagger.Component
import dev.kokorev.coin_paprika_api.ContextProviderCPApi
import javax.inject.Singleton

// Component to provide context to room db module
@Singleton
@Component
interface ContextComponentCPApi: ContextProviderCPApi {
    companion object {
        private var contextComponentCPApi: ContextProviderCPApi? = null
        fun create(application: Application): ContextProviderCPApi {
            return contextComponentCPApi ?: DaggerContextComponentCPApi
                .builder()
                .application(application.applicationContext)
                .build().also {
                    contextComponentCPApi = it
                }
        }
    }

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(context: Context): Builder
        fun build(): ContextComponentCPApi
    }
}*/
