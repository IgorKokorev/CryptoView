package dev.kokorev.cryptoview.data.entity

data class RecentCoin(
    override val id: Int = 0,
    override val coinPaprikaId: String,
    override val name: String,
    override val symbol: String,
    override val rank: Int,
    override val logo: String?,
    override val type: String?,
    val lastTime: Long, // the only difference from FavoriteCoin
    override val price: Double?,
    override val dailyVolume: Double?,
    override val marketCap: Double?,
    override val percentChange24h: Double?,
): SavedCoin