package com.example.progettoesame.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.progettoesame.ui.screens.CategoryScreen
import com.example.progettoesame.ui.screens.ChangePasswordScreen
import com.example.progettoesame.ui.screens.EditProfileScreen
import com.example.progettoesame.ui.screens.HomeScreen
import com.example.progettoesame.ui.screens.InitialErrorScreen
import com.example.progettoesame.ui.screens.LoginScreen
import com.example.progettoesame.ui.screens.NewRecipeScreen
import com.example.progettoesame.ui.screens.ProfileScreen
import com.example.progettoesame.ui.screens.RecipeScreen
import com.example.progettoesame.ui.screens.SettingScreen
import com.example.progettoesame.ui.screens.SignUpScreen
import com.example.progettoesame.ui.viewmodels.AuthViewModel
import com.example.progettoesame.ui.viewmodels.CategoryViewModel
import com.example.progettoesame.ui.viewmodels.HomeViewModel
import com.example.progettoesame.ui.viewmodels.InitialErrorViewModel
import com.example.progettoesame.ui.viewmodels.NewRecipeViewModel
import com.example.progettoesame.ui.viewmodels.RecipeViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

sealed interface NavigationRoute {
    @Serializable data object Login : NavigationRoute
    @Serializable data object SignUp : NavigationRoute
    @Serializable data object Home : NavigationRoute
    @Serializable data object Error : NavigationRoute
    @Serializable data class CategoryRecipes(val categoryId : String, val categoryName : String) : NavigationRoute
    @Serializable data class RecipeDetails(val recipeId : String) : NavigationRoute
    @Serializable data class NewRecipe(val recipeId : String? = null) : NavigationRoute
    @Serializable data class Profile(val userId : String) : NavigationRoute
    @Serializable data object Settings : NavigationRoute
    @Serializable data object ChangePassword : NavigationRoute
    @Serializable data object EditProfile : NavigationRoute
    @Serializable data object ResetPassword : NavigationRoute
}

@Composable
fun NavGraph(navController: NavHostController, startDestination: NavigationRoute) {
    val authVM: AuthViewModel = koinViewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<NavigationRoute.Login> { LoginScreen(navController, authVM) }
        composable<NavigationRoute.SignUp> { SignUpScreen(navController, authVM) }
        composable<NavigationRoute.Home> {
            val homeVm = koinViewModel<HomeViewModel>()
            HomeScreen(navController, homeVm)
        }
        composable<NavigationRoute.Error> {
            val initialErrorVm = koinViewModel<InitialErrorViewModel>()
            InitialErrorScreen(navController, initialErrorVm)
        }
        composable<NavigationRoute.CategoryRecipes> { backStackEntry ->
            val categoryVm = koinViewModel<CategoryViewModel>()
            val route = backStackEntry.toRoute<NavigationRoute.CategoryRecipes>()
            CategoryScreen(navController, route.categoryId, route.categoryName, categoryVm)
        }
        composable<NavigationRoute.RecipeDetails> { backStackEntry ->
            val recipeVm = koinViewModel<RecipeViewModel>()
            val route = backStackEntry.toRoute<NavigationRoute.RecipeDetails>()
            RecipeScreen(navController, recipeVm, route.recipeId)
        }
        composable<NavigationRoute.NewRecipe> { backStackEntry ->
            val route = backStackEntry.toRoute<NavigationRoute.NewRecipe>()
            val newRecipeVm = koinViewModel<NewRecipeViewModel>()
            NewRecipeScreen(navController, newRecipeVm, route.recipeId)
        }
        composable<NavigationRoute.Profile> { ProfileScreen(navController) }
        composable<NavigationRoute.Settings> { SettingScreen(navController, authVM) }
        composable<NavigationRoute.ChangePassword> { ChangePasswordScreen(navController, authVM) }
        composable<NavigationRoute.EditProfile> { EditProfileScreen(navController, authVM) }
        composable<NavigationRoute.ResetPassword> { ChangePasswordScreen(navController, authVM, isFromReset = true) }
    }
}