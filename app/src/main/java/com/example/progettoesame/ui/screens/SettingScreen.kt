package com.example.progettoesame.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(navController: NavController, userId: Int) {
    var isDarkTheme by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Impostazioni", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Account",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            SettingsItem(icon = Icons.Default.Lock, label = "Cambia Password", hasArrow = true)
            Spacer(modifier = Modifier.height(12.dp))
            SettingsItem(icon = Icons.Default.Person, label = "Modifica Profilo", hasArrow = true)
            Spacer(modifier = Modifier.height(12.dp))
            SettingsItem(icon = Icons.Default.ExitToApp, label = "Esci dall'account", hasArrow = false)

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Preferenze",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF2F2F2),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tema", fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isDarkTheme) "Scuro" else "Chiaro",
                            color = Color.Black,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { isDarkTheme = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color.DarkGray
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Colori:",
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val colorGradients = listOf(
                listOf(Color(0xFFFFF176), Color(0xFFF9A825)), // Giallo
                listOf(Color(0xFFA5D6A7), Color(0xFF2E7D32)), // Verde
                listOf(Color(0xFFE1BEE7), Color(0xFF7B1FA2)), // Viola
                listOf(Color(0xFFFF8A65), Color(0xFFC62828)), // Rosso
                listOf(Color(0xFF4FC3F7), Color(0xFF1565C0))  // Blu
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                maxItemsInEachRow = 4
            ) {
                colorGradients.forEach { colors ->
                    Box(
                        modifier = Modifier
                            .size(75.dp)
                            .background(
                                brush = Brush.verticalGradient(colors),
                                //shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { /* Logica selezione colore */ }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, label: String, hasArrow: Boolean) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF2F2F2),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Azione al click */ }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            if (hasArrow) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}