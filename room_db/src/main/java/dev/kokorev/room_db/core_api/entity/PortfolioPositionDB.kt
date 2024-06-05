package com.coinpaprika.apiclient.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio_coin", indices = [Index(value = ["coin_paprika_id"], unique = true)])
data class PortfolioPositionDB(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "coin_paprika_id") var coinPaprikaId: String,
    @ColumnInfo(name = "name") var name: String = "",
    @ColumnInfo(name = "symbol") var symbol: String = "",
    @ColumnInfo(name = "logo") var logo: String? = null,
    @ColumnInfo(name = "time_open") var timeOpen: Long = 0L,
    @ColumnInfo(name = "quantity") var quantity: Double = 0.0,
    @ColumnInfo(name = "price_open") var priceOpen: Double = 0.0,
    @ColumnInfo(name = "time_last_evaluation") var timeLastEvaluation: Long = 0L,
    @ColumnInfo(name = "price_last_evaluation") var priceLastEvaluation: Double = 0.0,
)