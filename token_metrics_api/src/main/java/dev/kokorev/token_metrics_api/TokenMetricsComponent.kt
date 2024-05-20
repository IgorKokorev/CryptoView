package dev.kokorev.token_metrics_api

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [TokenMetricsModule::class]
)
interface TokenMetricsComponent : TokenMetricsProvider