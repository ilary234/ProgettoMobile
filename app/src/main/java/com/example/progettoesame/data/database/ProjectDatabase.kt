package com.example.progettoesame.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.progettoesame.data.database.daos.RecipeDAO

@Database([Category::class, User::class, Recipe::class, UserFavourite::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ProjectDatabase : RoomDatabase() {
    abstract fun RecipeDAO(): RecipeDAO
    //Aggiungere la abstract anche per gli altri DAO
}