package com.example.socalnet.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String =
        gson.toJson(value ?: emptyList<String>())

    @TypeConverter
    fun toStringList(value: String?): List<String> =
        if (value.isNullOrBlank()) emptyList()
        else gson.fromJson(value, object : TypeToken<List<String>>() {}.type)

    @TypeConverter
    fun fromIntMap(value: Map<String, Int>?): String =
        gson.toJson(value ?: emptyMap<String, Int>())

    @TypeConverter
    fun toIntMap(value: String?): Map<String, Int> =
        if (value.isNullOrBlank()) emptyMap()
        else gson.fromJson(value, object : TypeToken<Map<String, Int>>() {}.type)
}