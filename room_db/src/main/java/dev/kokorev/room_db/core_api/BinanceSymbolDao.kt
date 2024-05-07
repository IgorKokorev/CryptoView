package dev.kokorev.room_db.core_api

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.kokorev.room_db.core_api.entity.BinanceSymbol
import io.reactivex.rxjava3.core.Observable

@Dao
interface BinanceSymbolDao {
    @Query("SELECT * FROM binance_symbol")
    fun getBinanceSymbols(): Observable<List<BinanceSymbol>>

    @Query("SELECT * FROM binance_symbol WHERE base_asset = :asset")
    fun findByBaseAsset(asset: String): Observable<List<BinanceSymbol>>

    @Insert
    fun insertBinanceSymbol(binanceSymbol: BinanceSymbol)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<BinanceSymbol>)

    @Query("DELETE FROM binance_symbol")
    fun deleteAll()
}