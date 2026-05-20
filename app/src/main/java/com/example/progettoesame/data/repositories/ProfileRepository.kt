package com.example.progettoesame.data.repositories

import com.example.progettoesame.data.database.Recipe
import com.example.progettoesame.data.database.daos.RecipeDAO
import com.example.progettoesame.data.database.daos.UserFavouriteDAO
import com.example.progettoesame.ui.utils.getFormattedTimeStamp

class ProfileRepository(
    private val recipeDAO: RecipeDAO,
    private val userFavouriteDAO: UserFavouriteDAO
) {
    suspend fun getRecipesByAuthor(userId: String): List<Recipe> = recipeDAO.getRecipesByAuthor(userId)

    suspend fun deleteRecipe(recipe: Recipe) {
        val timestamp = getFormattedTimeStamp()

        val deletedRecipe = recipe.copy(
            isDeleted = true,
            isSynced = false,
            updatedAt = timestamp
        )
        recipeDAO.upsert(deletedRecipe)
        //da capire se è da tenere
        userFavouriteDAO.softDeleteFavoritesByRecipe(recipe.recipeId, timestamp)
    }
}