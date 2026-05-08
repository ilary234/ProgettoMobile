package com.example.progettoesame.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progettoesame.data.database.Recipe
import com.example.progettoesame.data.database.Step
import com.example.progettoesame.data.repositories.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StepState(val stepStates : Map<Step, Boolean>)
data class RecipeData(val recipe: Recipe, val author: String)

data class RecipeActions (
    val onFavorite: (String, String) -> Unit,
    val onRate: (String, String, Int) -> Unit,
    val onToggleStep: (Step) -> Unit
)
class RecipeViewModel(private val repository: RecipeRepository): ViewModel() {
    private val _recipe = MutableStateFlow<RecipeData?>(null)
    val recipe = _recipe.asStateFlow()

    private val _stepsState = MutableStateFlow(StepState(emptyMap()))
    val stepsState = _stepsState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite = _isFavorite.asStateFlow()

    private val _rate = MutableStateFlow(0)
    val rate = _rate.asStateFlow()

    fun fetchRecipe(recipeId: String) {
        viewModelScope.launch {
            val recipe = repository.getRecipe(recipeId)
            val author = repository.getAuthor(recipe.author)
            _recipe.value = RecipeData(recipe, author)
            _stepsState.value = StepState(_recipe.value?.recipe?.steps?.associateWith { false } ?: emptyMap())
        }
    }

    fun getUserRecipeData(recipeId: String, userId: String) {
        viewModelScope.launch {
            _isFavorite.value = repository.isFavorite(recipeId, userId)
            _rate.value = repository.getRating(recipeId, userId) ?: 0
        }
    }

    private fun invertStepState(step: Step) {
        val currentMap = _stepsState.value.stepStates
        val updatedMap = currentMap.toMutableMap()
        updatedMap[step] = !(updatedMap[step] ?: false)
        _stepsState.value = _stepsState.value.copy(stepStates = updatedMap)
    }

    val actions = RecipeActions(
        onFavorite = { recipeId, userId -> viewModelScope.launch {
            if (_isFavorite.value) {
                repository.deleteFavorite(recipeId, userId)
            } else {
                repository.setFavorite(recipeId, userId)
            }
            _isFavorite.value = !_isFavorite.value
        }},
        onRate = { recipeId, userId, rating -> viewModelScope.launch {
            if (_rate.value != rating) {
                repository.updateRating(recipeId, userId, rating)
                _rate.value = rating
            } else {
                repository.deleteRating(recipeId, userId)
                _rate.value = 0
            }
        }},
        onToggleStep = { step -> viewModelScope.launch {
            invertStepState(step)
        }}
    )
}