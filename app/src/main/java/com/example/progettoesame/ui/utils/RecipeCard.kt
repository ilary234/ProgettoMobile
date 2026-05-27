package com.example.progettoesame.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StarHalf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.progettoesame.R
import com.example.progettoesame.data.database.Recipe

@Composable
fun RecipeCard(textColor: Color, imageUrl: String, title: String, rating: Float, time: String, isFavorite: Boolean, onCardClick: () -> Unit, onFavoriteClick: () -> Unit) {
    val roundedRating = getRoundedRating(rating)
    val formattedRating = formatRating(roundedRating)

    Column(
        modifier = Modifier
            .width(160.dp)
            .padding(2.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clickable { onCardClick() }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Immagine di $title",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = painterResource(R.drawable.ic_image_error)
                )

                IconButton(
                    onClick = { onFavoriteClick() },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Inserisci nei preferiti",
                        tint = if (isFavorite) Color.Red else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            color = textColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = time,
            style = MaterialTheme.typography.bodySmall,
            color = textColor.copy(alpha = 0.6f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            repeat(5) { index ->
                val starIndex = index + 1
                val icon = when {
                    roundedRating >= starIndex -> Icons.Default.Star
                    roundedRating >= starIndex - 0.75 -> Icons.AutoMirrored.Outlined.StarHalf
                    else -> Icons.Outlined.StarBorder
                }
                val tint = if (rating >= starIndex - 0.75) Color(0xFFFFB400) else textColor.copy(alpha = 0.3f)

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = formattedRating,
                style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun RecipePreviewCard(onClick: () -> Unit, recipe: Recipe, isFavorite: Boolean,
                      containerColor: Color, textColor: Color,
                      onLoggedFavourite: () -> Unit, onUnloggedFavourite: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 8.dp)
            .background(color = containerColor, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
            .clickable{
                onClick()
            },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        PreviewCard(recipe.previewImageUrl, recipe.title)

        val time = recipe.preparation + recipe.cooking + (recipe.waiting ?: 0)
        InfoPreview(recipe.title, formatTime(time), recipe.averageRating, textColor)

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
                    tint = if (isFavorite) Color.Red else textColor.copy(alpha = 0.6f)
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
fun InfoPreview(title: String, time: String, rating: Float, textColor: Color) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            color = textColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = time,
            style = MaterialTheme.typography.bodySmall,
            color = textColor.copy(alpha = 0.7f)
        )
        RatingRow(rating, textColor)
    }
}

@Composable
fun RatingRow(rating: Float, textColor: Color) {
    val roundedRating = getRoundedRating(rating)
    val formattedRating = formatRating(roundedRating)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        repeat(5) { index ->
            val starIndex = index + 1
            val icon = when {
                roundedRating >= starIndex -> Icons.Default.Star
                roundedRating >= starIndex - 0.75 -> Icons.AutoMirrored.Outlined.StarHalf
                else -> Icons.Outlined.StarBorder
            }
            val tint = if (rating >= starIndex - 0.75) Color(0xFFFFB400) else textColor.copy(alpha = 0.3f)

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = formattedRating,
            style = MaterialTheme.typography.bodySmall,
            color = textColor.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
    }
}

@Composable
fun BulletPointText(text: String, textColor: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Text("• ", color = textColor, fontWeight = FontWeight.Bold)
        Text(text = text, color = textColor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Normal)
    }
}

@Composable
fun LoginRequiredDialog(
    containerColor: Color,
    textColor: Color,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit){
    AlertDialog(
        title = {Text("Login Richiesto")},
        text = {Text("Per eseguire questa azione è necessario effettuare il login.")},
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Accedi", color = textColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla", color = textColor.copy(alpha = 0.6f))
            }
        },
        containerColor = containerColor,
        textContentColor = textColor,
        titleContentColor = textColor
    )
}