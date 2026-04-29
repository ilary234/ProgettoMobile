package com.example.progettoesame.data.repositories


import android.util.Log
import com.example.progettoesame.data.database.Recipe
import com.example.progettoesame.data.database.User
import com.example.progettoesame.data.database.UserFavourite
import com.example.progettoesame.data.database.daos.RecipeDAO
import com.example.progettoesame.data.database.daos.UserDAO
import com.example.progettoesame.data.database.daos.UserFavouriteDAO
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class SyncRepository(private val recipeDAO: RecipeDAO,
                     private val userDAO: UserDAO,
                     private val userFavouriteDAO: UserFavouriteDAO,
                     private val supabase : SupabaseClient) {

    suspend fun getUnsyncedRecipes(): List<Recipe> = recipeDAO.getUnsyncedRecipes()
    suspend fun getUnsyncedUsers(): List<User> = userDAO.getUnsyncedUsers()
    suspend fun getUnsyncedUsersFavourites(): List<UserFavourite> = userFavouriteDAO.getUnsyncedUsersFavourites()
    suspend fun markRecipeAsSynced(id: String) = recipeDAO.markAsSynced(id)
    suspend fun markUserAsSynced(id: String) = userDAO.markAsSynced(id)
    suspend fun markUserFavouriteAsSynced(id: String) = userFavouriteDAO.markAsSynced(id)

    suspend fun sendRecipeToSupabase(recipe: Recipe) {
        supabase.from("recipes").upsert(recipe)
    }

    suspend fun sendUserToSupabase(user: User) {
        supabase.from("users").upsert(user)
    }

    suspend fun sendUserFavouriteToSupabase(userFavourite: UserFavourite) {
        supabase.from("user_favourite_recipes").upsert(userFavourite)
    }

    suspend fun pullFromSupabase() {
        try {
            val lastRecipeUpdate = recipeDAO.getLastUpdateTimestamp() ?: "2026-04-21T00:00:00Z"
            val remoteRecipeChanges = supabase.from("recipes").select {
                    filter {
                        gt("updated_at", lastRecipeUpdate)
                    }
                }.decodeList<Recipe>()
            if (remoteRecipeChanges.isNotEmpty()) {
                val syncedRecipes = remoteRecipeChanges.map { it.copy(isSynced = true) }
                recipeDAO.upsertAll(syncedRecipes)
            }

            val lastUserUpdate = userDAO.getLastUpdateTimestamp() ?: "2026-04-21T00:00:00Z"
            val remoteUserChanges = supabase.from("users").select {
                filter {
                    gt("updated_at", lastUserUpdate)
                }
            }.decodeList<User>()
            if (remoteUserChanges.isNotEmpty()) {
                val syncedUsers = remoteUserChanges.map { it.copy(isSynced = true) }
                userDAO.upsertAll(syncedUsers)
            }

            val lastUserFavouriteUpdate = userFavouriteDAO.getLastUpdateTimestamp() ?: "2026-04-21T00:00:00Z"
            val remoteUserFavouriteChanges = supabase.from("user_favourite_recipes").select {
                filter {
                    gt("updated_at", lastUserFavouriteUpdate)
                }
            }.decodeList<UserFavourite>()
            if (remoteUserFavouriteChanges.isNotEmpty()) {
                val syncedUserFavourites = remoteUserFavouriteChanges.map { it.copy(isSynced = true) }
                userFavouriteDAO.upsertAll(syncedUserFavourites)
            }
        } catch (e: Exception) {
            Log.e("Sync", "Errore durante la pull", e)
        }
    }
}