package com.example.progettoesame.data.repositories


import android.util.Log
import com.example.progettoesame.data.database.Recipe
import com.example.progettoesame.data.database.daos.RecipeDAO
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class CategoryRepository(private val recipeDAO: RecipeDAO, private val supabase : SupabaseClient) {

    suspend fun getUnsyncedRecipes(): List<Recipe> = recipeDAO.getUnsyncedRecipes()
    suspend fun markAsSynced(id: String) = recipeDAO.markAsSynced(id)
    suspend fun upsert(recipe: Recipe) = recipeDAO.upsert(recipe)

    suspend fun sendRecipeToSupabase(recipe: Recipe) {
        supabase.from("recipes").upsert(recipe)
    }

    suspend fun pullFromSupabase() {
        try {
            val lastUpdate = recipeDAO.getLastUpdateTimestamp() ?: "2026-04-21T00:00:00Z"
            val remoteChanges = supabase.from("recipes").select {
                    filter {
                        gt("updated_at", lastUpdate)
                    }
                }.decodeList<Recipe>()

            if (remoteChanges.isNotEmpty()) {
                val syncedRecipes = remoteChanges.map { it.copy(isSynced = true) }
                recipeDAO.upsertAll(syncedRecipes)
            }
        } catch (e: Exception) {
            Log.e("Sync", "Errore durante la pull", e)
        }
    }

}