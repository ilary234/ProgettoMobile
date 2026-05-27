package com.example.progettoesame.data.repositories

import com.example.progettoesame.data.database.daos.CategoryDAO

class CategoryRepository(private val categoryDAO: CategoryDAO) {
    val categories = categoryDAO.getAll()
}