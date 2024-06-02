package com.coinpaprika.apiclient.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_coin", indices = [Index(value = ["coin_paprika_id"], unique = true)])
data class FavoriteCoinDB(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "coin_paprika_id") val coinPaprikaId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "symbol") val symbol: String,
    @ColumnInfo(name = "rank") val rank: Int,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "logo") val logo: String?,
    @ColumnInfo(name = "type") val type: String?,
    @ColumnInfo(name = "open_source") val openSource: Boolean?,
    @ColumnInfo(name = "development_status") val developmentStatus: String?,
    @ColumnInfo(name = "hardware_wallet") val hardwareWallet: Boolean?,
    @ColumnInfo(name = "proof_type") val proofType: String?,
    @ColumnInfo(name = "org_structure") val organizationStructure: String?,
    @ColumnInfo(name = "hash_algorithm") val algorithm: String?,
    @ColumnInfo(name = "time_notified", defaultValue = "0") val timeNotified: Long = 0L,
)