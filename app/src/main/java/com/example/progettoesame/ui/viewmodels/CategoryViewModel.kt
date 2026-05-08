package com.example.progettoesame.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progettoesame.data.SyncManager
import com.example.progettoesame.data.database.Recipe
import com.example.progettoesame.data.repositories.CategoryRepository
import com.example.progettoesame.data.repositories.SyncRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecipesState(val recipes: Map<Recipe, Boolean>)
data class RecipesActions (
    val onFavorite: (Recipe, String) -> Unit
)

class CategoryViewModel(private val repository: CategoryRepository,
                        private val syncManager: SyncManager,
                        private val syncRepository: SyncRepository) : ViewModel() {
    private val _recipesState = MutableStateFlow(RecipesState(emptyMap()))
    val recipesState = _recipesState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private suspend fun loadData(userId: String, categoryId: String) {
        val recipes = repository.getRecipesFromCategory(categoryId)
        val favoritesIds = repository.getUserFavorites(userId).map { it.recipeId }.toSet()
        _recipesState.value = RecipesState(recipes.associateWith { recipe ->
            favoritesIds.contains(recipe.recipeId)
        })
    }

    fun fetchRecipes(userId: String, categoryId: String) {
        viewModelScope.launch {
            loadData(userId, categoryId)
        }
    }

    fun syncAndFetchRecipes(userId: String, categoryId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val startTime = System.currentTimeMillis()
                if (syncManager.isOnline()) {
                    syncRepository.fullSync()
                }
                loadData(userId, categoryId)
                val elapsedTime = System.currentTimeMillis() - startTime
                if (elapsedTime < 1000) delay(1000 - elapsedTime)
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Sync failed", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    val actions = RecipesActions(
        onFavorite = { recipe, userId -> viewModelScope.launch {
            val currentMap = _recipesState.value.recipes
            val isFavorite = currentMap[recipe] ?: false
            if (isFavorite) {
                repository.deleteFavorite(recipe.recipeId, userId)
            } else {
                repository.setFavorite(recipe.recipeId, userId)
            }
            val updatedMap = currentMap.toMutableMap()
            updatedMap[recipe] = !isFavorite
            _recipesState.value = _recipesState.value.copy(recipes = updatedMap)
        } }
    )

}