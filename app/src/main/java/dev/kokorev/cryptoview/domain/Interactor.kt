package dev.kokorev.cryptoview.domain

import com.coinpaprika.apiclient.entity.CoinDetailsEntity
import com.coinpaprika.apiclient.entity.TopMoversEntity
import dev.kokorev.binance_api.BinanceApi
import dev.kokorev.binance_api.entity.Binance24hrStatsDTO
import dev.kokorev.binance_api.entity.BinanceAvgPriceDTO
import dev.kokorev.binance_api.entity.BinanceExchangeInfoDTO
import dev.kokorev.cmc_api.CmcApi
import dev.kokorev.cmc_api.entity.cmc_listing.CmcListingDTO
import dev.kokorev.cmc_api.entity.cmc_metadata.CmcMetadataDTO
import dev.kokorev.coin_paprika_api.CoinPaprikaApi
import io.reactivex.rxjava3.core.Observable

class Interactor(val binanceApi: BinanceApi, val cmcApi: CmcApi, val coinPaprikaApi: CoinPaprikaApi) {

    fun getBinanceInfo() : Observable<BinanceExchangeInfoDTO> = binanceApi.getExchangeInfo()

    fun getBinanceCurrentAvgPrice(symbol: String) : Observable<BinanceAvgPriceDTO> = binanceApi.getCurrentAvgPrice(symbol)

    fun getBinance24hrstats(symbol: String) : Observable<Binance24hrStatsDTO> = binanceApi.get24hrstats(symbol)

    fun getCmcMetadata(symbol: String) : Observable<CmcMetadataDTO> = cmcApi.getMetadata(symbol)

    fun getCmcListingLatest() : Observable<CmcListingDTO> = cmcApi.getListingLatest()

    fun getCoinPaprikaTop10Movers() : Observable<TopMoversEntity> = coinPaprikaApi.getTop10Movers()

    fun getCoinPaprikaCoinInfo(id: String): Observable<CoinDetailsEntity> = coinPaprikaApi.getCoin(id)
}