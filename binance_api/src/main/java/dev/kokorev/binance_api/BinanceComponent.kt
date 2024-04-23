package dev.kokorev.binance_api

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [BinanceModule::class]
)
interface BinanceComponent : BinanceProvider