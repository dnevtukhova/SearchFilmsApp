package com.example.dnevtukhova.searchfilmsapp.domain

import com.example.dnevtukhova.searchfilmsapp.data.FilmsRepository
import com.example.dnevtukhova.searchfilmsapp.data.api.PopularFilms
import com.example.dnevtukhova.searchfilmsapp.data.api.ServerApi
import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmsItem
import io.reactivex.Flowable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FilmsInteractor @Inject constructor(
    val serverApi: ServerApi,
    val filmsRepository: FilmsRepository

) {
    fun getFilms(key: String, language: String, page: Int, callback: GetFilmsCallback) {
        serverApi.getFilms(key, language, page)
            .subscribeOn(Schedulers.io())
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
                                    true,
                                    true,
                                    null
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

    fun getFavorite(): Flowable<List<FilmsItem>>? {
        return filmsRepository.favoriteFilms
    }

    interface GetFilmsCallback {
        fun onSuccess(films: Flowable<List<FilmsItem>>?)
        fun onError(error: String)
    }

    fun selectFavorite(filmsItem: FilmsItem) {
        if (filmsItem.favorite) {
            filmsItem.favorite = false
            filmsRepository.setFilms(filmsItem)
        } else {
            filmsItem.favorite = true
            filmsRepository.setFilms(filmsItem)
        }
    }

    fun changeFavorite(favoriteItem: FilmsItem, favorite: Boolean) {
        filmsRepository.setFavorite(favoriteItem, favorite)
    }

    fun getWatchLater(): Flowable<List<FilmsItem>>? {
        return filmsRepository.watchLaterFilms
    }

    fun changeWatchLater(filmsItem: FilmsItem) {
        filmsRepository.setFilms(filmsItem)
    }

    fun setDateToWatch(watchLaterItem: FilmsItem) {
        filmsRepository.setDateToWatch(watchLaterItem)
    }
}