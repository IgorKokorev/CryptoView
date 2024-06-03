package dev.kokorev.cryptoview.domain

import android.util.Log
import com.coinpaprika.apiclient.entity.FavoriteCoinDB
import com.coinpaprika.apiclient.entity.MessageDB
import com.coinpaprika.apiclient.entity.MessageType
import com.coinpaprika.apiclient.entity.PortfolioCoinDB
import com.coinpaprika.apiclient.entity.RecentCoinDB
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.data.entity.FavoriteCoin
import dev.kokorev.room_db.core_api.BinanceSymbolDao
import dev.kokorev.room_db.core_api.entity.BinanceSymbolDB
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTickerDB
import dev.kokorev.room_db.core_api.entity.TopMoverDB
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.Executors

// Interactor to communicate with local db
class Repository() {
    private val binanceSymbolDao: BinanceSymbolDao = App.instance.binanceSymbolDao
    private val topMoverDao = App.instance.topMoverDao
    private val coinPaprikaTickerDao = App.instance.coinPaprikaTickerDao
    private val favoriteCoinDao = App.instance.favoriteCoinDao
    private val portfolioCoinDao = App.instance.portfolioCoinDao
    private val recentCoinDao = App.instance.recentCoinDao
    private val messageDao = App.instance.messageDao

    // BinanceSymbol table interaction
    fun saveBinanceSymbol(binanceSymbolDB: BinanceSymbolDB) {
        Executors.newSingleThreadExecutor().execute {
            binanceSymbolDao.insertBinanceSymbol(binanceSymbolDB)
        }
    }

    fun saveBinanceSymbols(list: List<BinanceSymbolDB>) {
        Executors.newSingleThreadExecutor().execute {
            binanceSymbolDao.insertAll(list)
        }
    }
    fun getAllBinanceSymbols() =
        binanceSymbolDao.getBinanceSymbols().addSettings()

    fun findBinanceSymbolsByBaseAsset(symbol: String) =
        binanceSymbolDao.findByBaseAsset(symbol).addSettings()

    fun findBinanceSymbolsByQuoteAsset(symbol: String) =
        binanceSymbolDao.findByQuoteAsset(symbol).addSettings()

    // TopMover table interaction
    fun getTopMovers() = topMoverDao.getAll().addSettings()
    fun saveTopMovers(list: List<TopMoverDB>) {
        Executors.newSingleThreadExecutor().execute {
            topMoverDao.updateAll(list)
        }
    }

    // CoinPaprikaTicker table interaction
    fun getAllCoinPaprikaTickers() =
        coinPaprikaTickerDao.getCoinPaprikaTickers().addSettings()

    fun getCPGainers(minMcap: Long, minVol: Long, field: String = "percent_change_24h", limit: Int = 5): Observable<List<CoinPaprikaTickerDB>> =
        coinPaprikaTickerDao.getCoinPaprikaTickersSortedDesc(
            minMcap = minMcap,
            minVol = minVol,
            field = field,
            limit = limit
        ).addSettings()

    fun getCPLosers(minMcap: Long, minVol: Long, field: String = "percent_change_24h", limit: Int = 5): Observable<List<CoinPaprikaTickerDB>> =
        coinPaprikaTickerDao.getCoinPaprikaTickersSortedAsc(
            minMcap = minMcap,
            minVol = minVol,
            field = field,
            limit = limit
        ).addSettings()

    fun getAllCoinPaprikaTickersFiltered(minMcap: Long, minVol: Long) =
        coinPaprikaTickerDao.getCoinPaprikaTickersFiltered(minMcap, minVol).addSettings()

    fun getCPTickerById(cpId: String) = coinPaprikaTickerDao.findById(cpId).addSettings()

    fun saveCoinPaprikaTickers(list: List<CoinPaprikaTickerDB>) {
        Executors.newSingleThreadExecutor().execute {
            coinPaprikaTickerDao.updateAll(list)
        }
    }

    fun findCoinPaprikaTickerBySymbol(symbol: String) =
        coinPaprikaTickerDao.findBySymbol(symbol).addSettings()

