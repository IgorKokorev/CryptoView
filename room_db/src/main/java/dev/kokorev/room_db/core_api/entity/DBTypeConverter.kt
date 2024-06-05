package dev.kokorev.room_db.core_api.entity

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate



class DBTypeConverter {
    @TypeConverter
    fun timestampToLocalDate(value: Long): LocalDate = LocalDate.ofEpochDay(value)
    
    @TypeConverter
    fun localDateToTimestamp(date: LocalDate): Long = date.toEpochDay()
    
    @TypeConverter
    fun timestampToInstant(value: Long): Instant = Instant.ofEpochMilli(value)
    
    @TypeConverter
    fun instantToTimestamp(instant: Instant): Long = instant.toEpochMilli()
}