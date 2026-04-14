package com.example.progettoesame.data.database

import android.media.Image
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Time

@Entity
data class Category (
    @PrimaryKey val categoryId : Int,
    @ColumnInfo val name : String
)

@Entity
data class User (
    @PrimaryKey(autoGenerate = true) val userId: Int,
    @ColumnInfo val username : String,
    @ColumnInfo val password : String,
    @ColumnInfo val email : String,
    @ColumnInfo val recipeNumber : Int = 0,
    @ColumnInfo val mean : Float = 0.0f
)

data class Times (
    @ColumnInfo val preparation : Time,
    @ColumnInfo val waiting : Time?,
    @ColumnInfo val cocking : Time,
)

data class Ingredient (
    @ColumnInfo val name : String,
    @ColumnInfo val quantity : Float,
    @ColumnInfo val unit : String
)

data class Step (
    @ColumnInfo val number : Int,
    @ColumnInfo val images: List<Image>,
    @ColumnInfo val description : String
)

@Entity(primaryKeys = ["title", "author", "categoryId"])
data class Recipe (
    @ColumnInfo val title : String,
    @ColumnInfo val author : Int,
    @ColumnInfo val category : Int,
    @ColumnInfo val previewImage : Image,
    @Embedded val times : Times,
    @Embedded val ingredients : List<Ingredient>,
    @Embedded val steps : List<Step>,
    @ColumnInfo val mean : Float = 0.0f
)

@Entity(primaryKeys = ["userId", "title", "author", "categoryId"])
data class UserFavourite (
    @ColumnInfo val userId: Int,
    @ColumnInfo val title : String,
    @ColumnInfo val author : Int,
    @ColumnInfo val category : Int
)