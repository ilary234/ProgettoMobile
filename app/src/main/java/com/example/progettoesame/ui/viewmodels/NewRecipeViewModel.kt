package com.example.progettoesame.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progettoesame.data.database.Category
import com.example.progettoesame.data.database.Ingredient
import com.example.progettoesame.data.database.Recipe
import com.example.progettoesame.data.database.Step
import com.example.progettoesame.data.repositories.CategoryRepository
import com.example.progettoesame.data.repositories.RecipeRepository
import com.example.progettoesame.data.repositories.SyncRepository
import com.example.progettoesame.data.repositories.UserRepository
import com.example.progettoesame.ui.utils.AuthState
import com.example.progettoesame.ui.utils.getFormattedTimeStamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class RecipeState (
    val id : String? = null,
    val previewImageUrl: String? = null,
    val title: String = "",
    val category: Category? = null,
    val preparation: Int = 0,
    val waiting: Int? = null,
    val cooking: Int = 0,
    val rating: Float = 0f,
    val ingredients: List<Ingredient> = listOf(Ingredient(name = "", quantity = 0f, unit = "", getFormattedTimeStamp())),
    val steps: List<Step> = listOf(Step(number = 1, imageUrls = emptyList(), description = "", getFormattedTimeStamp()))
)

data class OnRecipeChangeActions(
    val onTitleChange: (String) -> Unit,
    val onPreviewImageChange: (String?) -> Unit,
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
    val onAddImage: (Int, String) -> Unit,
    val onDeleteImage: (Int, Int) -> Unit,
    val onImageChange: (Int, Int, String) -> Unit
)

