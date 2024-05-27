package dev.kokorev.room_db.core_api

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTickerDB
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable

@Dao
interface CoinPaprikaTickerDao {
    @Query("SELECT * FROM coin_paprika_ticker ORDER BY rank")
    fun getCoinPaprikaTickers(): Observable<List<CoinPaprikaTickerDB>>

    @Query("SELECT * FROM coin_paprika_ticker WHERE market_cap >= :minMcap AND volume_24h >= :minVol ORDER BY rank")
    fun getCoinPaprikaTickersFiltered(minMcap: Long, minVol: Long): Observable<List<CoinPaprikaTickerDB>>

    @Query("SELECT * FROM coin_paprika_ticker WHERE market_cap >= :minMcap AND volume_24h >= :minVol ORDER BY :field ASC LIMIT :limit")
    fun getCoinPaprikaTickersSortedAsc(minMcap: Long, minVol: Long, field: String, limit: Int): Observable<List<CoinPaprikaTickerDB>>

    @Query("SELECT * FROM coin_paprika_ticker WHERE market_cap >= :minMcap AND volume_24h >= :minVol ORDER BY :field DESC LIMIT :limit")
    fun getCoinPaprikaTickersSortedDesc(minMcap: Long, minVol: Long, field: String, limit: Int): Observable<List<CoinPaprikaTickerDB>>

    @Query("SELECT * FROM coin_paprika_ticker WHERE symbol = :symbol")
    fun findBySymbol(symbol: String): Observable<List<CoinPaprikaTickerDB>>

    @Query("SELECT * FROM coin_paprika_ticker WHERE coin_paprika_id = :cpId")
    fun findById(cpId: String): Maybe<CoinPaprikaTickerDB>

    @Insert
    fun insertCoinPaprikaTicker(coinPaprikaTickerDB: CoinPaprikaTickerDB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<CoinPaprikaTickerDB>)

    @Query("DELETE FROM coin_paprika_ticker")
    fun deleteAll()

    @Transaction
    fun updateAll(list: List<CoinPaprikaTickerDB>) {
        deleteAll()
        insertAll(list)
    }
}