package dev.kokorev.room_db.core_impl

import androidx.room.Database
import androidx.room.RoomDatabase
import com.coinpaprika.apiclient.entity.FavoriteCoinDB
import com.coinpaprika.apiclient.entity.MessageDB
import com.coinpaprika.apiclient.entity.RecentCoinDB
import dev.kokorev.room_db.core_api.db.DbContract
import dev.kokorev.room_db.core_api.entity.BinanceSymbolDB
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTickerDB
import dev.kokorev.room_db.core_api.entity.TopMoverDB

@Database(
    entities = [
        BinanceSymbolDB::class,
        TopMoverDB::class,
        CoinPaprikaTickerDB::class,
        FavoriteCoinDB::class,
        RecentCoinDB::class,
        MessageDB::class,
    ], version = 1, exportSchema = true
)
abstract class CoinViewDb : RoomDatabase(), DbContract