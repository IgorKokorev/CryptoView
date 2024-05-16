package com.coinpaprika.apiclient.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message")
data class MessageDB(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "time") val time: Long,
    @ColumnInfo(name = "type") val type: MessageType,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "message") val message: String,
)

enum class MessageType {
    IN,
    OUT
}
