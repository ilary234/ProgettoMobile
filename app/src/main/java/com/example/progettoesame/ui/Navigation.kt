package com.example.progettoesame.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.progettoesame.ui.screens.EditProfileScreen
import com.example.progettoesame.ui.screens.CategoryScreen
import com.example.progettoesame.ui.screens.ChangePasswordScreen
import com.example.progettoesame.ui.screens.HomeScreen
import com.example.progettoesame.ui.screens.LoginScreen
import com.example.progettoesame.ui.screens.NewRecipeScreen
import com.example.progettoesame.ui.screens.ProfileScreen
import com.example.progettoesame.ui.screens.RecipeScreen
import com.example.progettoesame.ui.screens.SettingScreen
import com.example.progettoesame.ui.screens.SignUpScreen
import com.example.progettoesame.ui.viewmodels.CategoryViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

sealed interface NavigationRoute {
    @Serializable data object Login : NavigationRoute
    @Serializable data object SignUp : NavigationRoute
    @Serializable data object Home : NavigationRoute
    @Serializable data class CategoryRecipes(val categoryId : Int) : NavigationRoute
    @Serializable data class RecipeDetails(val recipeId : Int) : NavigationRoute
    @Serializable data object NewRecipe : NavigationRoute
    @Serializable data class Profile(val userId : Int) : NavigationRoute
    @Serializable data class Settings(val userId : Int) : NavigationRoute
    @Serializable data object ChangePassword : NavigationRoute
    @Serializable data class EditProfile(val userId : Int) : NavigationRoute
}

@Composable
fun NavGraph(navController: NavHostController) {
    val isLoggedIn = false //da sistemare quando usi Auth

    NavHost(
        navController = navController,
        startDestination = NavigationRoute.Home
    ) {
        composable<NavigationRoute.Login> {
            LoginScreen(navController) {
                navController.navigate(NavigationRoute.SignUp)
            }
        }
        composable<NavigationRoute.SignUp> {
            SignUpScreen(navController) {
                navController.popBackStack()
            }
        }
        composable<NavigationRoute.Home> { HomeScreen(navController) }
        composable<NavigationRoute.CategoryRecipes> { backStackEntry ->
            //Inserire l'inizializzazione del ViewModel prima del composable e passarlo come parametro
            val categoryVm = koinViewModel<CategoryViewModel>()
            val route = backStackEntry.toRoute<NavigationRoute.CategoryRecipes>()
            CategoryScreen(navController, route.categoryId)
        }
        composable<NavigationRoute.RecipeDetails> { backStackEntry ->
            val route = backStackEntry.toRoute<NavigationRoute.RecipeDetails>()
            RecipeScreen(navController, route.recipeId)
        }
        composable<NavigationRoute.NewRecipe> { NewRecipeScreen(navController) }
        composable<NavigationRoute.Profile> { backStackEntry ->
            val route = backStackEntry.toRoute<NavigationRoute.Profile>()
            if (isLoggedIn) {
                ProfileScreen(navController, route.userId)
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(NavigationRoute.Login)
                }
            }
        }
        composable<NavigationRoute.Settings> { backStackEntry ->
            val route = backStackEntry.toRoute<NavigationRoute.Settings>()
            SettingScreen(navController, route.userId)
        }
        composable<NavigationRoute.ChangePassword> {
            ChangePasswordScreen(navController) {
                navController.popBackStack()
            }
        }
        composable<NavigationRoute.EditProfile> { backStackEntry ->
            val route = backStackEntry.toRoute<NavigationRoute.EditProfile>()
            EditProfileScreen(
                userId = route.userId,
                onSave = { newEmail, newUsername ->
                    // Qui andrà la logica per salvare sul DB
                    navController.popBackStack()
                }
            )
        }
    }
}