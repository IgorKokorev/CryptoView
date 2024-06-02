package dev.kokorev.cryptoview.data.entity

data class GainerCoin(
    val id: Int = 0,
    val coinPaprikaId: String,
    val name: String,
    val symbol: String,
    val rank: Int,
    val price: Double?,
    val dailyVolume: Double?,
    val marketCap: Double?,
    val percentChange: Double?,
)