package dev.kokorev.room_db.core_api

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.coinpaprika.apiclient.entity.PortfolioCoinDB
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

@Dao
interface PortfolioCoinDao {
    @Query("SELECT * FROM portfolio_coin")
    fun getAll(): Observable<List<PortfolioCoinDB>>

    @Query("SELECT * FROM portfolio_coin")
    fun getAllSingle(): Single<List<PortfolioCoinDB>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPortfolioCoin(PortfolioCoinDB: PortfolioCoinDB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<PortfolioCoinDB>)

    @Query("DELETE FROM portfolio_coin")
    fun deleteAll()

    @Query("DELETE FROM portfolio_coin WHERE id = :id")
    fun deleteById(id: Int)

    @Query("DELETE FROM portfolio_coin WHERE coin_paprika_id = :coinPaprikaId")
    fun deleteByCoinPaprikaId(coinPaprikaId: String)

    @Query("SELECT * FROM portfolio_coin WHERE coin_paprika_id = :coinPaprikaId")
    fun findByCoinPaprikaId(coinPaprikaId: String): Maybe<PortfolioCoinDB>

}