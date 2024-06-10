package dev.kokorev.cryptoview.backgroundService

data class AlarmData(
    val id: Int,
    val action: String,
    var time: Long,
    val period: Long,
    val cls: Class<*>,
)