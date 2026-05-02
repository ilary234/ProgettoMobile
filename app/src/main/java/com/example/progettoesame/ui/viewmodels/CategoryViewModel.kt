package com.example.progettoesame.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progettoesame.data.SyncManager
import com.example.progettoesame.data.database.Recipe
import com.example.progettoesame.data.repositories.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecipesState(val recipes: Map<Recipe, Boolean>)
data class RecipesActions (
    val onFavorite: (Recipe, String) -> Unit
)

class CategoryViewModel(private val repository: CategoryRepository, private val syncManager: SyncManager) : ViewModel() {
    private val _recipesState = MutableStateFlow(RecipesState(emptyMap()))
    val recipesState = _recipesState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun fetchRecipes(userId: String, categoryId: String) {
        viewModelScope.launch {
            val recipes = repository.getRecipesFromCategory(categoryId)
            val favoritesIds = repository.getUserFavorites(userId).map { it.recipeId }.toSet()
            _recipesState.value = RecipesState(recipes.associateWith { recipe ->
                favoritesIds.contains(recipe.recipeId)
            })
        }
    }

    fun fetchRecipesFromServer(userId: String, categoryId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                syncManager.triggerSync()
                fetchRecipes(userId, categoryId)
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