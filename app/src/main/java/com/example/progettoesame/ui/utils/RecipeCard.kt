package com.example.progettoesame.ui.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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