package com.example.progettoesame.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.progettoesame.ui.NavigationRoute
import com.example.progettoesame.ui.utils.BulletPointText
import com.example.progettoesame.ui.utils.LoginRequiredDialog
import com.example.progettoesame.ui.utils.PreviewCard
import com.example.progettoesame.ui.utils.RatingRow
import com.example.progettoesame.ui.viewmodels.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(navController: NavController, recipeViewModel: RecipeViewModel, recipeId : String) {
    val recipeState by recipeViewModel.recipe.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(recipeId) {
        recipeViewModel.fetchRecipe(recipeId)
    }

    val currentRecipe = recipeState?: run {
        Scaffold {
            CircularProgressIndicator()
        }
        return
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
            Text("Tempi:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Normal)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Tempi:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Normal)
                BulletPointText("Preparazione: ${FormatTime(currentRecipe.preparation)}")
                if (currentRecipe.waiting != null) {
                    BulletPointText("Riposo: ${FormatTime(currentRecipe.waiting)}")
                }
                BulletPointText("Cottura: ${FormatTime(currentRecipe.cooking)}")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Azione Share */ }) { //TODO
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
                IconButton(onClick = {
                    if (false/*isLoggedIn()*/) {
                        //recipeViewModel.actions.onFavorite(recipe, "userId") //TODO
                    } else {
                        showDialog = true
                    }
                }) {
                    val isFavorite = true //TODO recipeViewModel.isFavorite(recipeId)
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

            //Aggiungi sezioni passi e procedimento

        }
    }
}

fun FormatTime(time: Int) : String {
    val hours = time / 60
    val minutes = time % 60

    val formattedTime = when {
        time < 60 -> "$time min"
        minutes == 0 -> "${hours} h"
        else -> "${hours} h ${minutes} min"
    }
    return formattedTime
}