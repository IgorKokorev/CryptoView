package dev.kokorev.cryptoview.di

import dagger.Component
import dev.kokorev.binance_api.BinanceProvider
import dev.kokorev.cmc_api.CmcProvider
import dev.kokorev.coin_paprika_api.CoinPaprikaProvider
import dev.kokorev.cryptoview.di.modules.DomainModule
import dev.kokorev.cryptoview.viewModel.ActivityViewModel
import dev.kokorev.cryptoview.viewModel.ChartViewModel
import dev.kokorev.cryptoview.viewModel.FavoritesViewModel
import dev.kokorev.cryptoview.viewModel.InfoViewModel
import dev.kokorev.cryptoview.viewModel.MainViewModel
import dev.kokorev.cryptoview.viewModel.SearchViewModel
import dev.kokorev.cryptoview.viewModel.SettingsViewModel
import javax.inject.Singleton

@Singleton
@Component(
    dependencies = [
        BinanceProvider::class,
        CoinPaprikaProvider::class,
        CmcProvider::class
    ],
    modules = [DomainModule::class]
)
interface AppComponent {
    fun inject(activityViewModel: ActivityViewModel)
    fun inject(mainViewModel: MainViewModel)
    fun inject(favoritesViewModel: FavoritesViewModel)
    fun inject(settingsViewModel: SettingsViewModel)
    fun inject(chartViewModel: ChartViewModel)
    fun inject(infoViewModel: InfoViewModel)
    fun inject(searchViewModel: SearchViewModel)
}