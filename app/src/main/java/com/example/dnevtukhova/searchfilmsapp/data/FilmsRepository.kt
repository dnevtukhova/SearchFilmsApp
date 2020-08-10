package com.example.dnevtukhova.searchfilmsapp.data

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers

class FilmsRepository(filmsDb: FilmsDb?) {
    private var filmsDao = filmsDb?.getFilmsDao()
    private var filmsData: Flowable<List<FilmsItem>>? = filmsDao?.getFilms()
    private var favoriteData: Flowable<List<FavoriteItem>>? = filmsDao?.getAllFavorite()
    private var watchLaterData: Flowable<List<WatchLaterItem>>? = filmsDao?.getAllWatchLater()

    val films: Flowable<List<FilmsItem>>?
        get() = filmsData
    val favoriteFilms: Flowable<List<FavoriteItem>>?
        get() = favoriteData
    val watchLaterFilms: Flowable<List<WatchLaterItem>>?
        get() = watchLaterData

    fun addToCache(films: List<FilmsItem>) {
        Completable.fromRunnable {
            filmsDao?.insertAll(films)
        }
            .subscribeOn(Schedulers.computation())
            .subscribe()
    }

    fun getItemFavorite(id: Int): FavoriteItem? = filmsDao?.getItemFavorite(id)

    fun addToFavorite(favoriteItem: FavoriteItem, isFavorite: Boolean) {
        Completable.fromRunnable {
            filmsDao?.insertInFavorite(favoriteItem)
            filmsDao?.updateIsFavorite(favoriteItem.id, isFavorite)
        }
            .subscribeOn(Schedulers.computation())
            .subscribe()
    }

    fun addToFavorite(itemFilm: FavoriteItem) {
        Completable.fromRunnable {
            filmsDao?.insertInFavorite(itemFilm)
        }
            .subscribeOn(Schedulers.computation())
            .subscribe()
    }

    fun removeFromFavorite(itemFilm: FavoriteItem) {
        Completable.fromRunnable {
            filmsDao?.deleteItemFavorite(itemFilm)
        }
            .subscribeOn(Schedulers.computation())
            .subscribe()
    }

    fun setFilms(itemFilm: FilmsItem) {
        Completable.fromRunnable {
            filmsDao?.updateFilms(itemFilm)
        }
            .subscribeOn(Schedulers.computation())
            .subscribe()
    }

    fun setFavorite(itemFilm: FavoriteItem, favorite: Boolean) {
        Completable.fromRunnable {
            filmsDao?.setFilms(itemFilm.id, favorite)
        }
            .subscribeOn(Schedulers.computation())
            .subscribe()
    }

    fun removeAllFilms() {
        Completable.fromRunnable {
            filmsDao?.removeAllFilms()
        }
            .subscribeOn(Schedulers.computation())
            .subscribe()
    }

    //watchLater

    fun addToWatchLater(itemFilm: WatchLaterItem) {
        Completable.fromRunnable {
            filmsDao?.insertInWatchLater(itemFilm)
        }
            .subscribeOn(Schedulers.computation())
            .subscribe()
    }

    fun removeFromWatchLater(itemFilm: WatchLaterItem) {
        Completable.fromRunnable {
            filmsDao?.deleteItemWatchLater(itemFilm)
        }
            .subscribeOn(Schedulers.computation())
            .subscribe()
    }

    fun getItemWatchLater(id: Int): WatchLaterItem? {
        return filmsDao?.getItemWatchLater(id)
    }

    fun setDateToWatch(itemFilm: WatchLaterItem) {
        Completable.fromRunnable {
            filmsDao?.updateTimeToWatch(itemFilm.id, itemFilm.dateToWatch)
        }
            .subscribeOn(Schedulers.computation())
            .subscribe()
    }
}

