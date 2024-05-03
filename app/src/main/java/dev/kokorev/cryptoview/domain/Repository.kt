package dev.kokorev.cryptoview.domain

import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.data.PreferenceProvider
import dev.kokorev.room_db.core_api.BinanceSymbolDao
import dev.kokorev.room_db.core_api.entity.BinanceSymbol
import dev.kokorev.room_db.core_api.entity.TopMover
import java.util.concurrent.Executors

// Interactor to communicate with local db
class Repository(val preferenceProvider: PreferenceProvider) {
    val binanceSymbolDao: BinanceSymbolDao = App.instance.binanceSymbolDao
    val topMoverDao = App.instance.topMoverDao


    fun addBinanceSymbol(binanceSymbol: BinanceSymbol) {
        binanceSymbolDao.insertBinanceSymbol(binanceSymbol)
    }

    fun addBinanceSymbols(list: List<BinanceSymbol>) {
        binanceSymbolDao.insertAll(list)
    }

    fun getAllSymbols() = binanceSymbolDao.getBinanceSymbols()

    fun getTopMovers() = topMoverDao.getAll()

    fun saveTopMovers(list: List<TopMover>) {
        topMoverDao.insertAll(list)
    }

    fun getLastTopMoversCallTime() = preferenceProvider.getLastTopMoversCallTime()

    fun saveLastTopMoversCallTime() = preferenceProvider.saveLastTopMoversCallTime()
    fun clearTopMovers() {
        Executors.newSingleThreadExecutor().execute {
            topMoverDao.deleteAll()
        }
    }
}