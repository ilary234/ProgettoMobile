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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<Map<Recipe, Boolean>>(emptyMap())
    val searchResults = _searchResults.asStateFlow()

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

    fun onSearchQueryChange(newQuery: String, userId: String) {
        _searchQuery.value = newQuery

        if (newQuery.isBlank()) {
            _searchResults.value = emptyMap()
            return
        }

        viewModelScope.launch {
            val searchWords = newQuery.lowercase()
                .split("\\s+".toRegex())
                .filter { it.length >= 4 }

            if (searchWords.isEmpty()) {
                _searchResults.value = emptyMap()
                return@launch
            }

            val wordToRecipesMap = mutableMapOf<String, List<Recipe>>()

            for (word in searchWords) {
                val recipesForWord = homeRepository.searchRecipesByWord(word)
                wordToRecipesMap[word] = recipesForWord
            }

            val sortedSearchWords = searchWords.sortedBy { word ->
                wordToRecipesMap[word]?.size ?: 0
            }

            val allMatchingRecipes = wordToRecipesMap.values.flatten().toSet()

            val sortedRecipes = allMatchingRecipes.sortedWith { r1, r2 ->
                val t1 = r1.title.lowercase()
                val t2 = r2.title.lowercase()

                var comparison = 0
                for (word in sortedSearchWords) {
                    val hasW1 = t1.contains(word)
                    val hasW2 = t2.contains(word)
                    if (hasW1 && !hasW2) {
                        comparison = -1
                        break
                    } else if (!hasW1 && hasW2) {
                        comparison = 1
                        break
                    }
                }
                comparison
            }

            val favoritesIds = categoryRepository.getUserFavorites(userId).map { it.recipeId }.toSet()

            _searchResults.value = sortedRecipes.associateWith { favoritesIds.contains(it.recipeId) }
        }
    }

    val actions = HomeActions(
        onFavorite = { recipe, userId ->
            viewModelScope.launch {
                val currentSections = _homeState.value.sections

                val isFavoriteInHome = currentSections.flatMap { it.recipes.entries }
                    .firstOrNull { it.key.recipeId == recipe.recipeId }?.value
                val isFavoriteInSearch = _searchResults.value[recipe]
                val isFavorite = isFavoriteInHome ?: isFavoriteInSearch ?: false

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

                if (_searchResults.value.containsKey(recipe)) {
                    val newSearchMap = _searchResults.value.toMutableMap()
                    newSearchMap[recipe] = !isFavorite
                    _searchResults.value = newSearchMap
                }
            }
        }
    )
}