package com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dnevtukhova.searchfilmsapp.App
import com.example.dnevtukhova.searchfilmsapp.App.Companion.pageNumber
import com.example.dnevtukhova.searchfilmsapp.data.FilmsItem
import com.example.dnevtukhova.searchfilmsapp.domain.FilmsInteractor

class FilmsListViewModel(private val filmsInteractor: FilmsInteractor) : ViewModel() {
    private val filmsLiveData = MutableLiveData<List<FilmsItem>>()
    private val favoriteLiveData = MutableLiveData<List<FilmsItem>>()
    private val filmsDetailLiveData = MutableLiveData<FilmsItem>()
    private val errorLiveData = SingleLiveEvent<String>()

    val films: LiveData<List<FilmsItem>>
        get() = filmsLiveData

    val favoriteFilms: LiveData<List<FilmsItem>>
        get() = favoriteLiveData

    val filmsDetail: LiveData<FilmsItem>
        get() = filmsDetailLiveData

    val error: LiveData<String>
        get() = errorLiveData

    fun getAllFilms() {
        filmsInteractor.getFilms(
            App.API_KEY,
            App.LANGUAGE,
            pageNumber,
            object : FilmsInteractor.GetFilmsCallback {
                override fun onSuccess(films: List<FilmsItem>) {
                    filmsLiveData.postValue(films)
                }

                override fun onError(error: String) {
                    errorLiveData.postValue(error)
                }
            })
    }

    fun selectFilm(filmsItem: FilmsItem) {
        filmsDetailLiveData.postValue(filmsItem)
    }

    fun selectFavorite(filmsItem: FilmsItem, position: Int) {
        filmsInteractor.selectFavorite(filmsItem, position)
    }

    fun getFavorite() {
        filmsInteractor.getFavorite(object : FilmsInteractor.GetFavoriteCallback {
            override fun getFavorite(films: List<FilmsItem>) {
                favoriteLiveData.postValue(films)
            }
        })
    }

    fun getFilmPagination() {
        pageNumber++
        filmsInteractor.getFilms(
            App.API_KEY,
            App.LANGUAGE,
            pageNumber,
            object : FilmsInteractor.GetFilmsCallback {
                override fun onSuccess(films: List<FilmsItem>) {
                    filmsLiveData.postValue(films)
                }

                override fun onError(error: String) {
                    errorLiveData.postValue(error)
                }
            })
    }

    fun removeFromFavorite(filmsItem: FilmsItem, favorite: Boolean) {
        filmsInteractor.removeFromFavorite(filmsItem, favorite)
    }

    fun addToFavorite(filmsItem: FilmsItem, favorite: Boolean) {
        filmsInteractor.addToFavorite(filmsItem, favorite)
    }
}