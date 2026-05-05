package com.example.progettoesame.ui.screens

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(navController: NavController, recipeViewModel: RecipeViewModel, recipeId : String) {
    val recipeState by recipeViewModel.recipe.collectAsStateWithLifecycle()
    val stepsState by recipeViewModel.stepsState.collectAsStateWithLifecycle()
    val isFavorite by recipeViewModel.isFavorite.collectAsStateWithLifecycle()
    val rating by recipeViewModel.rate.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }

    val ctx = LocalContext.current

    val tts = remember { TextToSpeech(ctx) { status ->
        }.apply {
            setLanguage(Locale.getDefault())
            setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    isSpeaking = true
                }
                override fun onDone(utteranceId: String?) {
                    isSpeaking = false
                }
                override fun onError(utteranceId: String?) {
                    isSpeaking = false
                }
            })
        }
    }

    LaunchedEffect(recipeId) {
        recipeViewModel.fetchRecipe(recipeId)
        if(true/*isLoggedIn*/) { //TODO
            recipeViewModel.getUserRecipeData(recipeId, "userId")
        }
    }

    val currentRecipe = recipeState?: run {
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
            onConfirm = { navController.navigate(NavigationRoute.Login) }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = currentRecipe.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PreviewCard(currentRecipe.previewImageUrl, currentRecipe.title)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(currentRecipe.author, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                RatingRow(currentRecipe.averageRating)
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Tempi:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Normal)
                BulletPointText("Preparazione: ${formatTime(currentRecipe.preparation)}")
                if (currentRecipe.waiting != null) {
                    BulletPointText("Riposo: ${formatTime(currentRecipe.waiting)}")
                }
                BulletPointText("Cottura: ${formatTime(currentRecipe.cooking)}")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { shareRecipe(ctx, currentRecipe.title, currentRecipe.previewImageUrl)}) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
                IconButton(onClick = {
                    if (true/*isLoggedIn()*/) {//TODO
                        recipeViewModel.actions.onFavorite(currentRecipe, "userId")
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
            Surface(
                color = Color(0xFFEEEEEE),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    currentRecipe.ingredients.forEach { ingredient ->
                        BulletPointText(ingredient.name + ":" + ingredient.quantity + ingredient.unit)
                    }
                }
            }

            Row() {
                Text("Procedimento", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = {
                    if (isSpeaking) {
                        tts.stop()
                        isSpeaking = false
                    } else {
                        val text = currentRecipe.steps
                            .sortedBy { it.number }
                            .joinToString(separator = ". ") { it.description }
                        val params = Bundle()
                        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "recipe_id")
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "recipe_id")
                    }
                }) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = "Play/Stop text-to-speech Icon",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Gray
                    )
                }
            }
            currentRecipe.steps.sortedBy { it.number }.forEach { step ->
                val isOpen = stepsState.stepStates[step] ?: false
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Passaggio ${step.number}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Normal)
                        IconButton(onClick = { recipeViewModel.actions.onToggleStep(step) }) {
                            Icon(
                                imageVector = if (isOpen) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Arrow Icon",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                    if (isOpen && step.imageUrls.isNotEmpty()) {
                        val pagerState = rememberPagerState(
                            pageCount = { step.imageUrls.size }
                        )

                        HorizontalPager(
                            state = pagerState,
                            contentPadding = PaddingValues(horizontal = 32.dp),
                            pageSpacing = 16.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) { page ->
                            PreviewCard(step.imageUrls[page],"Passaggio ${step.number} - Foto ${page + 1}"
                            )
                        }

                        Text(text = step.description, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Lascia una recensione:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Normal)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    repeat(5) { index ->
                        val active = index < rating
                        IconButton(onClick = {
                            if (true/*isLoggedIn()*/) {//TODO
                                recipeViewModel.actions.onRate(currentRecipe, "userId", index + 1)
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
