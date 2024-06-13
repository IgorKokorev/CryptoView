package dev.kokorev.room_db.core_api.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.coinpaprika.apiclient.entity.PortfolioEvaluationDB
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.time.LocalDate

@Dao
interface PortfolioEvaluationDao {
    @Query("SELECT * FROM portfolio_evaluation ORDER BY date ASC")
    fun getAll(): Observable<List<PortfolioEvaluationDB>>
    
    @Query("SELECT * FROM portfolio_evaluation WHERE date >= :dateFrom ORDER BY date ASC")
    fun getLatest(dateFrom: LocalDate): Single<List<PortfolioEvaluationDB>>

    @Query("SELECT * FROM portfolio_evaluation ORDER BY date ASC")
    fun getAllSingle(): Single<List<PortfolioEvaluationDB>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPortfolioEvaluation(portfolioEvaluationDB: PortfolioEvaluationDB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<PortfolioEvaluationDB>)

    @Query("DELETE FROM portfolio_evaluation")
    fun deleteAll()

    @Query("DELETE FROM portfolio_evaluation WHERE id = :id")
    fun deleteById(id: Int)
    
    @Query("SELECT * FROM portfolio_evaluation WHERE date = :date")
    fun getEvaluationByDate(date: LocalDate): Maybe<PortfolioEvaluationDB>
}