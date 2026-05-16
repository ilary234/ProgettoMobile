package com.example.progettoesame.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progettoesame.data.database.Category
import com.example.progettoesame.data.repositories.CategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class Categories(val categories: List<Category>)

class HomeViewModel(private val categoryRepository: CategoryRepository) : ViewModel() {
     val categories = categoryRepository.categories.map { Categories(it) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = Categories(emptyList()))

}