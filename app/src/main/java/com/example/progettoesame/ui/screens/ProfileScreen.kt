package com.example.progettoesame.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(navController: NavController, userId: Int) {
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(28.dp))
            Icon(Icons.Default.Settings, null, modifier = Modifier.size(28.dp))
        }

        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(100.dp).clip(CircleShape).background(Color(0xFFFFD54F)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = Color.White)
            }
            Spacer(modifier = Modifier.width(32.dp))
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("30", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Ricette", color = Color.Gray, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(24.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("4.5", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Star, null, modifier = Modifier.size(18.dp))
                    }
                    Text("Valutazione", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }

        Text("Username", modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 24.dp), fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(45.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF0F0F0))
        ) {
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight().background(if (selectedTab == 0) Color(0xFFDCDCDC) else Color.Transparent).clickable { selectedTab = 0 },
                contentAlignment = Alignment.Center
            ) {
                Text("Le mie ricette", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
            }
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight().background(if (selectedTab == 1) Color(0xFFDCDCDC) else Color.Transparent).clickable { selectedTab = 1 },
                contentAlignment = Alignment.Center
            ) {
                Text("Preferiti", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f) // prende tutto lo spazio disponibile tranne lo spacer sotto
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(5) { ProfileRecipeItem() }
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .background(Color.White)
        )
    }
}

@Composable
fun ProfileRecipeItem() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(200.dp)) {
            Box(modifier = Modifier.fillMaxSize().background(Color.LightGray))
        }
        Row(modifier = Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { }) { Icon(Icons.Default.Share, null, modifier = Modifier.size(24.dp)) }
            IconButton(onClick = { }) { Icon(Icons.Default.Delete, null, modifier = Modifier.size(24.dp)) }
        }
        Text("Titolo Ricetta", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 4.dp))
        Text("30 min", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 4.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp, start = 4.dp)) {
            repeat(5) { index -> Icon(Icons.Default.Star, null, tint = if (index < 2) Color(0xFFFFB400) else Color.LightGray, modifier = Modifier.size(16.dp)) }
            Text(" 2.0", color = Color.Gray, fontSize = 12.sp)
        }
    }
}