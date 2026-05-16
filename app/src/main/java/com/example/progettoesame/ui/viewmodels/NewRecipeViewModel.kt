package com.example.progettoesame.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progettoesame.data.database.Category
import com.example.progettoesame.data.database.Ingredient
import com.example.progettoesame.data.database.Step
import com.example.progettoesame.data.repositories.CategoryRepository
import com.example.progettoesame.data.repositories.RecipeRepository
import com.example.progettoesame.ui.utils.getFormattedTimeStamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipeState (
    val id : String? = null,
    val title: String = "",
    val category: Category? = null,
    val preparation: Int = 0,
    val waiting: Int? = null,
    val cooking: Int = 0,
    val ingredients: List<Ingredient> = listOf(Ingredient(name = "", quantity = 0f, unit = "", getFormattedTimeStamp())),
    val steps: List<Step> = listOf(Step(number = 1, imageUrls = emptyList(), description = "", getFormattedTimeStamp()))
)

data class OnRecipeChangeActions(
    val onTitleChange: (String) -> Unit,
    val onCategoryChange: (String) -> Unit,
    val onPreparationChange: (Int) -> Unit,
    val onWaitingChange: (Int) -> Unit,
    val onCookingChange: (Int) -> Unit,
)

data class IngredientActions(
    val onAddIngredient: () -> Unit,
    val onDeleteIngredient: (Int) -> Unit,
    val onValueChange: (Int, Ingredient) -> Unit,
)

data class StepActions(
    val onAddStep: (Int) -> Unit,
    val onDeleteStep: (Step) -> Unit,
    val onDescriptionChange: (Step) -> Unit,
    val onImageChange: (Step) -> Unit
)

class NewRecipeViewModel(private val recipeRepository: RecipeRepository,
                         private val categoryRepository: CategoryRepository,
                         private val recipeId: String? = null) : ViewModel() {

    val categories = categoryRepository.categories.map { Categories(it) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = Categories(emptyList()))

    private val _state = MutableStateFlow(RecipeState())
    val state = _state.asStateFlow()

    init {
        recipeId?.let { id ->
            viewModelScope.launch {
                val recipe = recipeRepository.getRecipe(id)
                val category = categories.value.categories.find { it.categoryId == recipe.category }
                _state.update {
                    it.copy(
                        id = id,
                        title = recipe.title,
                        category = category,
                        preparation = recipe.preparation,
                        waiting = recipe.waiting,
                        cooking = recipe.cooking,
                        ingredients = recipe.ingredients,
                        steps = recipe.steps
                    )
                }
            }
        }
    }

    val recipeActions = OnRecipeChangeActions(
        onTitleChange = { newTitle -> _state.update { it.copy(title = newTitle) }},
        onCategoryChange = { categoryName ->
            val selected = categories.value.categories.find { it.name == categoryName }
            _state.update { it.copy(category = selected) }
        },
        onPreparationChange = { newPrep -> _state.update { it.copy(preparation = newPrep) }},
        onWaitingChange = { newWait -> _state.update { it.copy(waiting = newWait) } },
        onCookingChange = { newCook -> _state.update { it.copy(cooking = newCook) } },
    )

    val ingredientActions = IngredientActions(
        onAddIngredient = {
            val newIngredient = Ingredient("", 0f, "", getFormattedTimeStamp())
            _state.update { it.copy(ingredients = it.ingredients + newIngredient) }
        },
        onDeleteIngredient = { index ->
            _state.update { it.copy(ingredients = it.ingredients.filterIndexed { i, _ -> i != index }) }
        },
        onValueChange = { index, ingredient ->
            val updatedIngredients = _state.value.ingredients.mapIndexed { i, it -> if (i == index) ingredient else it }
            _state.update { it.copy(ingredients = updatedIngredients) }
        }
    )

    val stepActions = StepActions(
        onAddStep = { stepNumber ->
            val newStep = Step(stepNumber, emptyList(), "", getFormattedTimeStamp())
            _state.update { it.copy(steps = it.steps + newStep) }
        },
        onDeleteStep = { step ->
            _state.update { it.copy(steps = it.steps.filter { it != step }) }
        },
        onDescriptionChange = { step ->
            val updatedSteps = _state.value.steps.map { if (it.number == step.number) step else it }
            _state.update { it.copy(steps = updatedSteps) }
        },
        onImageChange = { step ->
            val updatedSteps = _state.value.steps.map { if (it == step) step else it }
            _state.update { it.copy(steps = updatedSteps) }
        }
    )

}