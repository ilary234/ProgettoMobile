package com.example.progettoesame.data.repositories

import com.example.progettoesame.data.database.daos.CategoryDAO

class HomeRepository(private val categoryDAO: CategoryDAO) {
    val categories = categoryDAO.getAll()
}