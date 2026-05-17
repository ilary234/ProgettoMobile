package com.example.progettoesame.data.repositories


import android.util.Log
import com.example.progettoesame.data.database.Recipe
import com.example.progettoesame.data.database.User
import com.example.progettoesame.data.database.UserFavourite
import com.example.progettoesame.data.database.UserRated
import com.example.progettoesame.data.database.daos.RecipeDAO
import com.example.progettoesame.data.database.daos.UserDAO
import com.example.progettoesame.data.database.daos.UserFavouriteDAO
import com.example.progettoesame.data.database.daos.UserRatedDAO
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncRepository(private val recipeDAO: RecipeDAO,
                     private val userDAO: UserDAO,
                     private val userFavouriteDAO: UserFavouriteDAO,
                     private val userRatedDAO: UserRatedDAO,
                     private val supabase : SupabaseClient) {

    suspend fun getUnsyncedRecipes(): List<Recipe> = recipeDAO.getUnsyncedRecipes()
    suspend fun getUnsyncedUsers(): List<User> = userDAO.getUnsyncedUsers()
    suspend fun getUnsyncedUsersFavourites(): List<UserFavourite> = userFavouriteDAO.getUnsyncedUsersFavourites()

    suspend fun getUnsyncedUsersRated(): List<UserRated> = userRatedDAO.getUnsyncedUsersRated()
    suspend fun markRecipeAsSynced(id: String) = recipeDAO.markAsSynced(id)
    suspend fun markUserAsSynced(id: String) = userDAO.markAsSynced(id)
    suspend fun markUserFavouriteAsSynced(userId: String, recipeId: String) = userFavouriteDAO.markAsSynced(userId, recipeId)
    suspend fun markUserRatedAsSynced(userId: String, recipeId: String) = userRatedDAO.markAsSynced(userId, recipeId)

    suspend fun sendRecipeToSupabase(recipe: Recipe) = withContext(Dispatchers.IO) {
        supabase.from("recipes").upsert(recipe)
    }

    suspend fun sendUserToSupabase(user: User) = withContext(Dispatchers.IO) {
        supabase.from("users").upsert(user)
    }

    suspend fun sendUserFavouriteToSupabase(userFavourite: UserFavourite) = withContext(Dispatchers.IO) {
        supabase.from("user_favourite_recipes").upsert(userFavourite)
    }

    suspend fun sendUserRatedToSupabase(userRated: UserRated) = withContext(Dispatchers.IO) {
        supabase.from("user_rated_recipes").upsert(userRated)
    }

    suspend fun pullFromSupabase() = withContext(Dispatchers.IO) {
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

            val lastUserRatedUpdate = userRatedDAO.getLastUpdateTimestamp() ?: "2026-04-21T00:00:00Z"
            val remoteUserRatedChanges = supabase.from("user_rated_recipes").select {
                filter {
                    gt("updated_at", lastUserRatedUpdate)
                }
            }.decodeList<UserRated>()
            if (remoteUserRatedChanges.isNotEmpty()) {
                val syncedUserRated = remoteUserRatedChanges.map { it.copy(isSynced = true) }
                userRatedDAO.upsertAll(syncedUserRated)
            }
        } catch (e: Exception) {
            Log.e("Sync", "Errore durante la pull", e)
        }
    }

    suspend fun uploadImage(fileName: String, imageBytes: ByteArray): String? = withContext(Dispatchers.IO){
        return@withContext try {
            val bucket = supabase.storage.from("Ricette")
            bucket.upload(path = fileName, data = imageBytes) {
                upsert = true
            }

            val url = bucket.publicUrl(fileName)
            url
        } catch (e: Exception) {
            Log.e("Sync", "Errore durante l'upload", e)
            null
        }
    }

    suspend fun deleteImage(url: String) = withContext(Dispatchers.IO){
        try {
            val fileName = url.substringAfterLast("/")
            val bucket = supabase.storage.from("Ricette")
            bucket.delete(fileName)
        } catch (e: Exception) {
            Log.e("Sync", "Errore durante l'eliminazione dell'immagine", e)
            null
        }
    }

    suspend fun fullSync() = withContext(Dispatchers.IO) {
        val unsyncedRecipes = getUnsyncedRecipes()
        val unsyncedUsers = getUnsyncedUsers()
        val unsyncedUsersFavourites = getUnsyncedUsersFavourites()
        val unsyncedUsersRated = getUnsyncedUsersRated()

        try {
            unsyncedRecipes.forEach { recipe ->
                sendRecipeToSupabase(recipe)
                markRecipeAsSynced(recipe.recipeId)
            }
            unsyncedUsers.forEach { user ->
                sendUserToSupabase(user)
                markUserAsSynced(user.userId)
            }
            unsyncedUsersFavourites.forEach { userFavourite ->
                sendUserFavouriteToSupabase(userFavourite)
                markUserFavouriteAsSynced(userFavourite.userId, userFavourite.recipeId)
            }
            unsyncedUsersRated.forEach { userRated ->
                sendUserRatedToSupabase(userRated)
                markUserRatedAsSynced(userRated.userId, userRated.recipeId)
            }

            pullFromSupabase()
        } catch (e: Exception) {
            Log.e("SyncRepository", "Error syncing data: ${e.message}")
        }
    }
}