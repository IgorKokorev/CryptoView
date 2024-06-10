package dev.kokorev.cryptoview.utils

import android.os.Parcelable
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.INTENT_ACTION

data class NotificationData(
    val title: String,
    val text: String,
    val keyExtra: String? = null,
    val extra: Parcelable? = null,
    val action: String = INTENT_ACTION,
    val id: Int = Constants.NOTIFICATION_ID
)
