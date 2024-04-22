package dev.kokorev.cryptoview.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import dev.kokorev.binance_api.BinanceApi
import dev.kokorev.cryptoview.domain.Interactor
import javax.inject.Singleton

@Module
class DomainModule(val context: Context) {
    @Provides
    fun provideContext() = context

/*    @Singleton
    @Provides
    fun providePreferences(context: Context) = PreferenceProvider(context)*/

    @Singleton
    @Provides
    fun provideInteractor(binanceApi: BinanceApi) =
        Interactor(binanceApi = binanceApi)

}