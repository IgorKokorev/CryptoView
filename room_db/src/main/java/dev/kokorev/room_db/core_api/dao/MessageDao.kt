package dev.kokorev.room_db.core_api.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.coinpaprika.apiclient.entity.MessageDB
import io.reactivex.rxjava3.core.Observable

@Dao
interface MessageDao {
    @Query("SELECT * FROM message")
    fun getAll(): Observable<List<MessageDB>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(messageDB: MessageDB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<MessageDB>)

    @Query("DELETE FROM message")
    fun deleteAll()

    @Query("DELETE FROM message WHERE time < :time")
    fun deleteOld(time: Long)

    @Query("SELECT * FROM message WHERE time > :time")
    fun getNewMessages(time: Long): Observable<List<MessageDB>>
}