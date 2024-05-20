package dev.kokorev.token_metrics_api

interface TokenMetricsProvider {
    fun provideCoinPaprikaApi() : TokenMetricsApi
}