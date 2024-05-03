package dev.kokorev.cryptoview.utils

import com.coinpaprika.apiclient.entity.MoverEntity
import dev.kokorev.binance_api.entity.BinanceSymbolDTO
import dev.kokorev.room_db.core_api.entity.BinanceSymbol
import dev.kokorev.room_db.core_api.entity.TopMover

object ConvertData {
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
}