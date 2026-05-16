package com.example.progettoesame.data.repositories

import com.example.progettoesame.data.database.Recipe
import com.example.progettoesame.data.database.UserFavourite
import com.example.progettoesame.data.database.daos.CategoryDAO
import com.example.progettoesame.data.database.daos.RecipeDAO
import com.example.progettoesame.data.database.daos.UserFavouriteDAO
import com.example.progettoesame.ui.utils.getFormattedTimeStamp

class CategoryRepository(private val categoryDAO: CategoryDAO) {
    val categories = categoryDAO.getAll()
}