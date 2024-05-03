package dev.kokorev.room_db.core_impl

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.kokorev.room_db.core_api.db.DbContract
import dev.kokorev.room_db.core_api.entity.BinanceSymbol
import dev.kokorev.room_db.core_api.entity.TopMover

@Database(entities = [BinanceSymbol::class, TopMover::class], version = 2, exportSchema = true)
abstract class CoinViewDb : RoomDatabase(), DbContract