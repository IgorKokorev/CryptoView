package com.coinpaprika.apiclient.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "portfolio_transaction")
data class PortfolioTransactionDB(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "coin_paprika_id") var coinPaprikaId: String,
    @ColumnInfo(name = "time") var time: Instant,
    @ColumnInfo(name = "price") var price: Double,
    @ColumnInfo(name = "quantity") var quantity: Double,
)