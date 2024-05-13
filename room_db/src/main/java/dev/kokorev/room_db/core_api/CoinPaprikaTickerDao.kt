package dev.kokorev.room_db.core_api

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTickerDB
import io.reactivex.rxjava3.core.Observable

@Dao
interface CoinPaprikaTickerDao {
    @Query("SELECT * FROM coin_paprika_ticker")
    fun getCoinPaprikaTickers(): Observable<List<CoinPaprikaTickerDB>>

    @Query("SELECT * FROM coin_paprika_ticker WHERE symbol = :symbol")
    fun findBySymbol(symbol: String): Observable<List<CoinPaprikaTickerDB>>

    @Insert
    fun insertCoinPaprikaTicker(coinPaprikaTickerDB: CoinPaprikaTickerDB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<CoinPaprikaTickerDB>)

    @Query("DELETE FROM coin_paprika_ticker")
    fun deleteAll()
}