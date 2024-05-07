package dev.kokorev.room_db.core_api.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "top_movers_cache", indices = [Index(value = ["symbol", "coin_paprika_id"], unique = true)])
data class TopMover(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "symbol") var symbol: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "coin_paprika_id") var coinPaprikaId: String,
    @ColumnInfo(name = "percent_change") var percentChange: Double
)
