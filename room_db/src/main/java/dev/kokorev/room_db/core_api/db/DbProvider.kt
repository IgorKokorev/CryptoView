package dev.kokorev.room_db.core_api.db

import dev.kokorev.room_db.core_api.dao.BinanceSymbolDao
import dev.kokorev.room_db.core_api.dao.CoinPaprikaTickerDao
import dev.kokorev.room_db.core_api.dao.FavoriteCoinDao
import dev.kokorev.room_db.core_api.dao.MessageDao
import dev.kokorev.room_db.core_api.dao.PortfolioEvaluationDao
import dev.kokorev.room_db.core_api.dao.PortfolioPositionDao
import dev.kokorev.room_db.core_api.dao.PortfolioTransactionDao
import dev.kokorev.room_db.core_api.dao.RecentCoinDao
import dev.kokorev.room_db.core_api.dao.TopMoverDao

interface DbProvider {
    fun binanceSymbolDao(): BinanceSymbolDao
    fun topMoverDao(): TopMoverDao
    fun coinPaprikaTickerDao(): CoinPaprikaTickerDao
    fun favoriteCoinDao(): FavoriteCoinDao
    fun portfolioPositionDao(): PortfolioPositionDao
    fun portfolioEvaluationDao(): PortfolioEvaluationDao
    fun portfolioTransactionDao(): PortfolioTransactionDao
    fun recentCoinDao(): RecentCoinDao
    fun messageDao(): MessageDao

}