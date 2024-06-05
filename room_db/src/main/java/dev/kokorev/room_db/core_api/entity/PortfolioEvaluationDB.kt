package com.coinpaprika.apiclient.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "portfolio_evaluation", indices = [Index(value = ["date"], unique = true)])
data class PortfolioEvaluationDB(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "date") var date: LocalDate,
    @ColumnInfo(name = "valuation") var valuation: Double? = 0.0,
    @ColumnInfo(name = "inflow") var inflow: Double? = 0.0,
    @ColumnInfo(name = "change") var change: Double? = 0.0,
    @ColumnInfo(name = "percent_change") var percentChange: Double? = 0.0,
    @ColumnInfo(name = "positions") var positions: Int? = 0,
)