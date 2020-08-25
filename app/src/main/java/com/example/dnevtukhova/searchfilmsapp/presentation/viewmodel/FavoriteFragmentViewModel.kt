package com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel
import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmsItem
import com.example.dnevtukhova.searchfilmsapp.domain.FilmsInteractor
import io.reactivex.schedulers.Schedulers

class FavoriteFragmentViewModel(private val filmsInteractor: FilmsInteractor) : ViewModel() {
    private val favoriteLiveData: LiveData<List<FilmsItem>>? =
        LiveDataReactiveStreams.fromPublisher(filmsInteractor.getFavorite()!!.subscribeOn(Schedulers.io()))

    val favoriteFilms: LiveData<List<FilmsItem>>?
        get() = favoriteLiveData

    fun changeFavorite(filmsItem: FilmsItem, favorite: Boolean) {
        filmsInteractor.changeFavorite(filmsItem, favorite)
    }
}