package com.example.progettoesame

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.progettoesame.data.database.ProjectDatabase
import com.example.progettoesame.data.repositories.CategoryRepository
import com.example.progettoesame.ui.viewmodels.CategoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val Context.dataStore by
preferencesDataStore("theme")
val appModule = module {
    single {
        Room.databaseBuilder(
            get(),
            ProjectDatabase::class.java,
            "recipes"
        ).build()
    }

    single { get<Context>().dataStore }
    single { CategoryRepository(get() /* o get<ProjectDatabase>().RecipeDAO() se serve Room e non il dataStore*/) }
    viewModel { CategoryViewModel(get()) }

    //Stessa cosa per gli altri repository e viewModel
}