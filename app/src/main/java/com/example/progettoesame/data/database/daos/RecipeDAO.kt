package com.example.progettoesame.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.progettoesame.data.database.Recipe

@Dao
interface RecipeDAO {

    @Query("SELECT * FROM recipes WHERE isSynced = 0")
    suspend fun getUnsyncedRecipes(): List<Recipe>

    @Query("UPDATE recipes SET isSynced = 1 WHERE recipeId = :id")
    suspend fun markAsSynced(id: String)

    @Query("UPDATE recipes SET isSynced = 1")
    suspend fun markAllRecipesAsSynced()

    @Query("SELECT MAX(updatedAt) FROM recipes")
    suspend fun getLastUpdateTimestamp(): String?

    @Upsert
    suspend fun upsertAll(recipes: List<Recipe>)

    @Upsert
    suspend fun upsert(recipe: Recipe)

    @Query("SELECT * FROM recipes WHERE category = :categoryId")
    suspend fun getRecipesFromCategory(categoryId: String) : List<Recipe>
}