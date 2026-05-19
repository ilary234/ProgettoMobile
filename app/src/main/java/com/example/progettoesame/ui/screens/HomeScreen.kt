package com.example.progettoesame.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.progettoesame.ui.NavigationRoute
import com.example.progettoesame.ui.utils.AuthState
import com.example.progettoesame.ui.utils.InfoPreview
import com.example.progettoesame.ui.utils.LoginRequiredDialog
import com.example.progettoesame.ui.utils.PreviewCard
import com.example.progettoesame.ui.utils.RecipeCard
import com.example.progettoesame.ui.utils.RecipePreviewCard
import com.example.progettoesame.ui.utils.formatTime
import com.example.progettoesame.ui.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, homeViewModel: HomeViewModel)  {
    var isMenuOpen by remember { mutableStateOf(false) }
    val categories by homeViewModel.categories.collectAsStateWithLifecycle()

    val homeState by homeViewModel.homeState.collectAsStateWithLifecycle()
    val currentUserId = AuthState.userId.value ?: "guest_user"

    var showLoginDialog by remember { mutableStateOf(false) }
    val isLoggedIn by AuthState.isLoggedIn
    val userEmail by AuthState.userEmail

    val searchQuery by homeViewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by homeViewModel.searchResults.collectAsStateWithLifecycle()

    val topBarTitle = if (isLoggedIn && !userEmail.isNullOrBlank()) {
        userEmail!!
    } else {
        "NomeApp"
    }

    val isResetMode by AuthState.isResetPasswordMode

    if (showLoginDialog) {
        LoginRequiredDialog(
            onDismiss = { showLoginDialog = false },
            onConfirm = {
                showLoginDialog = false
                navController.navigate(NavigationRoute.Login)
            }
        )
    }

    LaunchedEffect(currentUserId) {
        homeViewModel.fetchHomeData(currentUserId)
    }

    LaunchedEffect(isResetMode) {
        if (isResetMode) {
            navController.navigate(NavigationRoute.ResetPassword)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.White,
            topBar = {
                // alpha per nascondere senza rimuovere lo spazio
                Box(modifier = Modifier.alpha(if (isMenuOpen) 0f else 1f)) {
                    Column() {
                        HomeTopBar(
                            text = topBarTitle,
                            onMenuClick = { isMenuOpen = true },
                            onProfileClick = {
                                if (isLoggedIn) {
                                    navController.navigate(NavigationRoute.Profile(AuthState.userId.value!!))
                                } else {
                                    navController.navigate(NavigationRoute.Login)
                                }
                            }
                        )
                        SearchBar(value = searchQuery, onValueChange = { homeViewModel.onSearchQueryChange(it, currentUserId) })
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (homeState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                    ) {
                        if (searchQuery.isNotBlank()) {
                            if (searchResults.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                        Text("Nessuna ricetta trovata", color = Color.Gray)
                                    }
                                }
                            } else {
                                items(searchResults.entries.toList(), key = { it.key.recipeId }) { entry ->
                                    val recipe = entry.key
                                    val isFavorite = entry.value

                                    RecipePreviewCard({navController.navigate(NavigationRoute.RecipeDetails(recipe.recipeId))},
                                        recipe,isFavorite,{homeViewModel.actions.onFavorite(recipe, currentUserId)},
                                        {showLoginDialog = true})
                                }
                            }
                        } else {
                            items(homeState.sections) { section ->
                                Column {
                                    SectionHeader(title = section.title)

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        contentPadding = PaddingValues(
                                            horizontal = 16.dp,
                                            vertical = 8.dp
                                        )
                                    ) {
                                        items(section.recipes.keys.toList()) { recipe ->
                                            val isFavorite = section.recipes[recipe] ?: false
                                            val totalTime = recipe.preparation + recipe.cooking + (recipe.waiting ?: 0)

                                            RecipeCard(
                                                imageUrl = recipe.previewImageUrl,
                                                title = recipe.title,
                                                rating = recipe.averageRating.toDouble(),
                                                time = formatTime(totalTime),
                                                isFavorite = isFavorite,
                                                onCardClick = {
                                                    navController.navigate(NavigationRoute.RecipeDetails(recipe.recipeId))
                                                },
                                                onFavoriteClick = {
                                                    if (AuthState.isLoggedIn.value) {
                                                        homeViewModel.actions.onFavorite(recipe, currentUserId)
                                                    } else {
                                                        showLoginDialog = true
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isMenuOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.1f))
                    .clickable { isMenuOpen = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .fillMaxHeight()
                        .background(
                            color = Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                        )
                        .clickable(enabled = false) { }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { isMenuOpen = false }) {
                            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Indietro")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text("Menu", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.weight(1.3f))
                    }

                    categories.categories.sortedBy { it.order }
                        .forEach { CategoryMenuItem(it.name,
                            onCategoryClick = {
                                isMenuOpen = false
                                navController.navigate(NavigationRoute.CategoryRecipes(it.categoryId, it.name))
                            }
                        )}
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(text: String, onMenuClick: () -> Unit, onProfileClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(24.dp))
    ) {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
            title = { Text(text = text, fontWeight = FontWeight.SemiBold) },
            navigationIcon = {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
            },
            actions = {
                IconButton(onClick = onProfileClick) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "Profilo")
                }
            }
        )
    }
}

@Composable
fun SearchBar(value: String, onValueChange: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text("Search", color = Color.Gray) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF5F5F5),
            unfocusedContainerColor = Color(0xFFF5F5F5),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        )
    )
}

@Composable
fun CategoryMenuItem(title: String, onCategoryClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCategoryClick() }
            .padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}