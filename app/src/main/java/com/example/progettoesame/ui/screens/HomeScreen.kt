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
import com.example.progettoesame.ui.theme.AppTheme
import com.example.progettoesame.ui.utils.AuthState
import com.example.progettoesame.ui.utils.LoginRequiredDialog
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

    val isDark = AppTheme.isDark
    val currentPastel = AppTheme.pastelColor

    val appBackgroundColor = if (isDark) Color.Black else Color.White
    val appTextColor = if (isDark) Color.White else Color.Black
    val containerSectionColor = if (isDark) currentPastel.darkColor else currentPastel.lightColor

    if (showLoginDialog) {
        LoginRequiredDialog(
            containerColor = containerSectionColor,
            textColor = appTextColor,
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
            containerColor = appBackgroundColor,
            topBar = {
                Box(modifier = Modifier.alpha(if (isMenuOpen) 0f else 1f)) {
                    Column {
                        HomeTopBar(
                            text = topBarTitle,
                            onMenuClick = { isMenuOpen = true },
                            onProfileClick = {
                                if (isLoggedIn) {
                                    navController.navigate(NavigationRoute.Profile(AuthState.userId.value!!))
                                } else {
                                    navController.navigate(NavigationRoute.Login)
                                }
                            },
                            containerColor = containerSectionColor,
                            textColor = appTextColor
                        )
                        SearchBar(
                            value = searchQuery,
                            onValueChange = { homeViewModel.onSearchQueryChange(it) },
                            containerColor = containerSectionColor,
                            textColor = appTextColor
                        )
                    }
                }
            },
            floatingActionButton = {
                if (!isMenuOpen) {
                    FloatingActionButton(
                        onClick = {
                            if (isLoggedIn) {
                                navController.navigate(NavigationRoute.NewRecipe())
                            } else {
                                showLoginDialog = true
                            }
                        },
                        modifier = Modifier.padding(bottom = 16.dp),
                        containerColor = containerSectionColor,
                        contentColor = appTextColor,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Crea Nuova Ricetta"
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (homeState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = appTextColor
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        if (searchQuery.isNotBlank()) {
                            if (searchResults.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                        Text("Nessuna ricetta trovata", color = appTextColor.copy(alpha = 0.6f))
                                    }
                                }
                            } else {
                                items(searchResults.entries.toList(), key = { it.key.recipeId }) { entry ->
                                    val recipe = entry.key
                                    val isFavorite = entry.value

                                    RecipePreviewCard(
                                        onClick = { navController.navigate(NavigationRoute.RecipeDetails(recipe.recipeId)) },
                                        recipe = recipe,
                                        isFavorite = isFavorite,
                                        containerColor = containerSectionColor,
                                        textColor = appTextColor,
                                        onLoggedFavourite = { homeViewModel.actions.onFavorite(recipe, currentUserId) },
                                        onUnloggedFavourite = { showLoginDialog = true }
                                    )
                                }
                            }
                        } else {
                            items(homeState.sections) { section ->
                                Column {
                                    SectionHeader(title = section.title, textColor = appTextColor)

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
                                                textColor = appTextColor,
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
                    .background(appTextColor.copy(alpha = 0.1f))
                    .clickable { isMenuOpen = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .fillMaxHeight()
                        .background(
                            color = containerSectionColor,
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
                            Icon(
                                imageVector = Icons.Default.ArrowBackIosNew,
                                contentDescription = "Indietro",
                                tint = appTextColor
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text("Menu", color = appTextColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.weight(1.3f))
                    }

                    categories.categories.sortedBy { it.order }
                        .forEach {
                            CategoryMenuItem(
                                title = it.name,
                                textColor = appTextColor,
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
fun HomeTopBar(text: String, onMenuClick: () -> Unit, onProfileClick: () -> Unit, containerColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(containerColor, shape = RoundedCornerShape(24.dp))
    ) {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
            title = { Text(text = text, color = textColor, fontWeight = FontWeight.SemiBold) },
            navigationIcon = {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = textColor)
                }
            },
            actions = {
                IconButton(onClick = onProfileClick) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "Profilo", tint = textColor)
                }
            }
        )
    }
}

@Composable
fun SearchBar(value: String, onValueChange: (String) -> Unit, containerColor: Color, textColor: Color) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Search", color = textColor.copy(alpha = 0.5f)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = textColor.copy(alpha = 0.5f)) },
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor
        )
    )
}

@Composable
fun CategoryMenuItem(title: String, textColor: Color, onCategoryClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCategoryClick() }
            .padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = textColor.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun SectionHeader(title: String, textColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = textColor,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}