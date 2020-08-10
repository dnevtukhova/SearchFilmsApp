package com.example.dnevtukhova.searchfilmsapp.domain

import android.annotation.SuppressLint
import com.example.dnevtukhova.searchfilmsapp.data.*
import io.reactivex.Flowable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class FilmsInteractor(
    private val serverApi: ServerApi,
    private var filmsRepository: FilmsRepository

) {
    fun getFilms(key: String, language: String, page: Int, callback: GetFilmsCallback) {
        serverApi.getFilms(key, language, page)
            .subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.newThread())
            .subscribe(object : DisposableSingleObserver<PopularFilms>() {
                override fun onSuccess(t: PopularFilms) {
                    println("вошли в метод onSucsess")
                    val filmsList = mutableListOf<FilmsItem>()
                    t.results
                        .forEach {
                            filmsList.add(
                                FilmsItem(
                                    it.id,
                                    it.title,
                                    it.description,
                                    it.image,
                                    isFavorite(it.id),
                                    isWatchLater(it.id)
                                )
                            )
                        }

                    filmsRepository.addToCache(filmsList)
                    callback.onSuccess(filmsRepository.films)
                }

                override fun onError(e: Throwable) {
                    callback.onError("!!! произошла ошибка $e")
                }
            })
    }

    fun getFilms(): Flowable<List<FilmsItem>>? {
        return filmsRepository.films
    }

    fun getFavorite(): Flowable<List<FavoriteItem>>? {
        return filmsRepository.favoriteFilms
    }

    interface GetFilmsCallback {
        fun onSuccess(films: Flowable<List<FilmsItem>>?)
        fun onError(error: String)
    }

    @SuppressLint("CheckResult")
    private fun isFavorite(id: Int): Boolean {
        var isLike = true
        if (filmsRepository.getItemFavorite(id) != null) {
            isLike = false
        }
        return isLike
    }

    fun selectFavorite(filmsItem: FilmsItem) {
        val f = FavoriteItem(
            filmsItem.id,
            filmsItem.title,
            filmsItem.description,
            filmsItem.image,
            filmsItem.favorite,
            filmsItem.watchLater
        )
        if (f.favorite) {
            f.favorite = false
            filmsItem.favorite = false
            filmsRepository.setFilms(filmsItem)
            filmsRepository.addToFavorite(f)
        } else {
            filmsRepository.removeFromFavorite(f)
            filmsItem.favorite = true
            filmsRepository.setFilms(filmsItem)
        }
    }

    fun removeFromFavorite(favoriteItem: FavoriteItem, favorite: Boolean) {
        filmsRepository.removeFromFavorite(favoriteItem)
        filmsRepository.setFavorite(favoriteItem, favorite)
    }

    fun addToFavorite(favoriteItem: FavoriteItem, favorite: Boolean) {
        filmsRepository.addToFavorite(favoriteItem, favorite)
        filmsRepository.setFavorite(favoriteItem, favorite)
    }

    fun removeAllFilms() {
        filmsRepository.removeAllFilms()
    }

    //watchLater

    fun getWatchLater(): Flowable<List<WatchLaterItem>>? {
        return filmsRepository.watchLaterFilms
    }

    fun selectWatchLater(filmsItem: FilmsItem, dateToWatch: Long) {
        val w = WatchLaterItem(
            filmsItem.id,
            filmsItem.title,
            filmsItem.description,
            filmsItem.image,
            filmsItem.favorite,
            filmsItem.watchLater,
            dateToWatch
        )
        if (w.watchLater) {
            w.watchLater = false
            filmsItem.watchLater = false
            filmsRepository.setFilms(filmsItem)
            filmsRepository.addToWatchLater(w)
        } else {
            filmsRepository.removeFromWatchLater(
                filmsRepository.getItemWatchLater(
                    filmsItem.id
                )!!
            )
            filmsItem.watchLater = true
            filmsRepository.setFilms(filmsItem)
        }
    }

    private fun isWatchLater(id: Int): Boolean {
        var isWatchLater = true
        if (filmsRepository.getItemWatchLater(id) != null) {
            isWatchLater = false
        }
        return isWatchLater
    }

    fun setDateToWatch(watchLaterItem: WatchLaterItem) {
        filmsRepository.setDateToWatch(watchLaterItem)
    }
}