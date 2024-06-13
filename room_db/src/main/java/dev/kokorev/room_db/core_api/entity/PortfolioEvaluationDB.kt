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
    @ColumnInfo(name = "valuation") var valuation: Double? = null,
    @ColumnInfo(name = "inflow") var inflow: Double? = null,
    @ColumnInfo(name = "change") var change: Double? = null,
    @ColumnInfo(name = "percent_change") var percentChange: Double? = null,
    @ColumnInfo(name = "cumulative_change") var cumulativeChange: Double? = null,
    @ColumnInfo(name = "cumulative_percent_change") var cumulativePercentChange: Double? = null,
    @ColumnInfo(name = "positions") var positions: Int? = null,
)