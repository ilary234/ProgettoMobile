@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.progettoesame.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.progettoesame.R
import com.example.progettoesame.data.database.Recipe
import com.example.progettoesame.ui.NavigationRoute
import com.example.progettoesame.ui.utils.AuthState
import com.example.progettoesame.ui.utils.LoginRequiredDialog
import com.example.progettoesame.ui.utils.createImageUriInGallery
import com.example.progettoesame.ui.utils.formatTime
import com.example.progettoesame.ui.utils.shareRecipe
import com.example.progettoesame.ui.viewmodels.ProfileViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel,
    userId: String
) {
    var selectedTab by remember { mutableStateOf(0) }

    val userRecipesLazyState = rememberLazyListState()
    val favoriteRecipesLazyState = rememberLazyListState()

    val isLoading by profileViewModel.isLoading.collectAsStateWithLifecycle()
    val user by profileViewModel.user.collectAsStateWithLifecycle()
    val userRecipes by profileViewModel.userRecipes.collectAsStateWithLifecycle()
    val favoriteRecipes by profileViewModel.favoriteRecipes.collectAsStateWithLifecycle()
    val isOwnProfile by profileViewModel.isOwnProfile.collectAsStateWithLifecycle()

    var showLoginDialog by remember { mutableStateOf(false) }

    val ctx = LocalContext.current
    var showImagePickerDialog by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val pickMediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            profileViewModel.updateProfileImage(userId, uri.toString())
        }
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { pictureTaken ->
        if (pictureTaken) {
            cameraUri?.let { uri ->
                profileViewModel.updateProfileImage(userId, uri.toString())
            }
        }
    }

    if (showLoginDialog) {
        LoginRequiredDialog(
            onDismiss = { showLoginDialog = false },
            onConfirm = {
                showLoginDialog = false
                navController.navigate(NavigationRoute.Login)
            }
        )
    }

    if (showImagePickerDialog) {
        AlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            title = { Text("Foto Profilo") },
            text = { Text("Scegli come inserire o cambiare la tua foto profilo:") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickMediaLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                        showImagePickerDialog = false
                    }
                ) {
                    Text("Galleria")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val uri = createImageUriInGallery(ctx)
                        if (uri != null) {
                            cameraUri = uri
                            takePhotoLauncher.launch(uri)
                        }
                        showImagePickerDialog = false
                    }
                ) {
                    Text("Fotocamera")
                }
            }
        )
    }

    LaunchedEffect(userId) {
        profileViewModel.loadProfile(userId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profilo", fontWeight = FontWeight.SemiBold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    if(isOwnProfile) {
                        IconButton(onClick = { navController.navigate(NavigationRoute.Settings) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Impostazioni")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFF7EAD0))
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    val username = if (isOwnProfile) {
                        AuthState.username.value!!
                    } else {
                        user?.username ?: "Utente"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(96.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF7EAD0))
                                    .clickable(enabled = isOwnProfile) {
                                        showImagePickerDialog = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (user?.profileImageUrl != null) {
                                    AsyncImage(
                                        model = user?.profileImageUrl,
                                        contentDescription = "Foto profilo",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        error = painterResource(R.drawable.ic_image_error)
                                    )
                                } else {
                                    Text(
                                        text = username.take(1).uppercase(Locale.ROOT),
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }

                            if (isOwnProfile && user?.profileImageUrl != null) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .offset(x = 2.dp, y = 2.dp)
                                        .background(Color.Red, shape = CircleShape)
                                        .clickable {
                                            profileViewModel.updateProfileImage(userId, null)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Elimina foto profilo",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(32.dp))

                        Row {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = (user?.recipeNumber ?: 0).toString(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Ricette", color = Color.Gray, fontSize = 14.sp)
                            }

                            Spacer(modifier = Modifier.width(24.dp))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val rating = user?.averageRating ?: 0.0f

                                    val formattedRating = if (rating % 1 == 0f) {
                                        rating.toInt().toString()
                                    } else {
                                        String.format(Locale.getDefault(), "%.1f", rating)
                                    }

                                    Text(
                                        text = formattedRating,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFB400), modifier = Modifier.size(18.dp))
                                }
                                Text("Valutazione", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    }

                    Text(
                        text = username,
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 24.dp),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (isOwnProfile) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFFF5F5F5))
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (selectedTab == 0) Color.White else Color.Transparent)
                                    .clickable { selectedTab = 0 },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Le mie ricette",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 0) Color.Black else Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (selectedTab == 1) Color.White else Color.Transparent)
                                    .clickable { selectedTab = 1 },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Preferiti",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 1) Color.Black else Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFFF5F5F5))
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Ricette pubblicate",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
                LazyColumn(
                    state = if (selectedTab == 0) userRecipesLazyState else favoriteRecipesLazyState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    if (isOwnProfile) {
                        if (selectedTab == 0) {
                            if (userRecipes.isEmpty()) {
                                item { EmptyStateMessage("Non hai ancora inserito nessuna ricetta.") }
                            } else {
                                items(userRecipes.entries.toList(), key = { it.key.recipeId }) { entry ->
                                    ProfileRecipeCard(
                                        recipe = entry.key,
                                        isFavorite = entry.value,
                                        showDeleteButton = true,
                                        onCardClick = { navController.navigate(NavigationRoute.RecipeDetails(entry.key.recipeId)) },
                                        onFavoriteClick = { profileViewModel.actions.onFavorite(entry.key) },
                                        onDeleteClick = { profileViewModel.actions.onDelete(entry.key) }
                                    )
                                }
                            }
                        } else {
                            if (favoriteRecipes.isEmpty()) {
                                item { EmptyStateMessage("Non hai ancora ricette salvate nei preferiti.") }
                            } else {
                                items(favoriteRecipes.entries.toList(), key = { it.key.recipeId }) { entry ->
                                    val isMyOwnRecipe = entry.key.author == user?.userId
                                    ProfileRecipeCard(
                                        recipe = entry.key,
                                        isFavorite = entry.value,
                                        showDeleteButton = isMyOwnRecipe,
                                        onCardClick = { navController.navigate(NavigationRoute.RecipeDetails(entry.key.recipeId)) },
                                        onFavoriteClick = { profileViewModel.actions.onFavorite(entry.key) },
                                        onDeleteClick = { profileViewModel.actions.onDelete(entry.key) }
                                    )
                                }
                            }
                        }
                    } else {
                        items(userRecipes.entries.toList(), key = { it.key.recipeId }) { entry ->
                            ProfileRecipeCard(
                                recipe = entry.key,
                                isFavorite = entry.value,
                                showDeleteButton = false,
                                onCardClick = { navController.navigate(NavigationRoute.RecipeDetails(entry.key.recipeId)) },
                                onFavoriteClick = {
                                    if (AuthState.userId.value != null) {
                                        profileViewModel.actions.onFavorite(entry.key)
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

@Composable
fun ProfileRecipeCard(
    recipe: Recipe,
    isFavorite: Boolean,
    showDeleteButton: Boolean,
    onCardClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDeleteClick: () -> Unit = {}
) {
    val ctx = LocalContext.current
    val totalTime = recipe.preparation + recipe.cooking + (recipe.waiting ?: 0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFFF7EAD0), shape = RoundedCornerShape(16.dp))
            .clickable { onCardClick() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = recipe.previewImageUrl,
                contentDescription = "Immagine di anteprima della ricetta: ${recipe.title}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.ic_image_error)
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                color = Color.Black
            )
            Text(
                text = formatTime(totalTime),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                repeat(5) { index ->
                    val active = index < recipe.averageRating.toInt()
                    Icon(
                        imageVector = if (active) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = null,
                        tint = if (active) Color(0xFFFFB400) else Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = recipe.averageRating.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { shareRecipe(ctx, recipe.title, recipe.previewImageUrl) }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Condividi Ricetta",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (showDeleteButton) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Elimina Ricetta",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Stato Preferiti",
                    tint = if (isFavorite) Color.Red else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = Color.Gray, fontSize = 15.sp)
    }
}