package dev.kokorev.room_db.core_api

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.kokorev.room_db.core_api.entity.BinanceSymbolDB
import io.reactivex.rxjava3.core.Observable

@Dao
interface BinanceSymbolDao {
    @Query("SELECT * FROM binance_symbol")
    fun getBinanceSymbols(): Observable<List<BinanceSymbolDB>>

    @Query("""SELECT * FROM binance_symbol WHERE status = 'TRADING' AND base_asset = :asset""")
    fun findByBaseAsset(asset: String): Observable<List<BinanceSymbolDB>>

    @Query("SELECT * FROM binance_symbol WHERE status = 'TRADING' AND quote_asset = :asset")
    fun findByQuoteAsset(asset: String): Observable<List<BinanceSymbolDB>>

    @Insert
    fun insertBinanceSymbol(binanceSymbolDB: BinanceSymbolDB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<BinanceSymbolDB>)

    @Query("DELETE FROM binance_symbol")
    fun deleteAll()
}