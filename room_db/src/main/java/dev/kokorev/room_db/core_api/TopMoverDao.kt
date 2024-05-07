package dev.kokorev.room_db.core_api

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.kokorev.room_db.core_api.entity.TopMover
import io.reactivex.rxjava3.core.Observable

@Dao
interface TopMoverDao {
    @Query("SELECT * FROM top_movers_cache")
    fun getAll(): Observable<List<TopMover>>

    @Insert
    fun insertTopMover(topMover: TopMover)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<TopMover>)

    @Query("DELETE FROM top_movers_cache")
    fun deleteAll()
}