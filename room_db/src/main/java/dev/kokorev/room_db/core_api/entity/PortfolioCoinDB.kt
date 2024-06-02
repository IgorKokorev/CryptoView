package com.coinpaprika.apiclient.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio_coin", indices = [Index(value = ["coin_paprika_id"], unique = true)])
data class PortfolioCoinDB(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "coin_paprika_id") var coinPaprikaId: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "symbol") var symbol: String,
    @ColumnInfo(name = "logo") var logo: String?,
    @ColumnInfo(name = "time_open") var timeOpen: Long,
    @ColumnInfo(name = "quantity") var quantity: Double,
    @ColumnInfo(name = "price_open") var priceOpen: Double,
    @ColumnInfo(name = "time_last_evaluation") var timeLastEvaluation: Long,
    @ColumnInfo(name = "price_last_evaluation") var priceLastEvaluation: Double,
)