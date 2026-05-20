package com.example.progettoesame.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.progettoesame.data.database.Recipe
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM recipes WHERE recipeId = :id")
    suspend fun getRecipe(id: String): Recipe

    @Query("DELETE FROM recipes WHERE isDeleted = 1 AND datetime(updatedAt) < datetime('now', '-1 day')")
    suspend fun hardDeleteRecipes()

    @Upsert
    suspend fun upsertAll(recipes: List<Recipe>)

    @Upsert
    suspend fun upsert(recipe: Recipe)

    @Query("SELECT * FROM recipes WHERE category = :categoryId and isDeleted = 0")
    suspend fun getRecipesFromCategory(categoryId: String) : List<Recipe>

    @Query("SELECT * FROM recipes WHERE isDeleted = 0 ORDER BY averageRating DESC LIMIT 15")
    suspend fun getTopRatedRecipes(): List<Recipe>

    @Query("SELECT * FROM recipes WHERE title LIKE '%' || :query || '%' AND isDeleted = 0")
    fun searchRecipesByFullQuery(query: String): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE author = :userId AND isDeleted = 0")
    suspend fun getRecipesByAuthor(userId: String): List<Recipe>
}