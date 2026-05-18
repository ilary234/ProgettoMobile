package com.example.progettoesame.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.progettoesame.R // pacchetto probabilmente da modificare
import com.example.progettoesame.data.database.Recipe
import com.example.progettoesame.ui.NavigationRoute

@Composable
fun RecipeCard(title: String, time: String, rating: Double) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .padding(8.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            // Placeholder per l'immagine - Sostituisci R.drawable.food_placeholder con la tua risorsa
             /*Image(
                painter = painterResource(id = R.drawable.food_placeholder),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )*/
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = time,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            repeat(5) { index ->
                val active = index < rating.toInt()
                Icon(
                    imageVector = if (active) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = null,
                    tint = if (active) Color(0xFFFFB400) else Color.LightGray,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = rating.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun RecipePreviewCard(onClick: () -> Unit, recipe: Recipe, isFavorite: Boolean,
                      onLoggedFavourite: () -> Unit, onUnloggedFavourite: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 8.dp)
            .background(color = Color(0xfff7ead0), shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
            .clickable{
                onClick()
            },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        PreviewCard(recipe.previewImageUrl, recipe.title)

        val time = recipe.preparation + recipe.cooking + (recipe.waiting ?: 0)
        InfoPreview(recipe.title, formatTime(time), recipe.averageRating)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (AuthState.isLoggedIn.value) {
                    onLoggedFavourite()
                } else {
                    onUnloggedFavourite()
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
    }
}

@Composable
fun PreviewCard(imageUrl: String, recipeName: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Immagine di anteprima della ricetta: $recipeName",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.ic_image_error)
        )
    }
}

@Composable
fun InfoPreview(title: String, time: String, rating: Float) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = time,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        RatingRow(rating)
    }
}

@Composable
fun RatingRow(rating: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        repeat(5) { index ->
            val active = index < rating.toInt()
            Icon(
                imageVector = if (active) Icons.Default.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = if (active) Color(0xFFFFB400) else Color.LightGray,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = rating.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun BulletPointText(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text("• ", fontWeight = FontWeight.Bold)
        Text(text = text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Normal)
    }
}

@Composable
fun LoginRequiredDialog(onDismiss: () -> Unit, onConfirm: () -> Unit){
    AlertDialog(
        title = {Text("Login Richiesto")},
        text = {Text("Per eseguire questa azione è necessario effettuare il login.")},
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Accedi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        },
        containerColor = Color.White,
        textContentColor = Color.Black,
        titleContentColor = Color.Black
    )
}