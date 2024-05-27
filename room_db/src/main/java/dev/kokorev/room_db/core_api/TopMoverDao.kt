package dev.kokorev.room_db.core_api

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.kokorev.room_db.core_api.entity.TopMoverDB
import io.reactivex.rxjava3.core.Observable

@Dao
interface TopMoverDao {
    @Query("SELECT * FROM top_movers_cache")
    fun getAll(): Observable<List<TopMoverDB>>

    @Insert
    fun insertTopMover(topMoverDB: TopMoverDB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<TopMoverDB>)

    @Query("DELETE FROM top_movers_cache")
    fun deleteAll()

    @Transaction
    fun updateAll(list: List<TopMoverDB>) {
        deleteAll()
        insertAll(list)
    }
}