package com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dnevtukhova.searchfilmsapp.domain.FilmsInteractor

class FilmsViewModelFactory(private val filmsInteractor: FilmsInteractor) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(FilmsInteractor::class.java)
            .newInstance(filmsInteractor)
    }
}