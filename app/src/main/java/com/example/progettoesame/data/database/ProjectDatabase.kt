package com.example.progettoesame.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.progettoesame.data.database.daos.CategoryDAO
import com.example.progettoesame.data.database.daos.RecipeDAO
import com.example.progettoesame.data.database.daos.UserDAO
import com.example.progettoesame.data.database.daos.UserFavouriteDAO

@Database([Category::class, User::class, Recipe::class, UserFavourite::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ProjectDatabase : RoomDatabase() {
    abstract fun RecipeDAO(): RecipeDAO
    abstract fun CategoryDAO(): CategoryDAO
    abstract fun UserDAO(): UserDAO
    abstract fun UserFavouriteDAO(): UserFavouriteDAO
}