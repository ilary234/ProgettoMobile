package com.example.progettoesame.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.progettoesame.data.repositories.CategoryRepository

class SyncWorker(appContext: Context, workerParams: WorkerParameters, private val categoryRepository: CategoryRepository)
    : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val unsyncedRecipes = categoryRepository.getUnsyncedRecipes()

        return try {
            unsyncedRecipes.forEach { recipe ->
                categoryRepository.sendRecipeToSupabase(recipe)
                categoryRepository.markAsSynced(recipe.recipeId)
            }
            categoryRepository.pullFromSupabase()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}