package dev.kokorev.cryptoview.domain

import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.data.PreferenceProvider
import dev.kokorev.room_db.core_api.BinanceSymbolDao
import dev.kokorev.room_db.core_api.entity.BinanceSymbol
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTicker
import dev.kokorev.room_db.core_api.entity.TopMover
import java.util.concurrent.Executors

// Interactor to communicate with local db
class Repository(val preferenceProvider: PreferenceProvider) {
    val binanceSymbolDao: BinanceSymbolDao = App.instance.binanceSymbolDao
    val topMoverDao = App.instance.topMoverDao
    val coinPaprikaTickerDao = App.instance.coinPaprikaTickerDao

    // BinanceSymbol table interaction
    fun addBinanceSymbol(binanceSymbol: BinanceSymbol) = binanceSymbolDao.insertBinanceSymbol(binanceSymbol)
    fun addBinanceSymbols(list: List<BinanceSymbol>) = binanceSymbolDao.insertAll(list)
    fun getAllBinanceSymbols() = binanceSymbolDao.getBinanceSymbols()
    fun findBinanceSymbolsByBaseAsset(symbol: String) = binanceSymbolDao.findByBaseAsset(symbol)
    fun findBinanceSymbolsByQuoteAsset(symbol: String) = binanceSymbolDao.findByQuoteAsset(symbol)

    // TopMover table interaction
    fun getTopMovers() = topMoverDao.getAll()
    fun saveTopMovers(list: List<TopMover>) {
        Executors.newSingleThreadExecutor().execute {
            topMoverDao.deleteAll()
            topMoverDao.insertAll(list)
        }
    }
    fun clearTopMovers() {
        Executors.newSingleThreadExecutor().execute {
            topMoverDao.deleteAll()
        }
    }

    // CoinPaprikaTicker table interaction
    fun getAllCoinPaprikaTickers() = coinPaprikaTickerDao.getCoinPaprikaTickers()
    fun addCoinPaprikaTickers( list: List<CoinPaprikaTicker>) = coinPaprikaTickerDao.insertAll(list)
    fun findCoinPaprikaTickerBySymbol(symbol: String) = coinPaprikaTickerDao.findBySymbol(symbol)


    // Shared Preference interaction
    fun getLastTopMoversCallTime() = preferenceProvider.getLastTopMoversCallTime()
    fun saveLastTopMoversCallTime() = preferenceProvider.saveLastTopMoversCallTime()
    fun getLastCpTickersCallTime() = preferenceProvider.getLastCpTickersCallTime()
    fun saveLastCpTickersCallTime() = preferenceProvider.saveLastCpTickersCallTime()
    fun setLastAppUpdateTime() = preferenceProvider.setLastAppUpdateTime()
    fun getLastAppUpdateTime() = preferenceProvider.getLastAppUpdateTime()


}