    // FavoriteCoin table interaction
    fun getFavoriteCoins() = favoriteCoinDao.getAll().addSettings()
    fun getFavoriteCoinsSingle() = favoriteCoinDao.getAllSingle().addSettings()
    fun saveFavorite(coinDB: FavoriteCoinDB) {
        Executors.newSingleThreadExecutor().execute {
            favoriteCoinDao.insertFavoriteCoin(coinDB)
        }
    }

    fun deleteFavorite(coinPaprikaId: String) {
        Executors.newSingleThreadExecutor().execute {
            favoriteCoinDao.deleteByCoinPaprikaId(coinPaprikaId)
        }
    }

    fun findFavoriteCoinByCoinPaprikaId(coinPaprikaId: String) =
        favoriteCoinDao.findByCoinPaprikaId(coinPaprikaId).addSettings()

    fun setFavoriteTimeNotified(coin: FavoriteCoin) {
        Executors.newSingleThreadExecutor().execute {
            favoriteCoinDao.updateTimeNotified(coin.id, System.currentTimeMillis())
        }
    }


    // Portfolio coins
    fun getPortfolioPositionByCPId(cpId: String) = portfolioCoinDao.findByCoinPaprikaId(cpId).addSettings()
    fun savePortfolioPosition(portfolioCoinDB: PortfolioCoinDB) {
        Executors.newSingleThreadExecutor().execute {
            portfolioCoinDao.insertPortfolioCoin(portfolioCoinDB)
        }
    }
    fun getAllPortfolioPositions(): Observable<List<PortfolioCoinDB>> = portfolioCoinDao.getAll().addSettings()
    fun deletePortfolioPosition(id: Int) {
        Executors.newSingleThreadExecutor().execute {
            portfolioCoinDao.deleteById(id)
        }
    }


    // RecentCoin table interaction
    fun getRecentCoins() = recentCoinDao.getAll().addSettings()
    fun saveRecent(coinDB: RecentCoinDB) {
        Executors.newSingleThreadExecutor().execute {
            recentCoinDao.insertRecentCoin(coinDB)
        }
    }

    fun deleteAllRecentCoins() {
        Executors.newSingleThreadExecutor().execute {
            recentCoinDao.deleteAll()
        }
    }

    fun deleteOldRecentCoins(time: Long) {
        Executors.newSingleThreadExecutor().execute {
            recentCoinDao.deleteOld(time)
        }
    }



    // Ai chat q&a
    fun saveQuestion(name: String, message: String) {
        val messageDB: MessageDB = MessageDB(
            time = System.currentTimeMillis(),
            type = MessageType.OUT,
            name = name,
            message = message
        )
        saveMessage(messageDB)
    }

    fun saveAnswer(name: String, message: String) {
        val messageDB: MessageDB = MessageDB(
            time = System.currentTimeMillis(),
            type = MessageType.IN,
            name = name,
            message = message
        )
        saveMessage(messageDB)
    }

    fun saveMessage(messageDB: MessageDB) {
        Executors.newSingleThreadExecutor().execute {
            messageDao.insertMessage(messageDB)
        }
    }

    fun getNewMessages(): Observable<List<MessageDB>> {
        val time = System.currentTimeMillis() - Constants.CHAT_SHOW_TIME
        return messageDao.getNewMessages(time).addSettings()
    }



    // common settings for all local db request to get some data
    private fun <T : Any> Observable<T>.addSettings(): Observable<T> {
        return this
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorComplete {
                Log.d("Repository", "Error calling local db: ${it.localizedMessage}")
                true
            }
    }
    private fun <T : Any> Single<T>.addSettings(): Single<T> {
        return this
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError {
                Log.d(this.javaClass.simpleName, "Error calling local db: ${it.localizedMessage}")
            }
    }
    private fun <T : Any> Maybe<T>.addSettings(): Maybe<T> {
        return this
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError {
                Log.d(this.javaClass.simpleName, "Error calling local db: ${it.localizedMessage}")
            }
    }


}