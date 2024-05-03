package dev.kokorev.room_db.core_api.db

import dev.kokorev.room_db.core_api.BinanceSymbolDao
import dev.kokorev.room_db.core_api.TopMoverDao

interface DbProvider {
    fun binanceSymbolDao(): BinanceSymbolDao
    fun topMoverDao(): TopMoverDao
}