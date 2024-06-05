package dev.kokorev.room_db.core_api.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.coinpaprika.apiclient.entity.RecentCoinDB
import io.reactivex.rxjava3.core.Observable

@Dao
interface RecentCoinDao {
    @Query("SELECT * FROM recent_coin")
    fun getAll(): Observable<List<RecentCoinDB>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecentCoin(recentCoinDB: RecentCoinDB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<RecentCoinDB>)

    @Query("DELETE FROM recent_coin")
    fun deleteAll()

    @Query("SELECT * FROM recent_coin WHERE coin_paprika_id = :coinPaprikaId")
    fun findByCoinPaprikaId(coinPaprikaId: String): Observable<RecentCoinDB>

    @Query("DELETE FROM recent_coin WHERE last_time < :time")
    fun deleteOld(time: Long)
}