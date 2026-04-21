package com.example.progettoesame.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.navigation.NavHostController
import com.example.progettoesame.ui.NavigationRoute
import com.example.progettoesame.ui.utils.RecipeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController)  {
    var isMenuOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.White,
            topBar = {
                // alpha per nascondere senza rimuovere lo spazio
                Box(modifier = Modifier.alpha(if (isMenuOpen) 0f else 1f)) {
                    HomeTopBar(
                        onMenuClick = { isMenuOpen = true },
                        onProfileClick = {
                            navController.navigate(NavigationRoute.Profile(userId = 1)) // da sistemare
                        }
                    )
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                // SearchBar: resta nel layout ma diventa invisibile se il menu è aperto (non so se va bene fatta così)
                item {
                    Box(modifier = Modifier.alpha(if (isMenuOpen) 0f else 1f)) {
                        SearchBar()
                    }
                }

                item {
                    SectionHeader(title = "In primo piano")
                    LazyRow(contentPadding = PaddingValues(horizontal = 8.dp)) {
                        items(3) { RecipeCard(title = "Titolo", time = "Tempo", rating = 2.0) }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionHeader(title = "Primi piatti")
                    LazyRow(contentPadding = PaddingValues(horizontal = 8.dp)) {
                        items(3) { RecipeCard(title = "Titolo", time = "Tempo", rating = 2.0) }
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

                    val categorie = listOf("Antipasti", "Primi Piatti", "Secondi Piatti", "Contorni", "Dessert", "Lievitati")
                    categorie.forEach { CategoryMenuItem(title = it) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(onMenuClick: () -> Unit, onProfileClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(24.dp))
    ) {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
            title = { Text("NomeApp", fontWeight = FontWeight.SemiBold) },
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
fun SearchBar() {
    TextField(
        value = "",
        onValueChange = {},
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
fun CategoryMenuItem(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(20.dp))
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
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = CircleShape,
            color = Color(0xFFF0F0F0),
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "Vedi tutto",
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}