class NewRecipeViewModel(private val recipeRepository: RecipeRepository,
                         private val categoryRepository: CategoryRepository,
                         private val userRepository: UserRepository,
                         private val syncRepository: SyncRepository,
                         private val savedStateHandle: SavedStateHandle) : ViewModel() {
    val recipeId : String? = savedStateHandle["recipeId"]

    val categories = categoryRepository.categories.map { Categories(it) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = Categories(emptyList()))

    private val _state = MutableStateFlow(RecipeState())
    val state = _state.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved = _isSaved.asStateFlow()

    init {
        recipeId?.let { id ->
            viewModelScope.launch {
                val recipe = recipeRepository.getRecipe(id)
                _state.update {
                    it.copy(
                        id = id,
                        previewImageUrl = recipe.previewImageUrl,
                        title = recipe.title,
                        preparation = recipe.preparation,
                        waiting = recipe.waiting,
                        cooking = recipe.cooking,
                        ingredients = recipe.ingredients,
                        steps = recipe.steps,
                        rating = recipe.averageRating
                    )
                }
                categoryRepository.categories.collect { categories ->
                    val selected = categories.find { it.categoryId == recipe.category }
                    if (selected != null) {
                        _state.update { it.copy(category = selected) }
                    }
                }
            }
        }
    }

    val recipeActions = OnRecipeChangeActions(
        onTitleChange = { newTitle -> _state.update { it.copy(title = newTitle) }},
        onPreviewImageChange = { newUri -> viewModelScope.launch {
            if (_state.value.previewImageUrl != null && _state.value.previewImageUrl!!.startsWith("http")) {
                syncRepository.deleteImage(_state.value.previewImageUrl!!)
            }
            _state.update { it.copy(previewImageUrl = newUri) }
        }},
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
        onAddImage = { stepNumber, url ->
            val updatedSteps = _state.value.steps.map {
                if (it.number == stepNumber) it.copy(imageUrls = it.imageUrls + url) else it }
            _state.update { it.copy(steps = updatedSteps) }
        },
        onDeleteImage = { stepNumber, imageIndex ->
            viewModelScope.launch {
                val urlToDelete = _state.value.steps.filter { it.number == stepNumber }[0].imageUrls[imageIndex]
                if (urlToDelete.startsWith("http")) syncRepository.deleteImage(urlToDelete)
                val updatedImageUrls = _state.value.steps.filter { it.number == stepNumber }[0]
                    .imageUrls.filterIndexed { i, _ -> i != imageIndex }
                val updatedSteps = _state.value.steps.map {
                    if (it.number == stepNumber) it.copy(imageUrls = updatedImageUrls) else it
                }
                _state.update { it.copy(steps = updatedSteps) }
            }
        },
        onImageChange = { stepNumber, imageIndex, url ->
            val updatedImageUrls = _state.value.steps.filter { it.number == stepNumber }[0]
                .imageUrls.mapIndexed { i, it -> if (i == imageIndex) url else it }
            val updatedSteps = _state.value.steps.map {
                if (it.number == stepNumber) it.copy(imageUrls = updatedImageUrls) else it }
            _state.update { it.copy(steps = updatedSteps) }
        }

    )

    private suspend fun updateImageStorageUrl(ctx: Context, uriString: String) : String? {
        if (uriString.startsWith("http")) return uriString
        return try {
            val uri = uriString.toUri()
            val inputStream = ctx.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes != null) {
                val fileName = "${UUID.randomUUID()}.jpg"
                syncRepository.uploadImage(fileName, bytes)
            } else null
        } catch (e: Exception) {
            Log.e("NewRecipeViewModel", "Image upload failed", e)
            null
        }
    }

    fun saveRecipe(ctx: Context){
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val currentState = _state.value
                if (currentState.previewImageUrl == null || currentState.title.isEmpty() || currentState.category == null) {
                    _errorMessage.update { "Inserisci titolo, categoria e immagine di anteprima" }
                    _isRefreshing.value = false
                    return@launch
                }
                val notValidIngredient = _state.value.ingredients.find { it.name.isEmpty() || it.quantity == 0f || it.unit.isEmpty() }
                val notValidStep = _state.value.steps.find { it.description.isEmpty() }
                if (notValidIngredient != null || notValidStep != null) {
                    _errorMessage.update { "C'è stato un errore durante il caricamento. Controlla di aver inserito tutti i campi degli ingredienti e la descrizione dei passaggi." }
                    _isRefreshing.value = false
                    return@launch
                }


                val updatedPreviewUri = updateImageStorageUrl(ctx, _state.value.previewImageUrl!!)
                if (updatedPreviewUri == null) {
                    _isRefreshing.value = false
                    _errorMessage.update { "C'è stato un errore durante il caricamento delle immagini. Assicurati di essere connesso ad internet e riprova." }
                    return@launch
                }
                recipeActions.onPreviewImageChange(updatedPreviewUri)

                _state.value.steps.forEach {
                    it.imageUrls.forEachIndexed { i, uri ->
                        val updatedImageUri = updateImageStorageUrl(ctx, uri)
                        if (updatedImageUri == null) {
                            _isRefreshing.value = false
                            _errorMessage.update { "C'è stato un errore durante il caricamento delle immagini. Assicurati di essere connesso ad internet e riprova."}
                            return@launch
                        }
                        stepActions.onImageChange(it.number, i, updatedImageUri)
                    }
                }

                val authorId = AuthState.userId.value!!
                val author = userRepository.getUserById(authorId)
                if (author == null) {
                    _isRefreshing.value = false
                    _errorMessage.update { "C'è stato un errore durante il salvataggio. Riprova" }
                    return@launch
                }

                val recipe = when(recipeId) {
                    null -> Recipe(
                        title = _state.value.title,
                        author = authorId,
                        category = _state.value.category!!.categoryId,
                        previewImageUrl = _state.value.previewImageUrl!!,
                        preparation = _state.value.preparation,
                        waiting = _state.value.waiting,
                        cooking = _state.value.cooking,
                        ingredients = _state.value.ingredients,
                        steps = _state.value.steps,
                        averageRating = _state.value.rating,
                        updatedAt = getFormattedTimeStamp(),
                        isSynced = false)
                    else -> Recipe(
                        recipeId = recipeId,
                        title = _state.value.title,
                        author = authorId,
                        category = _state.value.category!!.categoryId,
                        previewImageUrl = _state.value.previewImageUrl!!,
                        preparation = _state.value.preparation,
                        waiting = _state.value.waiting,
                        cooking = _state.value.cooking,
                        ingredients = _state.value.ingredients,
                        steps = _state.value.steps,
                        averageRating = _state.value.rating,
                        updatedAt = getFormattedTimeStamp(),
                        isSynced = false)
                }
                recipeRepository.upsertRecipe(recipe)
                if (recipeId == null) {
                    userRepository.upsertUser(
                        author.copy(
                            averageRating = ((author.averageRating * author.recipeNumber) + recipe.averageRating) / (author.recipeNumber + 1),
                            recipeNumber = author.recipeNumber + 1,
                            isSynced = false,
                            updatedAt = getFormattedTimeStamp()
                        )
                    )
                }
                _isSaved.value = true
            } catch (e: Exception) {
                Log.e("NewRecipeViewModel", "Image upload failed", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.update { null }
    }
}
