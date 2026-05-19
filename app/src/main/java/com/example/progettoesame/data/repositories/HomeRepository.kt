package com.example.progettoesame.data.repositories

import com.example.progettoesame.data.database.Recipe
import com.example.progettoesame.data.database.daos.CategoryDAO
import com.example.progettoesame.data.database.daos.RecipeDAO
import kotlinx.coroutines.flow.Flow

class HomeRepository(private val categoryDAO: CategoryDAO, private val recipeDAO: RecipeDAO) {
    val categories = categoryDAO.getAll()

    suspend fun getTopRatedRecipes(): List<Recipe> = recipeDAO.getTopRatedRecipes()

    fun searchRecipesByFullQuery(query: String): Flow<List<Recipe>> = recipeDAO.searchRecipesByFullQuery(query)
}