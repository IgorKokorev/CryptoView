/*
package dev.kokorev.cryptoview.di

import android.app.Application
import dagger.Component
import dev.kokorev.coin_paprika_api.CoinPaprikaProvider
import dev.kokorev.coin_paprika_api.CoinPaprikaProvidersFactory
import dev.kokorev.coin_paprika_api.ContextProviderCPApi
import dev.kokorev.cryptoview.App

// Facade component for cp api module
@Component(
    dependencies = [ContextProviderCPApi::class, CoinPaprikaProvider::class]
)
interface CpApiFacadeComponent {

    companion object {
        fun init(application: Application): CpApiFacadeComponent =
            DaggerCpApiFacadeComponent.builder()
                .contextProvider(ContextComponentCPApi.create(application))
                .coinPaprikaProvider(CoinPaprikaProvidersFactory.createApiBuilder(ContextComponentCPApi.create(application)))
                .build()
    }

    fun inject(app: App)
}*/
