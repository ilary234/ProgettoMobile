package com.example.progettoesame.ui.viewmodels

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progettoesame.data.database.Recipe
import com.example.progettoesame.data.database.Step
import com.example.progettoesame.data.repositories.RecipeRepository
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
class RecipeViewModel(private val repository: RecipeRepository): ViewModel() {
    private var tts: TextToSpeech? = null
    private var orderedSteps : List<Step> = emptyList()

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
            val recipe = repository.getRecipe(recipeId)
            val author = repository.getAuthor(recipe.author)
            _recipe.value = RecipeData(recipe, author)
            _stepsState.value = StepState(_recipe.value?.recipe?.steps?.associateWith { false } ?: emptyMap())
            orderedSteps = recipe.steps.sortedBy { it.number }
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

    override fun onCleared() {
        super.onCleared()
        tts?.shutdown()
    }
}