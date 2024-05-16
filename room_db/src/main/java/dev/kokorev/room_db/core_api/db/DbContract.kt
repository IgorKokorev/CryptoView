package dev.kokorev.room_db.core_api.db

import dev.kokorev.room_db.core_api.BinanceSymbolDao
import dev.kokorev.room_db.core_api.CoinPaprikaTickerDao
import dev.kokorev.room_db.core_api.FavoriteCoinDao
import dev.kokorev.room_db.core_api.MessageDao
import dev.kokorev.room_db.core_api.RecentCoinDao
import dev.kokorev.room_db.core_api.TopMoverDao

interface DbContract {
    fun binanceSymbolDao(): BinanceSymbolDao
    fun topMoverDao(): TopMoverDao
    fun coinPaprikaTickerDao(): CoinPaprikaTickerDao
    fun favoriteCoinDao(): FavoriteCoinDao
    fun recentCoinDao(): RecentCoinDao
    fun messageDao(): MessageDao

}