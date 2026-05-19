package com.example.progettoesame.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progettoesame.data.database.Category
import com.example.progettoesame.data.database.Recipe
import com.example.progettoesame.data.repositories.HomeRepository
import com.example.progettoesame.data.repositories.RecipeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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

class HomeViewModel(private val homeRepository: HomeRepository, private val recipeRepository: RecipeRepository) : ViewModel() {

    val categories = homeRepository.categories.map { Categories(it) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = Categories(emptyList())
    )

    private val _homeState = MutableStateFlow(HomeState())
    val homeState = _homeState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val searchResults = _searchQuery
        .debounce(200L)
        .flatMapLatest { query ->
            val cleanedQuery = query.trim().lowercase()

            if (cleanedQuery.length < 3) {
                flowOf(emptyMap())
            } else {
                homeRepository.searchRecipesByFullQuery(cleanedQuery).map { recipesList ->

                    val searchWords = cleanedQuery.split("\\s+".toRegex())

                    val sortedRecipes = recipesList.sortedWith { r1, r2 ->
                        val t1 = r1.title.lowercase().trim()
                        val t2 = r2.title.lowercase().trim()

                        var score1 = 0
                        var score2 = 0

                        if (t1 == cleanedQuery) score1 += 100000
                        if (t2 == cleanedQuery) score2 += 100000

                        if (t1.startsWith(cleanedQuery)) score1 += 50000
                        if (t2.startsWith(cleanedQuery)) score2 += 50000

                        if (t1.contains(cleanedQuery)) score1 += 20000
                        if (t2.contains(cleanedQuery)) score2 += 20000

                        val wordsMatch1 = searchWords.count { t1.contains(it) }
                        val wordsMatch2 = searchWords.count { t2.contains(it) }
                        score1 += wordsMatch1 * 5000
                        score2 += wordsMatch2 * 5000

                        for (word in searchWords) {
                            val regex = "\\b${Regex.escape(word)}\\b".toRegex()
                            if (t1.contains(regex)) score1 += 1000
                            if (t2.contains(regex)) score2 += 1000
                        }

                        score2.compareTo(score1)
                    }

                    val currentHomeState = _homeState.value
                    sortedRecipes.associateWith { recipe ->
                        currentHomeState.sections.flatMap { it.recipes.entries }
                            .firstOrNull { it.key.recipeId == recipe.recipeId }?.value ?: false
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyMap()
        )

    fun fetchHomeData(userId: String) {
        viewModelScope.launch {
            _homeState.value = _homeState.value.copy(isLoading = true)
            try {
                val favoritesIds = recipeRepository.getUserFavorites(userId).map { it.recipeId }.toSet()
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

                val categoriesList = homeRepository.categories.first()
                categoriesList.sortedBy { it.order }.forEach { category ->
                    val categoryRecipes = recipeRepository.getRecipesFromCategory(category.categoryId)
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

            } catch (e: Exception) {
                _homeState.value = _homeState.value.copy(isLoading = false)
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    val actions = HomeActions(
        onFavorite = { recipe, userId ->
            viewModelScope.launch {
                val currentSections = _homeState.value.sections

                val isFavoriteInSearch = searchResults.value[recipe]
                val isFavoriteInHome = currentSections.flatMap { it.recipes.entries }
                    .firstOrNull { it.key.recipeId == recipe.recipeId }?.value

                val isFavorite = isFavoriteInHome ?: isFavoriteInSearch ?: false

                if (isFavorite) {
                    recipeRepository.deleteFavorite(recipe.recipeId, userId)
                } else {
                    recipeRepository.setFavorite(recipe.recipeId, userId)
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