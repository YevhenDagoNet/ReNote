package com.example.renote.data

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class Converters {

    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun epochDayToLocalDate(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun localTimeToSecondOfDay(time: LocalTime?): Int? = time?.toSecondOfDay()

    @TypeConverter
    fun secondOfDayToLocalTime(seconds: Int?): LocalTime? =
        seconds?.let { LocalTime.ofSecondOfDay(it.toLong()) }

    @TypeConverter
    fun instantToEpochMilli(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun epochMilliToInstant(millis: Long?): Instant? = millis?.let(Instant::ofEpochMilli)
}
