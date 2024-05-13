package com.coinpaprika.apiclient.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "recent_coin", indices = [Index(value = ["coin_paprika_id"], unique = true)])
data class RecentCoinDB(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "coin_paprika_id") val coinPaprikaId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "symbol") val symbol: String,
    @ColumnInfo(name = "rank") val rank: Int,
    @ColumnInfo(name = "logo") val logo: String?,
    @ColumnInfo(name = "type") val type: String?,
    @ColumnInfo(name = "last_time") val lastTime: Long,
)