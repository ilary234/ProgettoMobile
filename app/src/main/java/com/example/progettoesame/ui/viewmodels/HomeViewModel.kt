package com.example.progettoesame.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progettoesame.data.database.Category
import com.example.progettoesame.data.database.Recipe
import com.example.progettoesame.data.repositories.CategoryRepository
import com.example.progettoesame.data.repositories.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class Categories(val categories: List<Category>)

data class HomeSection(
    val title: String,
    val categoryId: String?,
    val recipes: Map<Recipe, Boolean>
)

data class HomeState(
    val sections: List<HomeSection> = emptyList(),
    val isLoading: Boolean = false
)

data class HomeActions(
    val onFavorite: (Recipe, String) -> Unit
)

class HomeViewModel(private val homeRepository: HomeRepository, private val categoryRepository: CategoryRepository) : ViewModel() {
     val categories = homeRepository.categories.map { Categories(it) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = Categories(emptyList()))

    private val _homeState = MutableStateFlow(HomeState())
    val homeState = _homeState.asStateFlow()

    fun fetchHomeData(userId: String) {
        viewModelScope.launch {
            _homeState.value = _homeState.value.copy(isLoading = true)
            try {
                val favoritesIds = categoryRepository.getUserFavorites(userId).map { it.recipeId }.toSet()
                val finalSections = mutableListOf<HomeSection>()

                val topRecipes = homeRepository.getTopRatedRecipes()
                if (topRecipes.isNotEmpty()) {
                    finalSections.add(
                        HomeSection(
                            title = "In primo piano",
                            categoryId = null,
                            recipes = topRecipes.associateWith { favoritesIds.contains(it.recipeId) }
                        )
                    )
                }

                homeRepository.categories.collect { categoriesList ->
                    categoriesList.sortedBy { it.order }.forEach { category ->
                        val categoryRecipes = categoryRepository.getRecipesFromCategory(category.categoryId)
                        val limitedRecipes = categoryRecipes.take(15)

                        if (limitedRecipes.isNotEmpty()) {
                            finalSections.add(
                                HomeSection(
                                    title = category.name,
                                    categoryId = category.categoryId,
                                    recipes = limitedRecipes.associateWith { favoritesIds.contains(it.recipeId) }
                                )
                            )
                        }
                    }
                    _homeState.value = HomeState(sections = finalSections, isLoading = false)
                }

            } catch (e: Exception) {
                _homeState.value = _homeState.value.copy(isLoading = false)
            }
        }
    }

    val actions = HomeActions(
        onFavorite = { recipe, userId ->
            viewModelScope.launch {
                val currentSections = _homeState.value.sections

                val isFavorite = currentSections.flatMap { it.recipes.entries }
                    .firstOrNull { it.key.recipeId == recipe.recipeId }?.value ?: false

                if (isFavorite) {
                    categoryRepository.deleteFavorite(recipe.recipeId, userId)
                } else {
                    categoryRepository.setFavorite(recipe.recipeId, userId)
                }

                val updatedSections = currentSections.map { section ->
                    if (section.recipes.containsKey(recipe)) {
                        val newRecipesMap = section.recipes.toMutableMap()
                        newRecipesMap[recipe] = !isFavorite
                        section.copy(recipes = newRecipesMap)
                    } else {
                        section
                    }
                }
                _homeState.value = _homeState.value.copy(sections = updatedSections)
            }
        }
    )
}