package com.example.progettoesame.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.progettoesame.ui.NavigationRoute
import com.example.progettoesame.ui.utils.AuthState
import com.example.progettoesame.ui.utils.LoginRequiredDialog
import com.example.progettoesame.ui.utils.RecipePreviewCard
import com.example.progettoesame.ui.viewmodels.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    navController: NavController,
    categoryId: String,
    categoryName: String,
    categoryViewModel: CategoryViewModel
) {
    val recipesState by categoryViewModel.favoriteState.collectAsStateWithLifecycle()
    val isRefreshing by categoryViewModel.isRefreshing.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    val appBackgroundColor = MaterialTheme.colorScheme.background
    val appTextColor = MaterialTheme.colorScheme.onBackground
    val containerSectionColor = MaterialTheme.colorScheme.surface

    val onRefresh: () -> Unit = {
        categoryViewModel.syncAndFetchRecipes(AuthState.userId.value, categoryId)
    }

    LaunchedEffect(categoryId) {
        categoryViewModel.fetchRecipes(AuthState.userId.value, categoryId)
    }

    if (showDialog) {
        LoginRequiredDialog(
            containerColor = containerSectionColor,
            textColor = appTextColor,
            onDismiss = { showDialog = false },
            onConfirm = { showDialog = false
                navController.navigate(NavigationRoute.Login) }
        )
    }

    Scaffold(
        containerColor = appBackgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = categoryName, color = appTextColor, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = appBackgroundColor,
                    titleContentColor = appTextColor,
                    navigationIconContentColor = appTextColor
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back icon",
                            tint = appTextColor
                        )
                    }
                }
            )
        }
    ) {paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                state = rememberLazyListState(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(recipesState.recipes.entries.toList(), key = { it.key.recipeId }) {
                    val recipe = it.key
                    val isFavorite = it.value
                    RecipePreviewCard({navController.navigate(NavigationRoute.RecipeDetails(recipe.recipeId))},
                    recipe, isFavorite, containerSectionColor, appTextColor, {categoryViewModel.actions.onFavorite(recipe, AuthState.userId.value!!)},
                        {showDialog = true})
                }
            }
        }
    }
}