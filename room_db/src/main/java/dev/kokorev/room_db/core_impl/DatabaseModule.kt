package dev.kokorev.room_db.core_impl

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dev.kokorev.room_db.core_api.dao.BinanceSymbolDao
import dev.kokorev.room_db.core_api.dao.CoinPaprikaTickerDao
import dev.kokorev.room_db.core_api.dao.FavoriteCoinDao
import dev.kokorev.room_db.core_api.dao.MessageDao
import dev.kokorev.room_db.core_api.dao.PortfolioEvaluationDao
import dev.kokorev.room_db.core_api.dao.PortfolioPositionDao
import dev.kokorev.room_db.core_api.dao.PortfolioTransactionDao
import dev.kokorev.room_db.core_api.dao.RecentCoinDao
import dev.kokorev.room_db.core_api.dao.TopMoverDao
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
    fun providePortfolioPositionDao(databaseContract: DbContract): PortfolioPositionDao {
        return databaseContract.portfolioPositionDao()
    }
    
    @Provides
    @Singleton
    fun providePortfolioEvaluationDao(databaseContract: DbContract): PortfolioEvaluationDao {
        return databaseContract.portfolioEvaluationDao()
    }
    
    @Provides
    @Singleton
    fun providePortfolioTransactionDao(databaseContract: DbContract): PortfolioTransactionDao {
        return databaseContract.portfolioTransactionDao()
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

