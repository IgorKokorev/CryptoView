package dev.kokorev.room_db.core_api

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.coinpaprika.apiclient.entity.FavoriteCoin
import io.reactivex.rxjava3.core.Observable

@Dao
interface FavoriteCoinDao {
    @Query("SELECT * FROM favorite_coin")
    fun getAll(): Observable<List<FavoriteCoin>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavoriteCoin(favoriteCoin: FavoriteCoin)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<FavoriteCoin>)

    @Query("DELETE FROM favorite_coin")
    fun deleteAll()

    @Query("DELETE FROM favorite_coin WHERE id = :id")
    fun deleteById(id: Int)
}