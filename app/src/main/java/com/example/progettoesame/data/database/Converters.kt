package com.example.progettoesame.data.database

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromIngredientList(value: List<Ingredient>): String = json.encodeToString(value)

    @TypeConverter
    fun toIngredientList(value: String): List<Ingredient> = json.decodeFromString(value)

    @TypeConverter
    fun fromStepList(value: List<Step>): String = json.encodeToString(value)

    @TypeConverter
    fun toStepList(value: String): List<Step> = json.decodeFromString(value)
}