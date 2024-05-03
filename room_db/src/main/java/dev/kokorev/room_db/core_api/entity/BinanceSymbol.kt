package dev.kokorev.room_db.core_api.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "binance_symbol")
data class BinanceSymbol(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "symbol") var symbol: String,
    @ColumnInfo(name = "status") var status: String,
    @ColumnInfo(name = "base_asset") var baseAsset: String,
    @ColumnInfo(name = "quote_asset") var quoteAsset: String
)
