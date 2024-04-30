package dev.kokorev.coin_paprika_api

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [CoinPaprikaModule::class]
)
interface CoinPaprikaComponent : CoinPaprikaProvider