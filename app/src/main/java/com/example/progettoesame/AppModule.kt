package com.example.progettoesame

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.progettoesame.data.repositories.CategoryRepository
import com.example.progettoesame.ui.viewmodels.CategoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val Context.dataStore by
preferencesDataStore("theme")
val appModule = module {
    single { get<Context>().dataStore }
    single { CategoryRepository(get()) }
    viewModel { CategoryViewModel(get()) }

    //Stessa cosa per gli altri repository e viewModel
}