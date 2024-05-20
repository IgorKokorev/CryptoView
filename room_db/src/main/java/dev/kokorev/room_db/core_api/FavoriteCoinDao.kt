package dev.kokorev.room_db.core_api

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.coinpaprika.apiclient.entity.FavoriteCoinDB
import io.reactivex.rxjava3.core.Observable

@Dao
interface FavoriteCoinDao {
    @Query("SELECT * FROM favorite_coin")
    fun getAll(): Observable<List<FavoriteCoinDB>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavoriteCoin(favoriteCoinDB: FavoriteCoinDB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<FavoriteCoinDB>)

    @Query("DELETE FROM favorite_coin")
    fun deleteAll()

    @Query("DELETE FROM favorite_coin WHERE id = :id")
    fun deleteById(id: Int)

    @Query("DELETE FROM favorite_coin WHERE coin_paprika_id = :coinPaprikaId")
    fun deleteByCoinPaprikaId(coinPaprikaId: String)

    @Query("SELECT * FROM favorite_coin WHERE coin_paprika_id = :coinPaprikaId")
    fun findByCoinPaprikaId(coinPaprikaId: String): Observable<FavoriteCoinDB>
}