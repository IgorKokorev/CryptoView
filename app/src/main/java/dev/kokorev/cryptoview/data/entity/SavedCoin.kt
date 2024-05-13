package dev.kokorev.cryptoview.data.entity

interface SavedCoin {
    val id: Int
    val coinPaprikaId: String
    val name: String
    val symbol: String
    val rank: Int
    val logo: String?
    val type: String?
    val price: Double?
    val dailyVolume: Double?
    val marketCap: Double?
    val percentChange24h: Double?
}