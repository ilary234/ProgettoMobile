package com.example.progettoesame.data.repositories

import android.util.Log
import com.example.progettoesame.data.SyncManager
import com.example.progettoesame.data.database.Category
import com.example.progettoesame.data.database.Recipe
import com.example.progettoesame.data.database.User
import com.example.progettoesame.data.database.UserFavourite
import com.example.progettoesame.data.database.daos.CategoryDAO
import com.example.progettoesame.data.database.daos.RecipeDAO
import com.example.progettoesame.data.database.daos.UserDAO
import com.example.progettoesame.data.database.daos.UserFavouriteDAO
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class SplashRepository(private val categoryDAO: CategoryDAO,
                       private val recipeDAO: RecipeDAO,
                       private val userDAO: UserDAO,
                       private val userFavouriteDAO: UserFavouriteDAO,
                       private val supabase: SupabaseClient,
                       private val syncManager: SyncManager) {
    suspend fun prepareData(): Boolean {
        try {
            val count = categoryDAO.getCount()
            if (count == 0 && syncManager.isOnline()) {
                val categories = supabase.from("categories").select().decodeList<Category>()
                categoryDAO.insertAll(categories)

                val recipes = supabase.from("recipes").select().decodeList<Recipe>()
                recipeDAO.upsertAll(recipes)
                recipeDAO.markAllRecipesAsSynced()

                val users = supabase.from("users").select().decodeList<User>()
                userDAO.insertAll(users)
                userDAO.markAllUsersAsSynced()

                val usersFavourites = supabase.from("user_favourite_recipes").select().decodeList<UserFavourite>()
                userFavouriteDAO.insertAll(usersFavourites)
                userFavouriteDAO.markAllUsersFavouriteAsSynced()
            }
            return true
        } catch (e: Exception) {

            Log.e("SplashRepository", "Errore durante prepareData: ${e.message}", e)
            return false
        }
    }
}