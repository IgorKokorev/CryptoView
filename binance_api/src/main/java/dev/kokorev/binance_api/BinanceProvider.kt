package dev.kokorev.binance_api

interface BinanceProvider {
    fun provideBinance() : BinanceApi
}