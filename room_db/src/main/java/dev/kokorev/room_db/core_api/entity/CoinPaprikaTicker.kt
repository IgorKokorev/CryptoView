package dev.kokorev.room_db.core_api.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "coin_paprika_ticker", indices = [Index(value = ["coin_paprika_id"], unique = true)])
data class CoinPaprikaTicker(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "coin_paprika_id") var coinPaprikaId: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "symbol") var symbol: String,
    @ColumnInfo(name = "rank") var rank: Int = -1,

    @ColumnInfo(name = "price") val price: Double?,
    @ColumnInfo(name = "volume_24h") val dailyVolume: Double?,
    @ColumnInfo(name = "market_cap") val marketCap: Double?,
    @ColumnInfo(name = "percent_change_1h") val percentChange1h: Double?,
    @ColumnInfo(name = "percent_change_24h") val percentChange24h: Double?,
    @ColumnInfo(name = "percent_change_7d") val percentChange7d: Double?,
    @ColumnInfo(name = "percent_change_30d") val percentChange30d: Double?,
    @ColumnInfo(name = "percent_change_1y") val percentChange1y: Double?,
    @ColumnInfo(name = "ath_price") val athPrice: Double?,
    @ColumnInfo(name = "ath_date") val athDate: String? = "",
    @ColumnInfo(name = "percent_from_price_ath") val percentFromPriceAth: Double?
)
