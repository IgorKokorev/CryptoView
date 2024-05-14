package dev.kokorev.cryptoview.domain

import com.coinpaprika.apiclient.entity.FavoriteCoinDB
import com.coinpaprika.apiclient.entity.RecentCoinDB
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.data.PreferenceProvider
import dev.kokorev.room_db.core_api.BinanceSymbolDao
import dev.kokorev.room_db.core_api.entity.BinanceSymbolDB
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTickerDB
import dev.kokorev.room_db.core_api.entity.TopMoverDB
import java.util.concurrent.Executors

// Interactor to communicate with local db
class Repository(private val preferenceProvider: PreferenceProvider) {
    private val binanceSymbolDao: BinanceSymbolDao = App.instance.binanceSymbolDao
    private val topMoverDao = App.instance.topMoverDao
    private val coinPaprikaTickerDao = App.instance.coinPaprikaTickerDao
    private val favoriteCoinDao = App.instance.favoriteCoinDao
    private val recentCoinDao = App.instance.recentCoinDao

    // BinanceSymbol table interaction
    fun addBinanceSymbol(binanceSymbolDB: BinanceSymbolDB) = binanceSymbolDao.insertBinanceSymbol(binanceSymbolDB)
    fun addBinanceSymbols(list: List<BinanceSymbolDB>) = binanceSymbolDao.insertAll(list)
    fun getAllBinanceSymbols() = binanceSymbolDao.getBinanceSymbols()
    fun findBinanceSymbolsByBaseAsset(symbol: String) = binanceSymbolDao.findByBaseAsset(symbol)
    fun findBinanceSymbolsByQuoteAsset(symbol: String) = binanceSymbolDao.findByQuoteAsset(symbol)

    // TopMover table interaction
    fun getTopMovers() = topMoverDao.getAll()
    fun saveTopMovers(list: List<TopMoverDB>) {
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
    fun addCoinPaprikaTickers(list: List<CoinPaprikaTickerDB>) = coinPaprikaTickerDao.insertAll(list)
    fun findCoinPaprikaTickerBySymbol(symbol: String) = coinPaprikaTickerDao.findBySymbol(symbol)

    // FavoriteCoin table interaction
    fun getFavoriteCoins() = favoriteCoinDao.getAll()
    fun addFavorite(coinDB: FavoriteCoinDB) {
        Executors.newSingleThreadExecutor().execute {
            favoriteCoinDao.insertFavoriteCoin(coinDB)
        }
    }
    fun deleteFavorite(coinPaprikaId: String) {
        Executors.newSingleThreadExecutor().execute {
            favoriteCoinDao.deleteByCoinPaprikaId(coinPaprikaId)
        }
    }
    fun findFavoriteCoinByCoinPaprikaId(coinPaprikaId: String) = favoriteCoinDao.findByCoinPaprikaId(coinPaprikaId)

    // RecentCoin table interaction
    fun getRecentCoins() = recentCoinDao.getAll()
    fun addRecent(coinDB: RecentCoinDB) {
        Executors.newSingleThreadExecutor().execute {
            recentCoinDao.insertRecentCoin(coinDB)
        }
    }
    fun deleteAllRecents() {
        Executors.newSingleThreadExecutor().execute {
            recentCoinDao.deleteAll()
        }
    }

    fun deleteOldRecents(time: Long) {
        Executors.newSingleThreadExecutor().execute {
            recentCoinDao.deleteOld(time)
        }
    }

    // Shared Preference interaction
    fun getLastTopMoversCallTime() = preferenceProvider.getLastTopMoversCallTime()
    fun saveLastTopMoversCallTime() = preferenceProvider.saveLastTopMoversCallTime()
    fun getLastCpTickersCallTime() = preferenceProvider.getLastCpTickersCallTime()
    fun saveLastCpTickersCallTime() = preferenceProvider.saveLastCpTickersCallTime()
    fun getLastAppUpdateTime() = preferenceProvider.getLastAppUpdateTime()
    fun saveLastAppUpdateTime() = preferenceProvider.saveLastAppUpdateTime()


}