package com.example.dnevtukhova.searchfilmsapp.data

import com.example.dnevtukhova.searchfilmsapp.data.db.FilmsDao
import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmsItem
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers

class FilmsRepository(filmsDao: FilmsDao) {
    private var filmsDao = filmsDao
    private var filmsData: Flowable<List<FilmsItem>>? = filmsDao.getFilms()
    private var favoriteData: Flowable<List<FilmsItem>>? = filmsDao.getAllFavorite()
    private var watchLaterData: Flowable<List<FilmsItem>>? = filmsDao.getAllWatchLater()

    val films: Flowable<List<FilmsItem>>?
        get() = filmsData
    val favoriteFilms: Flowable<List<FilmsItem>>?
        get() = favoriteData
    val watchLaterFilms: Flowable<List<FilmsItem>>?
        get() = watchLaterData

    fun addToCache(films: List<FilmsItem>) {
        Completable.fromRunnable {
            filmsDao.insertAll(films)
        }
            .subscribeOn(Schedulers.computation())
            .subscribe()
    }

    fun getFilm(id: Int): FilmsItem {
        return filmsDao.getFilm(id)
    }

    fun addFilm(filmsItem: FilmsItem) {
        Completable.fromRunnable {
            filmsDao.insert(filmsItem)
        }
            .subscribeOn(Schedulers.computation())
            .subscribe()
    }

    fun setFilms(itemFilm: FilmsItem) {
        Completable.fromRunnable {
            filmsDao.updateFilms(itemFilm)
        }
            .subscribeOn(Schedulers.computation())
            .subscribe()
    }

    fun setFavorite(itemFilm: FilmsItem, favorite: Boolean) {
        Completable.fromRunnable {
            filmsDao.setFilms(itemFilm.id, favorite)
        }
            .subscribeOn(Schedulers.computation())
            .subscribe()
    }

    fun setDateToWatch(itemFilm: FilmsItem) {
        Completable.fromRunnable {
            filmsDao.updateTimeToWatch(itemFilm.id, itemFilm.dateToWatch!!)
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
}

