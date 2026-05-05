package com.example.progettoesame.data.repositories

import com.example.progettoesame.data.database.Recipe
import com.example.progettoesame.data.database.UserFavourite
import com.example.progettoesame.data.database.daos.RecipeDAO
import com.example.progettoesame.data.database.daos.UserFavouriteDAO
import com.example.progettoesame.ui.utils.getFormattedTimeStamp

class CategoryRepository(private val recipeDAO: RecipeDAO, private val userFavouriteDAO: UserFavouriteDAO) {
    suspend fun getRecipesFromCategory(categoryId: String) : List<Recipe> = recipeDAO.getRecipesFromCategory(categoryId)
    suspend fun getUserFavorites(userId: String) : List<UserFavourite> = userFavouriteDAO.getUserFavorites(userId)
    suspend fun setFavorite(recipeId: String, userId: String) = userFavouriteDAO.upsert(
        UserFavourite(userId, recipeId,  getFormattedTimeStamp(), false, false))
    suspend fun deleteFavorite(recipeId: String, userId: String) = userFavouriteDAO.upsert(
        UserFavourite(userId, recipeId,getFormattedTimeStamp(), true, false))
}