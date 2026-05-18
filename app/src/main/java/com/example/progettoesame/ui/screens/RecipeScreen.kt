package com.example.progettoesame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.progettoesame.ui.NavigationRoute
import com.example.progettoesame.ui.utils.BulletPointText
import com.example.progettoesame.ui.utils.formatTime
import com.example.progettoesame.ui.utils.LoginRequiredDialog
import com.example.progettoesame.ui.utils.PreviewCard
import com.example.progettoesame.ui.utils.RatingRow
import com.example.progettoesame.ui.utils.shareRecipe
import com.example.progettoesame.ui.viewmodels.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(navController: NavController, recipeViewModel: RecipeViewModel, recipeId : String) {
    val recipeState by recipeViewModel.recipe.collectAsStateWithLifecycle()
    val stepsState by recipeViewModel.stepsState.collectAsStateWithLifecycle()
    val isFavorite by recipeViewModel.isFavorite.collectAsStateWithLifecycle()
    val rating by recipeViewModel.rate.collectAsStateWithLifecycle()
    val currentSpeakingId by recipeViewModel.currentSpeakingId.collectAsStateWithLifecycle()
    val isFullAudioStarted by recipeViewModel.isFullAudioStarted.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    val ctx = LocalContext.current


    LaunchedEffect(recipeId) {
        recipeViewModel.fetchRecipe(recipeId)
        if(true/*isLoggedIn*/) { //TODO
            recipeViewModel.getUserRecipeData(recipeId, "userId")
        }
        recipeViewModel.initTts(ctx)
    }

    if(recipeState == null) {
        Scaffold { paddingValues ->
            CircularProgressIndicator(
                modifier = Modifier.padding(paddingValues)
            )
        }
        return
    }

    if (showDialog) {
        LoginRequiredDialog(
            onDismiss = { showDialog = false },
            onConfirm = { showDialog = false
                navController.navigate(NavigationRoute.Login) }
        )
    }

    val currentData = recipeState ?: return
    val recipe = currentData.recipe
    val author = currentData.author

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = recipe.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PreviewCard(recipe.previewImageUrl, recipe.title)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(author,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    color = MaterialTheme.colorScheme.primary, //TODO mettere colore secondary (arancione scuro in questo caso)
                    modifier = Modifier.weight(1f)
                        .clickable{navController.navigate(NavigationRoute.Profile(0/*recipe.author*/))}) //TODO
                RatingRow(recipe.averageRating)
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Tempi:", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Normal)
                BulletPointText("Preparazione: ${formatTime(recipe.preparation)}")
                if (recipe.waiting != null) {
                    BulletPointText("Riposo: ${formatTime(recipe.waiting)}")
                }
                BulletPointText("Cottura: ${formatTime(recipe.cooking)}")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { shareRecipe(ctx, recipe.title, recipe.previewImageUrl)}) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.Gray)
                }
                IconButton(onClick = {
                    if (false/*isLoggedIn()*/) {//TODO
                        recipeViewModel.actions.onFavorite(recipe.recipeId, "userId")
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

            Text("Ingredienti", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(color = Color(0xfff7ead0), shape = RoundedCornerShape(12.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recipe.ingredients.forEach { ingredient ->
                    BulletPointText(ingredient.name + ": " + ingredient.quantity + " " + ingredient.unit)
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Procedimento", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isSpeaking = currentSpeakingId?.startsWith("full_recipe") ?: false
                        IconButton(onClick = { recipeViewModel.onPlayPauseButtonClick() }) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Pause else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = if (isSpeaking) "Pause audio Icon" else "Play audio Icon",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Gray
                            )
                        }

                        if (isFullAudioStarted) {
                            IconButton(onClick = { recipeViewModel.onFullRecipeStopButtonClick() }) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop audio Icon",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                }


                recipe.steps.sortedBy { it.number }.forEach { step ->
                    val isOpen = stepsState.stepStates[step] ?: false
                    val stepAudioId = "step_${step.number}"
                    StepElem(step.number, isOpen, currentSpeakingId, stepAudioId,
                        { recipeViewModel.onStepStopButtonClick() },
                        { text, stepAudioId -> recipeViewModel.onStepPlayButtonClick(text, stepAudioId) },
                        { recipeViewModel.actions.onToggleStep(step)}, step.description, step.imageUrls)
                }
            }


            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Lascia una recensione:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Normal)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(5) { index ->
                        val active = index < rating
                        IconButton(
                            modifier = Modifier.size(20.dp),
                            onClick = {
                            if (false/*isLoggedIn()*/) {//TODO
                                recipeViewModel.actions.onRate(recipe.recipeId, "userId", index + 1)
                            } else {
                                showDialog = true
                            }}) {
                            Icon(
                                imageVector = if (active) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = null,
                                tint = if (active) Color(0xFFFFB400) else Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun StepElem(number: Int, isOpen: Boolean,
             currentSpeakingId: String?, stepAudioId: String, onStop: () -> Unit, onPlay: (String, String) -> Unit,
             onToggleStep: () -> Unit, description: String, imageUrls: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f)
                    .background(
                        color = Color(0xfff7ead0),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(start = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Passaggio $number", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Normal)
                IconButton(onClick =  onToggleStep ) {
                    Icon(
                        imageVector = if (isOpen) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Arrow Icon",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Gray
                    )
                }
            }
            StepAudioButton(currentSpeakingId, stepAudioId, onStop, {onPlay(description, stepAudioId)})
        }
        if (isOpen) {
            if (imageUrls.isNotEmpty()) {
                val pagerState = rememberPagerState(
                    pageCount = { imageUrls.size }
                )

                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 32.dp),
                    pageSpacing = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    PreviewCard(
                        imageUrls[page],
                        "Passaggio ${number} - Foto ${page + 1}"
                    )
                }
            }

            Text(text = description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun StepAudioButton(currentSpeakingId: String?, stepAudioId: String, onStop: () -> Unit, onPlay: () -> Unit) {
    IconButton(onClick = { if (currentSpeakingId == stepAudioId) onStop() else onPlay() }) {
        Icon(
            imageVector = if (currentSpeakingId == stepAudioId) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = if (currentSpeakingId == stepAudioId) "Stop audio Icon" else "Play audio Icon",
            modifier = Modifier.size(24.dp),
            tint = Color.Gray
        )
    }
}
