package dev.kokorev.room_db.core_impl

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dev.kokorev.room_db.core_api.BinanceSymbolDao
import dev.kokorev.room_db.core_api.CoinPaprikaTickerDao
import dev.kokorev.room_db.core_api.FavoriteCoinDao
import dev.kokorev.room_db.core_api.MessageDao
import dev.kokorev.room_db.core_api.PortfolioCoinDao
import dev.kokorev.room_db.core_api.RecentCoinDao
import dev.kokorev.room_db.core_api.TopMoverDao
import dev.kokorev.room_db.core_api.db.DbContract
import javax.inject.Singleton

private const val DATABASE_NAME = "coin_view_db"

@Module
class DatabaseModule {
    @Provides
    @Singleton
    fun provideBinanceSymbolDao(databaseContract: DbContract): BinanceSymbolDao {
        return databaseContract.binanceSymbolDao()
    }

    @Provides
    @Singleton
    fun provideTopMoverDao(databaseContract: DbContract): TopMoverDao {
        return databaseContract.topMoverDao()
    }

    @Provides
    @Singleton
    fun provideCoinPaprikaTickerDao(databaseContract: DbContract): CoinPaprikaTickerDao {
        return databaseContract.coinPaprikaTickerDao()
    }

    @Provides
    @Singleton
    fun provideFavoriteCoinDao(databaseContract: DbContract): FavoriteCoinDao {
        return databaseContract.favoriteCoinDao()
    }

    @Provides
    @Singleton
    fun providePortfolioCoinDao(databaseContract: DbContract): PortfolioCoinDao {
        return databaseContract.portfolioCoinDao()
    }

    @Provides
    @Singleton
    fun provideRecentCoinDao(databaseContract: DbContract): RecentCoinDao {
        return databaseContract.recentCoinDao()
    }

    @Provides
    @Singleton
    fun provideMessageDao(databaseContract: DbContract): MessageDao {
        return databaseContract.messageDao()
    }

    @Provides
    @Singleton
    fun provideItemsDatabase(context: Context): DbContract {
        return Room.databaseBuilder(
            context,
            CoinViewDb::class.java, DATABASE_NAME
        )
/*            .setQueryCallback(
                { sqlQuery, bindArgs ->
                    Log.d(this.javaClass.simpleName, "SQL Query: $sqlQuery SQL Args: $bindArgs")
                }, Executors.newSingleThreadExecutor()
            )*/
            .build()
    }
}

