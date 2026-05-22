package com.example.progettoesame.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.progettoesame.ui.NavigationRoute
import com.example.progettoesame.ui.theme.AppTheme
import com.example.progettoesame.ui.utils.AppPastelColor
import com.example.progettoesame.ui.utils.Theme
import com.example.progettoesame.ui.viewmodels.SettingViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingScreen(navController: NavHostController, settingViewModel: SettingViewModel) {
    val isDarkTheme = AppTheme.isDark
    val currentPastel = AppTheme.pastelColor

    val appBackgroundColor = if (isDarkTheme) Color.Black else Color.White
    val appTextColor = if (isDarkTheme) Color.White else Color.Black
    val containerSectionColor = if (isDarkTheme) currentPastel.darkColor else currentPastel.lightColor
    val surfaceColor = if (isDarkTheme) currentPastel.lightColor else currentPastel.darkColor

    Scaffold(
        containerColor = appBackgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Impostazioni", fontWeight = FontWeight.Bold, color = appTextColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro", tint = appTextColor)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = appTextColor,
                    navigationIconContentColor = appTextColor
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Account",
                color = appTextColor,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            SettingsItem(
                icon = Icons.Default.Lock,
                label = "Cambia Password",
                hasArrow = true,
                containerColor = containerSectionColor,
                textColor = appTextColor,
                surfaceColor = surfaceColor,
                iconColor = appBackgroundColor,
                onClick = { navController.navigate(NavigationRoute.ChangePassword) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingsItem(
                icon = Icons.Default.Person,
                label = "Modifica Profilo",
                hasArrow = true,
                containerColor = containerSectionColor,
                textColor = appTextColor,
                surfaceColor = surfaceColor,
                iconColor = appBackgroundColor,
                onClick = { navController.navigate(NavigationRoute.EditProfile) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingsItem(
                icon = Icons.Default.ExitToApp,
                label = "Esci dall'account",
                hasArrow = false,
                containerColor = containerSectionColor,
                textColor = appTextColor,
                onClick = {
                    settingViewModel.logout {
                        navController.navigate(NavigationRoute.Home) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Preferenze",
                color = appTextColor,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = containerSectionColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tema",
                        fontWeight = FontWeight.Medium,
                        color = appTextColor
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isDarkTheme) "Scuro" else "Chiaro",
                            color = appTextColor.copy(alpha = 0.7f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { checked ->
                                val targetTheme = if (checked) Theme.Dark else Theme.Light
                                settingViewModel.actions.setTheme(targetTheme)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color.White.copy(alpha = 0.4f),
                                checkedBorderColor = Color.White,

                                uncheckedThumbColor = Color(0xFF8E8E93),
                                uncheckedTrackColor = containerSectionColor,
                                uncheckedBorderColor = Color(0xFF8E8E93)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Colori:",
                color = appTextColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                maxItemsInEachRow = 4
            ) {
                AppPastelColor.entries.forEach { pastelEnum ->
                    val isSelected = currentPastel == pastelEnum
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(75.dp)
                            .clip(CircleShape)
                            .background(if (isDarkTheme) pastelEnum.darkColor else pastelEnum.lightColor) //da eliminare se non piace a ila
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) appTextColor else Color.LightGray,
                                shape = CircleShape
                            )
                            .clickable {
                                settingViewModel.actions.setPastelColor(pastelEnum)
                            }
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selezionato",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    label: String,
    hasArrow: Boolean,
    containerColor: Color,
    surfaceColor: Color? = null,
    iconColor: Color? = null,
    textColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = textColor.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.weight(1f)
            )

            if (hasArrow) {
                Surface(
                    shape = CircleShape,
                    color = surfaceColor!!,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.padding(4.dp),
                        tint = iconColor!!
                    )
                }
            }
        }
    }
}