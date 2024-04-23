package dev.kokorev.cryptoview.domain

import dev.kokorev.binance_api.BinanceApi
import dev.kokorev.binance_api.entity.Binance24hrStatsDTO
import dev.kokorev.binance_api.entity.BinanceAvgPriceDTO
import dev.kokorev.binance_api.entity.BinanceExchangeInfoDTO
import io.reactivex.rxjava3.core.Observable

class Interactor(val binanceApi: BinanceApi) {

    fun getBinanceInfo() : Observable<BinanceExchangeInfoDTO> {
        return binanceApi.getExchangeInfo()
    }

    fun getBinanceCurrentAvgPrice(symbol: String) : Observable<BinanceAvgPriceDTO> {
        return binanceApi.getCurrentAvgPrice(symbol)
    }

    fun getBinance24hrstats(symbol: String) : Observable<Binance24hrStatsDTO> {
        return binanceApi.get24hrstats(symbol)
    }
}