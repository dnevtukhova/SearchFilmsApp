package com.example.dnevtukhova.searchfilmsapp.data

import androidx.lifecycle.LiveData
import java.util.concurrent.Executors

class FilmsRepository(filmsDb: FilmsDb?) {
    private var filmsDao = filmsDb?.getFilmsDao()
    private var filmsLiveData: LiveData<List<FilmsItem>>? = filmsDao?.getFilms()
    private var favoriteLiveData: LiveData<List<FavoriteItem>>? = filmsDao?.getAllFavorite()

    val films: LiveData<List<FilmsItem>>?
        get() = filmsLiveData
    val favoriteFilms: LiveData<List<FavoriteItem>>?
        get() = favoriteLiveData

    fun addToCache(films: List<FilmsItem>) {
        Executors.newSingleThreadExecutor().execute {
            filmsDao?.insertAll(films)
        }
    }

    fun getItemFavorite(id: Int): FavoriteItem? {
        return filmsDao?.getItemFavorite(id)
    }

    fun addToFavorite(favoriteItem: FavoriteItem, isFavorite: Boolean) {
        Executors.newSingleThreadExecutor().execute {
            filmsDao?.insertInFavorite(favoriteItem)
            filmsDao?.updateIsFavorite(favoriteItem.id, isFavorite)
        }
    }

    fun addToFavorite(itemFilm: FavoriteItem) {
        Executors.newSingleThreadExecutor().execute {
            filmsDao?.insertInFavorite(itemFilm)
        }
    }


    fun removeFromFavorite(itemFilm: FavoriteItem) {
        Executors.newSingleThreadExecutor().execute {
            filmsDao?.deleteItemFavorite(itemFilm)
        }
    }

    fun setFilms(itemFilm: FilmsItem) {
        Executors.newSingleThreadExecutor().execute {
            filmsDao?.updateFilms(itemFilm)
        }
    }

    fun setFilms(itemFilm: FavoriteItem, favorite: Boolean) {
        Executors.newSingleThreadExecutor().execute {
            filmsDao?.setFilms(itemFilm.id, favorite)
        }
    }

    fun removeAllFilms() {
        filmsDao?.removeAllFilms()
    }
}

