package com.example.progettoesame.data.repositories

import com.example.progettoesame.data.database.User
import com.example.progettoesame.data.database.daos.RecipeDAO
import com.example.progettoesame.data.database.daos.UserDAO
import com.example.progettoesame.ui.utils.getFormattedTimeStamp

class UserRepository(private val userDAO: UserDAO, private val recipeDAO: RecipeDAO) {
    suspend fun getUserById(userId: String): User? = userDAO.getUserById(userId)
    suspend fun upsertUser(user: User) = userDAO.upsert(user)
    suspend fun updateProfileImage(userId: String, imageUrl: String?): User {
        val currentUser = userDAO.getUserById(userId)!!

        val updatedUser = currentUser.copy(
            profileImageUrl = imageUrl,
            isSynced = false,
            updatedAt = getFormattedTimeStamp()
        )

        userDAO.upsert(updatedUser)
        return updatedUser
    }

    suspend fun updateUserAverageRating(userId: String): User? {
        val currentUser = userDAO.getUserById(userId) ?: return null

        val activeRecipes = recipeDAO.getRecipesByAuthor(userId)
        val totalRecipesCount = activeRecipes.size

        var sumOfAverages = 0.0f
        var recipesWithRatingsCount = 0

        for (recipe in activeRecipes) {
            if (recipe.averageRating > 0f) {
                sumOfAverages += recipe.averageRating
                recipesWithRatingsCount++
            }
        }

        val newAverageRating = if (recipesWithRatingsCount > 0) {
            sumOfAverages / recipesWithRatingsCount
        } else {
            0.0f
        }

        val updatedUser = currentUser.copy(
            recipeNumber = totalRecipesCount,
            averageRating = newAverageRating,
            isSynced = false,
            updatedAt = getFormattedTimeStamp()
        )

        userDAO.upsert(updatedUser)
        return updatedUser
    }

}