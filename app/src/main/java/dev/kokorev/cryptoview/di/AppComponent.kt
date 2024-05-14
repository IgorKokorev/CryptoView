package dev.kokorev.cryptoview.di

import dagger.Component
import dev.kokorev.binance_api.BinanceProvider
import dev.kokorev.cmc_api.CmcProvider
import dev.kokorev.coin_paprika_api.CoinPaprikaProvider
import dev.kokorev.cryptoview.di.modules.DomainModule
import dev.kokorev.cryptoview.viewModel.ActivityViewModel
import dev.kokorev.cryptoview.viewModel.AiChatViewModel
import dev.kokorev.cryptoview.viewModel.CoinViewModel
import dev.kokorev.cryptoview.viewModel.MainViewModel
import dev.kokorev.cryptoview.viewModel.SavedViewModel
import dev.kokorev.cryptoview.viewModel.SearchViewModel
import dev.kokorev.cryptoview.viewModel.SettingsViewModel
import dev.kokorev.token_metrics_api.TokenMetricsProvider
import javax.inject.Singleton

@Singleton
@Component(
    dependencies = [
        BinanceProvider::class,
        CoinPaprikaProvider::class,
        CmcProvider::class,
        TokenMetricsProvider::class,
    ],
    modules = [DomainModule::class]
)
interface AppComponent {
    fun inject(activityViewModel: ActivityViewModel)
    fun inject(mainViewModel: MainViewModel)
    fun inject(savedViewModel: SavedViewModel)
    fun inject(settingsViewModel: SettingsViewModel)
    fun inject(searchViewModel: SearchViewModel)
    fun inject(coinViewModel: CoinViewModel)
    fun inject(aiChatViewModel: AiChatViewModel)
}