package dev.kokorev.room_db.core_api.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.coinpaprika.apiclient.entity.PortfolioPositionDB
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable

@Dao
interface PortfolioPositionDao {
    @Query("SELECT * FROM portfolio_coin")
    fun getAll(): Observable<List<PortfolioPositionDB>>

    @Query("SELECT * FROM portfolio_coin")
    fun getAllMaybe(): Maybe<List<PortfolioPositionDB>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPortfolioCoin(PortfolioPositionDB: PortfolioPositionDB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<PortfolioPositionDB>)

    @Query("DELETE FROM portfolio_coin")
    fun deleteAll()

    @Query("DELETE FROM portfolio_coin WHERE id = :id")
    fun deleteById(id: Int)

    @Query("DELETE FROM portfolio_coin WHERE coin_paprika_id = :coinPaprikaId")
    fun deleteByCoinPaprikaId(coinPaprikaId: String)

    @Query("SELECT * FROM portfolio_coin WHERE coin_paprika_id = :coinPaprikaId")
    fun findByCoinPaprikaId(coinPaprikaId: String): Maybe<PortfolioPositionDB>

}