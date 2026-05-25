package com.example.progettoesame.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progettoesame.data.database.Recipe
import com.example.progettoesame.data.database.User
import com.example.progettoesame.data.repositories.ProfileRepository
import com.example.progettoesame.data.repositories.RecipeRepository
import com.example.progettoesame.data.repositories.SyncRepository
import com.example.progettoesame.data.repositories.UserRepository
import com.example.progettoesame.ui.utils.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileActions(
    val onFavorite: (Recipe) -> Unit,
    val onDelete: (Recipe) -> Unit
)

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository,
    private val recipeRepository: RecipeRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _userRecipes = MutableStateFlow<Map<Recipe, Boolean>>(emptyMap())
    val userRecipes = _userRecipes.asStateFlow()

    private val _favoriteRecipes = MutableStateFlow<Map<Recipe, Boolean>>(emptyMap())
    val favoriteRecipes = _favoriteRecipes.asStateFlow()

    private val _isOwnProfile = MutableStateFlow(false)
    val isOwnProfile = _isOwnProfile.asStateFlow()

    private val _isRecipeDeleted = MutableStateFlow(false)
    val isRecipeDeleted = _isRecipeDeleted.asStateFlow()

    fun resetDeleteState() {
        _isRecipeDeleted.value = false
    }

    fun loadProfile(profileUserId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val currentLoggedUserId = AuthState.userId.value
            val owned = currentLoggedUserId == profileUserId
            _isOwnProfile.value = owned

            val userDetails = userRepository.getUserById(profileUserId)
            _user.value = userDetails

            val rawRecipes = profileRepository.getRecipesByAuthor(profileUserId)
            _userRecipes.value = rawRecipes.associateWith { recipe ->
                if (currentLoggedUserId != null) {
                    recipeRepository.isFavorite(recipe.recipeId, currentLoggedUserId)
                } else {
                    false
                }
            }

            if (owned) {
                val favorites = recipeRepository.getUserFavorites(currentLoggedUserId)
                val favRecipesMap = favorites.map { recipeRepository.getRecipe(it.recipeId) }.associateWith { true }
                _favoriteRecipes.value = favRecipesMap
            } else {
                _favoriteRecipes.value = emptyMap()
            }

            _isLoading.value = false
        }
    }

    fun updateProfileImage(ctx: Context, userId: String, imageUrl: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            val updatedPreviewUri = when (imageUrl) {
                null -> null
                else -> syncRepository.updateImageStorageUrl(ctx, imageUrl) ?: return@launch
            }
            if (updatedPreviewUri == null && _user.value?.profileImageUrl!!.startsWith("http")) {
                syncRepository.deleteImage(_user.value?.profileImageUrl!!)
            }
            val updatedUser = userRepository.updateProfileImage(userId, updatedPreviewUri)
            _user.value = updatedUser
            _isLoading.value = false
        }
    }

    val actions = ProfileActions(
        onFavorite = { recipe ->
            viewModelScope.launch {
                val currentLoggedUserId = AuthState.userId.value ?: return@launch

                val isFavorite = _userRecipes.value[recipe]
                    ?: _favoriteRecipes.value[recipe]
                    ?: recipeRepository.isFavorite(recipe.recipeId, currentLoggedUserId)

                if (isFavorite) {
                    recipeRepository.deleteFavorite(recipe.recipeId, currentLoggedUserId)
                } else {
                    recipeRepository.setFavorite(recipe.recipeId, currentLoggedUserId)
                }

                val newFavoriteState = !isFavorite

                if (_userRecipes.value.containsKey(recipe)) {
                    val updatedUserRecipes = _userRecipes.value.toMutableMap()
                    updatedUserRecipes[recipe] = newFavoriteState
                    _userRecipes.value = updatedUserRecipes
                }

                if (_isOwnProfile.value) {
                    val updatedFavorites = _favoriteRecipes.value.toMutableMap()
                    if (newFavoriteState) {
                        updatedFavorites[recipe] = true
                    } else {
                        updatedFavorites.remove(recipe)
                    }
                    _favoriteRecipes.value = updatedFavorites
                }
            }
        },
        onDelete = { recipe ->
            viewModelScope.launch {
                profileRepository.deleteRecipe(recipe)

                val updatedUser = userRepository.updateUserAverageRating(recipe.author)

                if (updatedUser != null && _user.value?.userId == recipe.author) {
                    _user.value = updatedUser
                }

                val updatedUserRecipes = _userRecipes.value.toMutableMap()
                updatedUserRecipes.remove(recipe)
                _userRecipes.value = updatedUserRecipes

                val updatedFavorites = _favoriteRecipes.value.toMutableMap()
                updatedFavorites.remove(recipe)
                _favoriteRecipes.value = updatedFavorites

                _isRecipeDeleted.value = true
            }
        }
    )
}