package com.example.progettoesame

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.progettoesame.data.AuthManager
import com.example.progettoesame.data.HardDeleteWorker
import com.example.progettoesame.data.SyncManager
import com.example.progettoesame.data.SyncWorker
import com.example.progettoesame.data.database.ProjectDatabase
import com.example.progettoesame.data.repositories.AuthRepository
import com.example.progettoesame.data.repositories.CategoryRepository
import com.example.progettoesame.data.repositories.HomeRepository
import com.example.progettoesame.data.repositories.ProfileRepository
import com.example.progettoesame.data.repositories.RecipeRepository
import com.example.progettoesame.data.repositories.SyncRepository
import com.example.progettoesame.data.repositories.SplashRepository
import com.example.progettoesame.data.repositories.ThemeRepository
import com.example.progettoesame.data.repositories.UserRepository
import com.example.progettoesame.ui.viewmodels.AuthViewModel
import com.example.progettoesame.ui.viewmodels.CategoryViewModel
import com.example.progettoesame.ui.viewmodels.HomeViewModel
import com.example.progettoesame.ui.viewmodels.InitialErrorViewModel
import com.example.progettoesame.ui.viewmodels.NewRecipeViewModel
import com.example.progettoesame.ui.viewmodels.ProfileViewModel
import com.example.progettoesame.ui.viewmodels.RecipeViewModel
import com.example.progettoesame.ui.viewmodels.SettingViewModel
import com.example.progettoesame.ui.viewmodels.SplashViewModel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val Context.dataStore by preferencesDataStore("theme")
val appModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = "https://gerqtvdadryakiqvqita.supabase.co",
            supabaseKey = "sb_publishable_Dq9MKADaMDUdSSzfQZ-3-A_1eMwf6cH"
        ) {
            install(Auth) {
                scheme = "progettoesame"
                host = "login-callback"
            }
            install(Postgrest)
            install(Storage)
        }
    }


    single {
        Room.databaseBuilder(
            get(),
            ProjectDatabase::class.java,
            "recipes"
        ).fallbackToDestructiveMigration(dropAllTables = true)
         .build()
    }

    single { get<Context>().dataStore }
    single { AuthManager(get()) }
    single { SyncManager(get()) }

    single { ThemeRepository(get()) }

    single { SplashRepository(get<ProjectDatabase>().CategoryDAO(),
                                get<ProjectDatabase>().RecipeDAO(),
                                  get<ProjectDatabase>().UserDAO(),
                          get<ProjectDatabase>().UserFavouriteDAO(),
                          get<ProjectDatabase>().UserRatedDAO(),
                                           get(), get()) }

    single { SyncRepository(get<ProjectDatabase>().RecipeDAO(),
                              get<ProjectDatabase>().UserDAO(),
                       get<ProjectDatabase>().UserFavouriteDAO(),
                            get<ProjectDatabase>().UserRatedDAO(), get()) }
    single { CategoryRepository(get<ProjectDatabase>().CategoryDAO()) }
    single { RecipeRepository(get<ProjectDatabase>().RecipeDAO(),
                                get<ProjectDatabase>().UserDAO(),
                        get<ProjectDatabase>().UserFavouriteDAO(),
                        get<ProjectDatabase>().UserRatedDAO(),) }
    single { HomeRepository(get<ProjectDatabase>().CategoryDAO(), get<ProjectDatabase>().RecipeDAO()) }
    single { ProfileRepository( get<ProjectDatabase>().RecipeDAO(), get<ProjectDatabase>().UserFavouriteDAO()) }
    single { AuthRepository(get(), get<ProjectDatabase>().UserDAO()) }
    single { UserRepository(get<ProjectDatabase>().UserDAO(), get<ProjectDatabase>().RecipeDAO(), get<ProjectDatabase>().UserRatedDAO()) }

    viewModel { SettingViewModel(get(), get()) }
    viewModel { SplashViewModel(get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get()) }
    viewModel { CategoryViewModel(get(), get(), get()) }
    viewModel { InitialErrorViewModel(get()) }
    viewModel { RecipeViewModel(get(), get(), get()) }
    viewModel { NewRecipeViewModel(get(), get(),get(), get(), get()) }
    viewModel { AuthViewModel(get()) }

    worker { SyncWorker(get(), get(), get()) }
    worker { HardDeleteWorker(get(), get(), get()) }
}