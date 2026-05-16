package com.example.progettoesame

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.progettoesame.data.SyncManager
import com.example.progettoesame.data.SyncWorker
import com.example.progettoesame.data.database.ProjectDatabase
import com.example.progettoesame.data.repositories.CategoryRepository
import com.example.progettoesame.data.repositories.HomeRepository
import com.example.progettoesame.data.repositories.RecipeRepository
import com.example.progettoesame.data.repositories.SyncRepository
import com.example.progettoesame.data.repositories.SplashRepository
import com.example.progettoesame.ui.viewmodels.CategoryViewModel
import com.example.progettoesame.ui.viewmodels.HomeViewModel
import com.example.progettoesame.ui.viewmodels.InitialErrorViewModel
import com.example.progettoesame.ui.viewmodels.NewRecipeViewModel
import com.example.progettoesame.ui.viewmodels.RecipeViewModel
import com.example.progettoesame.ui.viewmodels.SplashViewModel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val Context.dataStore by
preferencesDataStore("theme")
val appModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = "https://gerqtvdadryakiqvqita.supabase.co",
            supabaseKey = "sb_publishable_Dq9MKADaMDUdSSzfQZ-3-A_1eMwf6cH"
        ) {
            install(Auth)
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
    single { SyncManager(get()) }

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
    single { HomeRepository(get<ProjectDatabase>().CategoryDAO()) }

    viewModel { SplashViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { CategoryViewModel(get(), get(), get()) }
    viewModel { InitialErrorViewModel(get()) }
    viewModel { RecipeViewModel(get()) }
    viewModel { NewRecipeViewModel(get(), get()) }

    worker { SyncWorker(get(), get(), get()) }
}