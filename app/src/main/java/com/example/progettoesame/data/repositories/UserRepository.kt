package com.example.progettoesame.data.repositories

import com.example.progettoesame.data.database.Recipe
import com.example.progettoesame.data.database.User
import com.example.progettoesame.data.database.daos.RecipeDAO
import com.example.progettoesame.data.database.daos.UserDAO
import com.example.progettoesame.data.database.daos.UserRatedDAO
import com.example.progettoesame.ui.utils.getFormattedTimeStamp

class UserRepository(private val userDAO: UserDAO, private val recipeDAO: RecipeDAO, private val userRatedDAO: UserRatedDAO) {
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

    //funzione per aggiornare l'average_rating dello user (da capire se è da togliere)
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

    //funzione per aggiornare l'average_rating della ricetta (da capire se è da togliere)
    suspend fun recalculateRecipeAverage(recipeId: String): Recipe {
        val totalRatings = userRatedDAO.countValidRatingsForRecipe(recipeId)

        val sumOfRatings = userRatedDAO.getSumOfRatingsForRecipe(recipeId) ?: 0

        val newAverage = if (totalRatings > 0) {
            sumOfRatings.toFloat() / totalRatings
        } else {
            0.0f
        }

        val currentRecipe = recipeDAO.getRecipe(recipeId)
        val updatedRecipe = currentRecipe.copy(
            averageRating = newAverage,
            isSynced = false,
            updatedAt = getFormattedTimeStamp()
        )
        recipeDAO.upsert(updatedRecipe)
        return updatedRecipe
    }
}