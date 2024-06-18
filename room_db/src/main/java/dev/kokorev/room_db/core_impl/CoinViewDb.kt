package dev.kokorev.room_db.core_impl

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.coinpaprika.apiclient.entity.FavoriteCoinDB
import com.coinpaprika.apiclient.entity.MessageDB
import com.coinpaprika.apiclient.entity.PortfolioEvaluationDB
import com.coinpaprika.apiclient.entity.PortfolioPositionDB
import com.coinpaprika.apiclient.entity.PortfolioTransactionDB
import com.coinpaprika.apiclient.entity.RecentCoinDB
import dev.kokorev.room_db.core_api.db.DbContract
import dev.kokorev.room_db.core_api.entity.BinanceSymbolDB
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTickerDB
import dev.kokorev.room_db.core_api.entity.DBTypeConverter
import dev.kokorev.room_db.core_api.entity.TopMoverDB

@Database(
    entities = [
        BinanceSymbolDB::class,
        TopMoverDB::class,
        CoinPaprikaTickerDB::class,
        FavoriteCoinDB::class,
        PortfolioPositionDB::class,
        PortfolioEvaluationDB::class,
        PortfolioTransactionDB::class,
        RecentCoinDB::class,
        MessageDB::class,
    ],
    version = 8,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
    ]
)
@TypeConverters(DBTypeConverter::class)
abstract class CoinViewDb : RoomDatabase(), DbContract