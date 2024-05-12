package dev.kokorev.cryptoview.utils

import com.coinpaprika.apiclient.entity.CoinDetailsEntity
import com.coinpaprika.apiclient.entity.FavoriteCoin
import com.coinpaprika.apiclient.entity.MoverEntity
import com.coinpaprika.apiclient.entity.TickerEntity
import dev.kokorev.binance_api.entity.BinanceSymbolDTO
import dev.kokorev.room_db.core_api.entity.BinanceSymbol
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTicker
import dev.kokorev.room_db.core_api.entity.TopMover

object Converter {
    fun dtoToBinanceSymbol(dto: BinanceSymbolDTO) : BinanceSymbol {
        return BinanceSymbol(
            symbol = dto.symbol,
            status = dto.status,
            baseAsset = dto.baseAsset,
            quoteAsset = dto.quoteAsset)
    }

    fun dtoToTopMover(dto: MoverEntity): TopMover {
        return TopMover(
            symbol = dto.symbol,
            name = dto.name,
            coinPaprikaId = dto.id,
            percentChange = dto.percentChange
        )

    }

    fun dtoToCoinPaprikaTicker(dto: TickerEntity): CoinPaprikaTicker {
        return CoinPaprikaTicker(
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

    fun CoinDetailsEntityToFavoriteCoin(coin: CoinDetailsEntity): FavoriteCoin {
        return FavoriteCoin(
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
}