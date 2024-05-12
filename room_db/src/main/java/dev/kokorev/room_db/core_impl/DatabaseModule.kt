package dev.kokorev.room_db.core_impl

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dev.kokorev.room_db.core_api.BinanceSymbolDao
import dev.kokorev.room_db.core_api.CoinPaprikaTickerDao
import dev.kokorev.room_db.core_api.FavoriteCoinDao
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
    fun provideItemsDatabase(context: Context): DbContract {
        return Room.databaseBuilder(
            context,
            CoinViewDb::class.java, DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}