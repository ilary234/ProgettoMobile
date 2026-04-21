package com.example.progettoesame

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.progettoesame.data.SyncManager
import com.example.progettoesame.data.SyncWorker
import com.example.progettoesame.data.database.ProjectDatabase
import com.example.progettoesame.data.repositories.CategoryRepository
import com.example.progettoesame.ui.viewmodels.CategoryViewModel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val Context.dataStore by
preferencesDataStore("theme")
val appModule = module {
    val supabase = createSupabaseClient(
        supabaseUrl = "https://gerqtvdadryakiqvqita.supabase.co",
        supabaseKey = "sb_publishable_Dq9MKADaMDUdSSzfQZ-3-A_1eMwf6cH"
    ) {
        install(Auth)
        install(Postgrest)
    }

    single {
        Room.databaseBuilder(
            get(),
            ProjectDatabase::class.java,
            "recipes"
        ).build()
    }

    single { get<Context>().dataStore }

    single { CategoryRepository(get<ProjectDatabase>().RecipeDAO(), get()) }
    viewModel { CategoryViewModel(get()) }

    //Stessa cosa per gli altri repository e viewModel
    single { SyncManager(get()) }
    worker { SyncWorker(get(), get(), get()) }
}