package dev.kokorev.cryptoview.domain

import com.coinpaprika.apiclient.entity.CoinDetailsEntity
import com.coinpaprika.apiclient.entity.TopMoversEntity
import dev.kokorev.binance_api.BinanceApi
import dev.kokorev.binance_api.entity.Binance24hrStatsDTO
import dev.kokorev.binance_api.entity.Binance24hrStatsType
import dev.kokorev.binance_api.entity.BinanceAvgPriceDTO
import dev.kokorev.binance_api.entity.BinanceExchangeInfoDTO
import dev.kokorev.cmc_api.CmcApi
import dev.kokorev.cmc_api.entity.cmc_listing.CmcListingDTO
import dev.kokorev.cmc_api.entity.cmc_metadata.CmcMetadataDTO
import dev.kokorev.coin_paprika_api.CoinPaprikaApi
import io.reactivex.rxjava3.core.Observable

// Interactor to communicate with remote apis
class RemoteApi(val binanceApi: BinanceApi, val cmcApi: CmcApi, val coinPaprikaApi: CoinPaprikaApi) {

    // Binance API info
    fun getBinanceInfo() : Observable<BinanceExchangeInfoDTO> = binanceApi.getExchangeInfo()

    fun getBinanceCurrentAvgPrice(symbol: String) : Observable<BinanceAvgPriceDTO> = binanceApi.getCurrentAvgPrice(symbol)

    fun getBinance24hrstats(symbol: String, type: Binance24hrStatsType = Binance24hrStatsType.FULL) : Observable<Binance24hrStatsDTO> = binanceApi.get24hrStats(symbol, type)

    fun getBinance24hrstatsAll(type: Binance24hrStatsType = Binance24hrStatsType.MINI) : Observable<List<Binance24hrStatsDTO>> = binanceApi.get24hrStatsAll(type)


    // CoinMarketCap API info
    fun getCmcMetadata(symbol: String) : Observable<CmcMetadataDTO> = cmcApi.getMetadata(symbol)

    fun getCmcListingLatest() : Observable<CmcListingDTO> = cmcApi.getListingLatest()


    // CoinPaprika API info
    fun getCoinPaprikaTop10Movers() : Observable<TopMoversEntity> = coinPaprikaApi.getTop10Movers()

    fun getCoinPaprikaCoinInfo(id: String): Observable<CoinDetailsEntity> = coinPaprikaApi.getCoin(id)

    fun getCoinPaprikaTicker(id: String) = coinPaprikaApi.getTicker(id)

    fun getCoinPaprikaTickerHistorical(id: String) = coinPaprikaApi.getTickerHistoricalTicks(id)
}