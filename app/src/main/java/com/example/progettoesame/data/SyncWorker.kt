package com.example.progettoesame.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.progettoesame.data.repositories.SyncRepository

class SyncWorker(appContext: Context, workerParams: WorkerParameters, private val repository: SyncRepository)
    : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val unsyncedRecipes = repository.getUnsyncedRecipes()
        val unsyncedUsers = repository.getUnsyncedUsers()
        val unsyncedUsersFavourites = repository.getUnsyncedUsersFavourites()

        return try {
            unsyncedRecipes.forEach { recipe ->
                repository.sendRecipeToSupabase(recipe)
                repository.markRecipeAsSynced(recipe.recipeId)
            }
            unsyncedUsers.forEach { user ->
                repository.sendUserToSupabase(user)
                repository.markUserAsSynced(user.userId)
            }
            unsyncedUsersFavourites.forEach { userFavourite ->
                repository.sendUserFavouriteToSupabase(userFavourite)
                repository.markUserFavouriteAsSynced(userFavourite.userId)
            }

            repository.pullFromSupabase()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}