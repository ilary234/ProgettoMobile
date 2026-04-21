package com.example.progettoesame.data.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity(tableName = "categories")
data class Category (
    @PrimaryKey
    @SerialName("category_id")
    val categoryId : String,
    @SerialName("name") val name : String,
    @SerialName("order") val order : Int,
    @Transient val isSynced: Boolean = false
)

@Serializable
@Entity(tableName = "users")
data class User (
    @PrimaryKey
    @SerialName("user_id")
    val userId: String,
    @SerialName("username") val username : String,
    @SerialName("password") val password : String,
    @SerialName("email") val email : String,
    @SerialName("recipe_number") val recipeNumber : Int = 0,
    @SerialName("average_rating") val averageRating : Float = 0.0f,
    @SerialName("updated_at") val updatedAt : String,
    @Transient val isSynced: Boolean = false
)

@Serializable
data class Times (
    @SerialName("preparation_minutes") val preparation : Int,
    @SerialName("waiting_minutes") val waiting : Int?,
    @SerialName("cooking_minutes") val cooking : Int,
)

@Serializable
data class Ingredient (
    @SerialName("ingredient_name") val name : String,
    @SerialName("quantity") val quantity : Float,
    @SerialName("unit") val unit : String, //enum di kg, g, ml, L
    @SerialName("updated_at") val updatedAt : String,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
    @Transient val isSynced: Boolean = false
)

@Serializable
data class Step (
    @SerialName("step_number") val number : Int, //Autoincrement da 1
    val imageUrls: List<String>,
    @SerialName("description") val description : String,
    @SerialName("updated_at") val updatedAt : String,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
    @Transient val isSynced: Boolean = false
)

@Serializable
@Entity(tableName = "recipes")
data class Recipe (
    @PrimaryKey
    @SerialName("recipe_id") val recipeId : String,
    @SerialName("title") val title : String,
    @SerialName("author_id") val author : String,
    @SerialName("category_id") val category : String,
    @SerialName("preview_image_url") val previewImageUrl : String,
    @Embedded val times : Times,
    val ingredients : List<Ingredient>,
    val steps : List<Step>,
    @SerialName("average_rating") val averageRating : Float = 0.0f,
    @SerialName("updated_at") val updatedAt : String,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
    @Transient val isSynced: Boolean = false
)

@Serializable
@Entity(tableName = "user_favourite_recipes", primaryKeys = ["userId", "recipeId"])
data class UserFavourite (
    @SerialName("user_id") val userId: String,
    @SerialName("recipe_id") val recipeId : String,
    @SerialName("updated_at") val updatedAt : String,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
    @Transient val isSynced: Boolean = false
)