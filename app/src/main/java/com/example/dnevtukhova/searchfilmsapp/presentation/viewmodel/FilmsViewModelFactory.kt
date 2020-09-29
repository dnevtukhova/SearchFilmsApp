package com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dnevtukhova.searchfilmsapp.domain.FilmsInteractor
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class FilmsViewModelFactory @Inject constructor
    (
    private val viewModels: MutableMap<Class<out ViewModel>, Provider<ViewModel>>,
    private val filmsInteractor: FilmsInteractor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(FilmsInteractor::class.java)
            .newInstance(filmsInteractor)
    }
}