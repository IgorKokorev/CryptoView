package dev.kokorev.room_db.core_api.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.coinpaprika.apiclient.entity.PortfolioTransactionDB
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.time.Instant

@Dao
interface PortfolioTransactionDao {
    @Query("SELECT * FROM portfolio_transaction ORDER BY time DESC")
    fun getAll(): Observable<List<PortfolioTransactionDB>>

    @Query("SELECT * FROM portfolio_transaction ORDER BY time DESC")
    fun getAllSingle(): Single<List<PortfolioTransactionDB>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPortfolioTransaction(PortfolioTransactionDB: PortfolioTransactionDB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<PortfolioTransactionDB>)

    @Query("DELETE FROM portfolio_transaction")
    fun deleteAll()

    @Query("DELETE FROM portfolio_transaction WHERE id = :id")
    fun deleteById(id: Int)

    @Query("SELECT * FROM portfolio_transaction WHERE time > :instant ORDER BY time DESC")
    fun findTransactionsFrom(instant: Instant): Maybe<List<PortfolioTransactionDB>>
    
    @Query("SELECT * FROM portfolio_transaction WHERE coin_paprika_id = :coinPaprikaId")
    fun getTransactionByCPId(coinPaprikaId: String): Maybe<List<PortfolioTransactionDB>>
}