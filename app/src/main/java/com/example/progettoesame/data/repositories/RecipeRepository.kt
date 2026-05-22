package com.example.progettoesame.data.repositories

import com.example.progettoesame.data.database.Recipe
import com.example.progettoesame.data.database.UserFavourite
import com.example.progettoesame.data.database.UserRated
import com.example.progettoesame.data.database.daos.RecipeDAO
import com.example.progettoesame.data.database.daos.UserDAO
import com.example.progettoesame.data.database.daos.UserFavouriteDAO
import com.example.progettoesame.data.database.daos.UserRatedDAO
import com.example.progettoesame.ui.utils.getFormattedTimeStamp

class RecipeRepository(private val recipeDAO: RecipeDAO,
                       private val userDAO: UserDAO,
                       private val userFavouriteDAO: UserFavouriteDAO,
                       private val userRatedDAO: UserRatedDAO) {
    suspend fun getRecipe(recipeId: String): Recipe = recipeDAO.getRecipe(recipeId)
    suspend fun upsertRecipe(recipe: Recipe) = recipeDAO.upsert(recipe)
    suspend fun getAuthor(userId: String): String = userDAO.getAuthor(userId)
    suspend fun isFavorite(recipeId: String, userId: String): Boolean = userFavouriteDAO.isFavorite(recipeId, userId)
    suspend fun getRating(recipeId: String, userId: String): Int? = userRatedDAO.getRating(userId, recipeId)
    suspend fun setFavorite(recipeId: String, userId: String) = userFavouriteDAO.upsert(
        UserFavourite(userId, recipeId, getFormattedTimeStamp(), false, false))
    suspend fun deleteFavorite(recipeId: String, userId: String) = userFavouriteDAO.upsert(
        UserFavourite(userId, recipeId,getFormattedTimeStamp(), true, false))
    suspend fun updateRating(recipeId: String, userId: String, rating: Int) = userRatedDAO.upsert(
        UserRated(userId, recipeId, rating, getFormattedTimeStamp(), false, false))
    suspend fun deleteRating(recipeId: String, userId: String) = userRatedDAO.upsert(
        UserRated(userId, recipeId, 0, getFormattedTimeStamp(), true, false))
    suspend fun getRecipesFromCategory(categoryId: String) : List<Recipe> = recipeDAO.getRecipesFromCategory(categoryId)
    suspend fun getUserFavorites(userId: String) : List<UserFavourite> = userFavouriteDAO.getUserFavorites(userId)
    suspend fun getNumberOfRatings(recipeId: String) : Int = userRatedDAO.getNumberOfRatings(recipeId)
}
