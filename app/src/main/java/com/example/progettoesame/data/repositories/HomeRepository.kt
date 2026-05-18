package com.example.progettoesame.data.repositories

import com.example.progettoesame.data.database.Recipe
import com.example.progettoesame.data.database.daos.CategoryDAO
import com.example.progettoesame.data.database.daos.RecipeDAO

class HomeRepository(private val categoryDAO: CategoryDAO, private val recipeDAO: RecipeDAO) {
    val categories = categoryDAO.getAll()

    suspend fun getTopRatedRecipes(): List<Recipe> = recipeDAO.getTopRatedRecipes()

}