package com.example.progettoesame.data.repositories

import com.example.progettoesame.data.database.User
import com.example.progettoesame.data.database.daos.UserDAO

class UserRepository(private val userDAO: UserDAO) {
    suspend fun getUserById(userId: String): User? = userDAO.getUserById(userId)
    suspend fun upsertUser(user: User) = userDAO.upsert(user)
}