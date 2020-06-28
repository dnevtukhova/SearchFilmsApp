package com.example.dnevtukhova.searchfilmsapp.data

import androidx.lifecycle.LiveData
import java.util.concurrent.Executors

class FilmsRepository(filmsDb: FilmsDb?) {
    private var filmsDao = filmsDb?.getFilmsDao()
    private var filmsLiveData: LiveData<List<FilmsItem>>? = filmsDao?.getFilms()
    private var favoriteLiveData: LiveData<List<FavoriteItem>>? = filmsDao?.getAllFavorite()
    private var watchLaterLiveData: LiveData<List<WatchLaterItem>>? = filmsDao?.getAllWatchLater()

    val films: LiveData<List<FilmsItem>>?
        get() = filmsLiveData
    val favoriteFilms: LiveData<List<FavoriteItem>>?
        get() = favoriteLiveData
    val watchLaterFilms: LiveData<List<WatchLaterItem>>?
        get() = watchLaterLiveData

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

    fun setFavorite(itemFilm: FavoriteItem, favorite: Boolean) {
        Executors.newSingleThreadExecutor().execute {
            filmsDao?.setFilms(itemFilm.id, favorite)
        }
    }

    fun removeAllFilms() {
        filmsDao?.removeAllFilms()
    }

    //watchLater

    fun addToWatchLater(itemFilm: WatchLaterItem) {
        Executors.newSingleThreadExecutor().execute {
            filmsDao?.insertInWatchLater(itemFilm)
        }
    }

    fun removeFromWatchLater(itemFilm: WatchLaterItem) {
        Executors.newSingleThreadExecutor().execute {
            filmsDao?.deleteItemWatchLater(itemFilm)
        }
    }

    fun getItemWatchLater(id: Int): WatchLaterItem? {
        return filmsDao?.getItemWatchLater(id)
    }

    fun setDateToWatch(itemFilm: WatchLaterItem) {
        Executors.newSingleThreadExecutor().execute() {
            filmsDao?.updateTimeToWatch(itemFilm.id, itemFilm.dateToWatch)
        }
    }
}

