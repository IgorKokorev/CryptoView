package dev.kokorev.cryptoview.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import dev.kokorev.binance_api.BinanceApi
import dev.kokorev.cmc_api.CmcApi
import dev.kokorev.coin_paprika_api.CoinPaprikaApi
import dev.kokorev.cryptoview.backgroundService.NotificationService
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
import dev.kokorev.cryptoview.utils.CacheManager
import dev.kokorev.token_metrics_api.TokenMetricsApi
import javax.inject.Singleton

@Module
class DomainModule(val context: Context) {
    @Provides
    fun provideContext() = context
/*
    @Singleton
    @Provides
    fun providePreferences(context: Context) = PreferenceProvider(context)*/

    @Singleton
    @Provides
    fun provideCache(context: Context) = CacheManager(context)

    @Singleton
    @Provides
    fun provideNotificationService(context: Context) = NotificationService(context)

    @Singleton
    @Provides
    fun provideRemoteApi(
        binanceApi: BinanceApi,
        cmcApi: CmcApi,
        coinPaprikaApi: CoinPaprikaApi,
        tokenMetricsApi: TokenMetricsApi
    ) =
        RemoteApi(
            context = context,
            binanceApi = binanceApi,
            cmcApi = cmcApi,
            coinPaprikaApi = coinPaprikaApi,
            tokenMetricsApi = tokenMetricsApi
        )

    @Singleton
    @Provides
    fun provideRepository() = Repository()

}