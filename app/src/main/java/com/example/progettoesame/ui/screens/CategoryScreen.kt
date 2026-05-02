package com.example.progettoesame.ui.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.progettoesame.ui.NavigationRoute
import com.example.progettoesame.ui.utils.InfoPreview
import com.example.progettoesame.ui.utils.LoginRequiredDialog
import com.example.progettoesame.ui.utils.PreviewCard
import com.example.progettoesame.ui.viewmodels.CategoryViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(navController: NavController,
                   categoryId : String,
                   categoryName: String,
                   categoryViewModel: CategoryViewModel) {
    val recipesState by categoryViewModel.recipesState.collectAsStateWithLifecycle()
    val isRefreshing by categoryViewModel.isRefreshing.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    val onRefresh: () -> Unit = {
        categoryViewModel.fetchRecipesFromServer("userId", categoryId)
    }

    LaunchedEffect(categoryId) {
        categoryViewModel.fetchRecipesFromServer("userId", categoryId)
    }

    if (showDialog) {
        LoginRequiredDialog(
            onDismiss = { showDialog = false },
            onConfirm = { navController.navigate(NavigationRoute.Login) }
        )
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(categoryName, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back icon"
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
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(
                    recipesState.recipes.entries.toList(),
                    key = { it.key.recipeId }) {
                    val recipe = it.key
                    val isFavorite = it.value
                    PreviewCard(recipe.previewImageUrl, recipe.title)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (false/*isLoggedIn()*/) {
                                categoryViewModel.actions.onFavorite(recipe, "userId") //TODO
                            } else {
                                showDialog = true
                            }
                        }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite Icon",
                                modifier = Modifier.size(24.dp),
                                tint = if (isFavorite) Color.Red else Color.Gray
                            )
                        }
                    }
                    val time = recipe.preparation + recipe.cooking + (recipe.waiting ?: 0)
                    val hours = time / 60
                    val minutes = time % 60

                    val formattedTime = when {
                        time < 60 -> "$time min"
                        minutes == 0 -> "${hours} h"
                        else -> "${hours} h ${minutes} min"
                    }

                    InfoPreview(recipe.title, formattedTime, recipe.averageRating)
                }
            }
        }
    }
}