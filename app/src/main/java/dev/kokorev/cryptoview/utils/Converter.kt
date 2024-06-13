package dev.kokorev.cryptoview.utils

import android.util.Log
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.HighLowDataEntry
import com.coinpaprika.apiclient.entity.CoinDetailsEntity
import com.coinpaprika.apiclient.entity.FavoriteCoinDB
import com.coinpaprika.apiclient.entity.MoverEntity
import com.coinpaprika.apiclient.entity.PortfolioPositionDB
import com.coinpaprika.apiclient.entity.RecentCoinDB
import com.coinpaprika.apiclient.entity.TickerEntity
import dev.kokorev.binance_api.entity.BinanceSymbolDTO
import dev.kokorev.cryptoview.data.entity.FavoriteCoin
import dev.kokorev.cryptoview.data.entity.GainerCoin
import dev.kokorev.cryptoview.data.entity.RecentCoin
import dev.kokorev.cryptoview.logd
import dev.kokorev.cryptoview.views.fragments.MainPriceSorting
import dev.kokorev.room_db.core_api.entity.BinanceSymbolDB
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTickerDB
import dev.kokorev.room_db.core_api.entity.TopMoverDB
import dev.kokorev.token_metrics_api.entity.TMMarketMetrics

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
            algorithm = coin.algorithm,
            timeNotified = System.currentTimeMillis()
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
        val tiker = tikers.find { ticker -> ticker.coinPaprikaId == db.coinPaprikaId }
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
            percentChange = tiker?.percentChange24h,
            timeNotified = db.timeNotified,
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
            percentChange = tiker?.percentChange24h
        )
    }

    fun cpTickerDBToGainerCoin(ticker: CoinPaprikaTickerDB, sorting: MainPriceSorting): GainerCoin {
        return GainerCoin(
            id = ticker.id,
            coinPaprikaId = ticker.coinPaprikaId,
            name = ticker.name,
            symbol = ticker.symbol,
            rank = ticker.rank,
            price = ticker.price,
            dailyVolume = ticker.dailyVolume,
            marketCap = ticker.marketCap,
            percentChange = when (sorting) {
                MainPriceSorting.H1 -> ticker.percentChange1h
                MainPriceSorting.H24 -> ticker.percentChange24h
                MainPriceSorting.D7 -> ticker.percentChange7d
                MainPriceSorting.D30 -> ticker.percentChange30d
                MainPriceSorting.Y1 -> ticker.percentChange1y
                MainPriceSorting.ATH -> ticker.percentFromPriceAth
            }
        )
    }

    fun createPortfolioPosition(coin: CoinDetailsEntity, price: Double, qty: Double): PortfolioPositionDB {
        val currentTime = System.currentTimeMillis()
        return PortfolioPositionDB(
            coinPaprikaId = coin.id,
            name = coin.name,
            symbol = coin.symbol,
            logo = coin.logo,
            timeOpen = currentTime,
            quantity = qty,
            priceOpen = price,
            timeLastEvaluation = currentTime,
            priceLastEvaluation = price
        )
    }

    fun binanceKLineToOHLCData(kline: ArrayList<Any>): OHLCDataEntry {
        val closeTime = try {
            (kline.get(6) as Double).toLong() + 1
        } catch (e: Exception) {
            Log.d(
                this.javaClass.simpleName,
                "Error converting Binance kline: ${kline.joinToString()}"
            )
            null
        }
        val openPrice = try {
            (kline.get(1) as String).toDouble()
        } catch (e: Exception) {
            Log.d(
                this.javaClass.simpleName,
                "Error converting Binance kline: ${kline.joinToString()}"
            )
            null
        }
        val highPrice = try {
            (kline.get(2) as String).toDouble()
        } catch (e: Exception) {
            Log.d(
                this.javaClass.simpleName,
                "Error converting Binance kline: ${kline.joinToString()}"
            )
            null
        }
        val lowPrice = try {
            (kline.get(3) as String).toDouble()
        } catch (e: Exception) {
            Log.d(
                this.javaClass.simpleName,
                "Error converting Binance kline: ${kline.joinToString()}"
            )
            null
        }
        val closePrice = try {
            (kline.get(4) as String).toDouble()
        } catch (e: Exception) {
            Log.d(
                this.javaClass.simpleName,
                "Error converting Binance kline: ${kline.joinToString()}"
            )
            null
        }
        val volume = try {
            (kline.get(5) as String).toDouble()
        } catch (e: Exception) {
            Log.d(
                this.javaClass.simpleName,
                "Error converting Binance kline: ${kline.joinToString()}"
            )
            null
        }
        
        return OHLCDataEntry(
            closeTime,
            openPrice,
            highPrice,
            lowPrice,
            closePrice,
            volume
        )
    }
    
    fun tmMarketMetricsToDataEntry(tmMarketMetrics: TMMarketMetrics, divisor: Double): DataEntry {
        val dataEntry = DataEntry()
        dataEntry.setValue("x", tmMarketMetrics.date)
        dataEntry.setValue("value", (tmMarketMetrics.totalCryptoMcap ?: 0.0) / divisor)
        return dataEntry
    }
    
}

class OHLCDataEntry(x: Long?, open: Double?, high: Double?, low: Double?, close: Double?, volume: Double?) :
    HighLowDataEntry(x, high, low) {
    init {
        setValue("open", open)
        setValue("close", close)
        setValue("volume", volume)
    }
}