package dev.kokorev.cryptoview.utils

import com.coinpaprika.apiclient.entity.CoinDetailsEntity
import com.coinpaprika.apiclient.entity.FavoriteCoinDB
import com.coinpaprika.apiclient.entity.MoverEntity
import com.coinpaprika.apiclient.entity.RecentCoinDB
import com.coinpaprika.apiclient.entity.TickerEntity
import dev.kokorev.binance_api.entity.BinanceSymbolDTO
import dev.kokorev.cryptoview.data.entity.FavoriteCoin
import dev.kokorev.cryptoview.data.entity.RecentCoin
import dev.kokorev.room_db.core_api.entity.BinanceSymbolDB
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTickerDB
import dev.kokorev.room_db.core_api.entity.TopMoverDB

object Converter {
    fun dtoToBinanceSymbol(dto: BinanceSymbolDTO) : BinanceSymbolDB {
        return BinanceSymbolDB(
            symbol = dto.symbol,
            status = dto.status,
            baseAsset = dto.baseAsset,
            quoteAsset = dto.quoteAsset)
    }

    fun dtoToTopMover(dto: MoverEntity): TopMoverDB {
        return TopMoverDB(
            symbol = dto.symbol,
            name = dto.name,
            coinPaprikaId = dto.id,
            percentChange = dto.percentChange
        )

    }

    fun dtoToCoinPaprikaTicker(dto: TickerEntity): CoinPaprikaTickerDB {
        return CoinPaprikaTickerDB(
            coinPaprikaId = dto.id,
            name = dto.name,
            symbol = dto.symbol,
            rank = dto.rank,
            price = dto.quotes?.get("USD")?.price,
            dailyVolume = dto.quotes?.get("USD")?.dailyVolume,
            marketCap = dto.quotes?.get("USD")?.marketCap,
            percentChange1h = dto.quotes?.get("USD")?.percentChange1h,
            percentChange24h = dto.quotes?.get("USD")?.percentChange24h,
            percentChange7d = dto.quotes?.get("USD")?.percentChange7d,
            percentChange30d = dto.quotes?.get("USD")?.percentChange30d,
            percentChange1y = dto.quotes?.get("USD")?.percentChange1y,
            athPrice = dto.quotes?.get("USD")?.athPrice,
            athDate = dto.quotes?.get("USD")?.athDate,
            percentFromPriceAth = dto.quotes?.get("USD")?.percentFromPriceAth,
        )

    }

    fun CoinDetailsEntityToFavoriteCoinDB(coin: CoinDetailsEntity): FavoriteCoinDB {
        return FavoriteCoinDB(
            coinPaprikaId = coin.id,
            name = coin.name,
            symbol = coin.symbol,
            rank = coin.rank,
            description = coin.description,
            logo = coin.logo,
            type = coin.type,
            openSource = coin.openSource,
            developmentStatus = coin.developmentStatus,
            hardwareWallet = coin.hardwareWallet,
            proofType = coin.proofType,
            organizationStructure = coin.organizationStructure,
            algorithm = coin.algorithm
        )
    }

    fun CoinDetailsEntityToRecentCoinDB(coin: CoinDetailsEntity): RecentCoinDB {
        return RecentCoinDB(
            coinPaprikaId = coin.id,
            name = coin.name,
            symbol = coin.symbol,
            rank = coin.rank,
            logo = coin.logo,
            type = coin.type,
            lastTime = System.currentTimeMillis()
        )
    }

    fun favoriteCoinDBToFavoriteCoin(db: FavoriteCoinDB, tikers: List<CoinPaprikaTickerDB>) : FavoriteCoin {
        val tiker = tikers.find { tiker -> tiker.coinPaprikaId == db.coinPaprikaId }
        return FavoriteCoin(
            id = db.id,
            coinPaprikaId = db.coinPaprikaId,
            name = db.name,
            symbol = db.symbol,
            rank = db.rank,
            logo = db.logo,
            type = db.type,
            price = tiker?.price,
            dailyVolume = tiker?.dailyVolume,
            marketCap = tiker?.marketCap,
            percentChange24h = tiker?.percentChange24h
        )
    }

    fun recentCoinDBToRecentCoin(db: RecentCoinDB, tikers: List<CoinPaprikaTickerDB>) : RecentCoin {
        val tiker = tikers.find { tiker -> tiker.coinPaprikaId == db.coinPaprikaId }
        return RecentCoin(
            id = db.id,
            coinPaprikaId = db.coinPaprikaId,
            name = db.name,
            symbol = db.symbol,
            rank = db.rank,
            logo = db.logo,
            type = db.type,
            lastTime = db.lastTime,
            price = tiker?.price,
            dailyVolume = tiker?.dailyVolume,
            marketCap = tiker?.marketCap,
            percentChange24h = tiker?.percentChange24h
        )
    }

}