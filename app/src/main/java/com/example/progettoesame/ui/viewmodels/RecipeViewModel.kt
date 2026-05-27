package com.example.progettoesame.ui.viewmodels

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progettoesame.data.SyncManager
import com.example.progettoesame.data.database.Recipe
import com.example.progettoesame.data.database.Step
import com.example.progettoesame.data.repositories.RecipeRepository
import com.example.progettoesame.data.repositories.UserRepository
import com.example.progettoesame.ui.utils.getFormattedTimeStamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.text.startsWith

data class StepState(val stepStates : Map<Step, Boolean>)
data class RecipeData(val recipe: Recipe, val author: String)

data class RecipeActions (
    val onFavorite: (String, String) -> Unit,
    val onRate: (String, String, Int) -> Unit,
    val onToggleStep: (Step) -> Unit
)
class RecipeViewModel(private val recipeRepository: RecipeRepository,
                      private val userRepository: UserRepository,
                      private val syncManager: SyncManager): ViewModel() {
    private var tts: TextToSpeech? = null
    private var orderedSteps : List<Step> = emptyList()
    private var initialRate: Int = 0
    private var initialIsFavorite: Boolean = false

    private val _recipe = MutableStateFlow<RecipeData?>(null)
    val recipe = _recipe.asStateFlow()

    private val _stepsState = MutableStateFlow(StepState(emptyMap()))
    val stepsState = _stepsState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite = _isFavorite.asStateFlow()

    private val _rate = MutableStateFlow(0)
    val rate = _rate.asStateFlow()

    private val _currentSpeakingId = MutableStateFlow<String?>(null)
    val currentSpeakingId = _currentSpeakingId.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(0)

    private val _isFullAudioStarted = MutableStateFlow(false)
    val isFullAudioStarted = _isFullAudioStarted.asStateFlow()


    fun initTts(ctx: Context) {
        if (tts == null) {
            tts = TextToSpeech(ctx) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.getDefault()
                    setupProgressListener()
                }
            }
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _currentSpeakingId.value = utteranceId
            }
            override fun onDone(utteranceId: String?) {
                if (utteranceId?.startsWith("full_recipe") ?: false) {
                    _currentStepIndex.value += 1
                    speakNextStep()
                } else {
                    _currentSpeakingId.value = null
                }
            }
            override fun onError(utteranceId: String?) {
                _currentSpeakingId.value = null
            }
            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                _currentSpeakingId.value = null
            }
        })
    }

    private fun speakNextStep() {
        val index = _currentStepIndex.value
        if (index < orderedSteps.size) {
            val text = orderedSteps[index].description
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "full_recipe_step_${_currentStepIndex.value}")
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "full_recipe_step_${_currentStepIndex.value}")
        } else {
            _currentSpeakingId.value = null
            _currentStepIndex.value = 0
            _isFullAudioStarted.value = false
        }
    }

    fun onPlayPauseButtonClick() {
        if (_currentSpeakingId.value?.startsWith("full_recipe") ?: false) {
            tts?.stop()
            _currentSpeakingId.value = null
        } else {
            if (_currentStepIndex.value == 0) _isFullAudioStarted.value = true
            speakNextStep()
        }
    }

    fun onFullRecipeStopButtonClick() {
        tts?.stop()
        _currentSpeakingId.value = null
        _currentStepIndex.value = 0
        _isFullAudioStarted.value = false
    }

    fun onStepStopButtonClick() {
        tts?.stop()
        _currentSpeakingId.value = null
    }

    fun onStepPlayButtonClick(text: String, stepAudioId: String) {
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, stepAudioId)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, stepAudioId)
    }

    fun fetchRecipe(recipeId: String) {
        viewModelScope.launch {
            val recipe = recipeRepository.getRecipe(recipeId)
            val author = recipeRepository.getAuthor(recipe.author)
            _recipe.value = RecipeData(recipe, author)
            _stepsState.value = StepState(_recipe.value?.recipe?.steps?.associateWith { false } ?: emptyMap())
            orderedSteps = recipe.steps.sortedBy { it.number }
        }
    }

    fun getUserRecipeData(recipeId: String, userId: String) {
        viewModelScope.launch {
            _isFavorite.value = recipeRepository.isFavorite(recipeId, userId)
            _rate.value = recipeRepository.getRating(recipeId, userId) ?: 0
            initialRate = _rate.value
            initialIsFavorite = _isFavorite.value
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
                recipeRepository.deleteFavorite(recipeId, userId)
            } else {
                recipeRepository.setFavorite(recipeId, userId)
            }
            _isFavorite.value = !_isFavorite.value
        }},
        onRate = { recipeId, userId, rating -> viewModelScope.launch {
            val currentRecipe = _recipe.value?.recipe ?: return@launch
            val numberOfRatings = recipeRepository.getNumberOfRatings(recipeId)
            val currentSum = currentRecipe.averageRating * numberOfRatings
            val newAverage = when (_rate.value) {
                0 -> (currentSum + rating) / (numberOfRatings + 1)
                rating -> {
                    if (numberOfRatings <= 1) 0.0
                    else (currentSum - rating) / (numberOfRatings - 1)
                }
                else -> (currentSum + rating - _rate.value) / numberOfRatings
            }

            val updatedRecipe = currentRecipe.copy(averageRating = newAverage.toFloat(), updatedAt = getFormattedTimeStamp(), isSynced = false)
            if (_rate.value != rating) {
                recipeRepository.updateRating(recipeId, userId, rating)
                _rate.value = rating
            } else {
                recipeRepository.deleteRating(recipeId, userId)
                _rate.value = 0
            }
            recipeRepository.upsertRecipe(updatedRecipe)
            _recipe.value = _recipe.value?.copy(recipe = updatedRecipe)
            userRepository.updateUserAverageRating(updatedRecipe.author)
        }},
        onToggleStep = { step -> viewModelScope.launch {
            invertStepState(step)
        }}
    )

    fun syncOnExit() {
        if (initialRate != _rate.value || initialIsFavorite != _isFavorite.value) {
            syncManager.triggerImmediateSync()
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.shutdown()
    }
}