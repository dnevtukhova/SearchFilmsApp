package com.example.dnevtukhova.searchfilmsapp.domain

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.example.dnevtukhova.searchfilmsapp.App
import com.example.dnevtukhova.searchfilmsapp.data.FilmsRepository
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants.FAVORITE
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants.WATCHLATER
import com.example.dnevtukhova.searchfilmsapp.data.api.PopularFilms
import com.example.dnevtukhova.searchfilmsapp.data.api.ServerApi
import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmsItem
import io.reactivex.Flowable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import testing.OpenForTesting
import java.util.*
import javax.inject.Inject

@OpenForTesting
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
                    val filmsList = mutableListOf<FilmsItem>()
                    t.results
                        .forEach {
                            filmsList.add(
                                FilmsItem(
                                    it.id,
                                    it.title,
                                    it.description,
                                    it.image,
                                    isFavorite(it.id.toString()),
                                    isWatchLater(it.id.toString()),
                                    getDateToWatchValue(it.id.toString()),
                                    it.average
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

    fun getSearchFilms(
        key: String,
        language: String,
        page: Int,
        query: String,
        callback: GetFilmsCallback
    ) {
        val disposable = serverApi.searchFilms(key, language, page, query)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.newThread())
            .subscribeBy(
                onError = {
                    callback.onError("!!! произошла ошибка $it")
                },
                onSuccess = { it ->
                    val filmsList = mutableListOf<FilmsItem>()
                    it.results.forEach {
                        filmsList.add(
                            FilmsItem(
                                it.id,
                                it.title,
                                it.description,
                                it.image,
                                isFavorite(it.id.toString()),
                                isWatchLater(it.id.toString()),
                                null,
                                it.average
                            )
                        )
                    }
                    callback.onSuccess(filmsList)
                }
            )
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
        fun onSuccess(films: MutableList<FilmsItem>)
    }

    fun selectFavorite(filmsItem: FilmsItem) {
        filmsItem.favorite = !filmsItem.favorite
        filmsRepository.setFilms(filmsItem)
    }

    fun changeFavorite(favoriteItem: FilmsItem, favorite: Boolean) {
        filmsRepository.setFavorite(favoriteItem, favorite)
        val mSettings = App.instance.applicationContext.getSharedPreferences(
            "Settings",
            Context.MODE_PRIVATE
        )
        val set = mSettings.getStringSet(FAVORITE, HashSet())

        if (favorite) {
            set?.remove(favoriteItem.id.toString())
        } else {
            set?.add(favoriteItem.id.toString())
        }
        mSettings.edit { putStringSet(FAVORITE, set) }
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

    fun removeAllFilms() {
        filmsRepository.removeAllFilms()
    }

    fun isFavorite(id: String): Boolean {
        val mSettings = App.instance.applicationContext.getSharedPreferences(
            "Settings",
            Context.MODE_PRIVATE
        )
        val set = mSettings.getStringSet(FAVORITE, HashSet())
        var isTrue = true
        for (r in set!!) {
            Log.d("FAVORITE IN SET", r)
            if (id == r) {
                isTrue = false
            }
        }
        return isTrue
    }

    fun isWatchLater(id: String): Boolean {
        val mSettings = App.instance.applicationContext.getSharedPreferences(
            "Settings",
            Context.MODE_PRIVATE
        )
        var isTrue = true
        val set = mSettings.getStringSet(WATCHLATER, HashSet())
        for (r in set!!) {
            if (id == r) {
                isTrue = false
            }
        }
        return isTrue
    }

    fun getDateToWatchValue(id: String): Long {
        val mSettings = App.instance.applicationContext.getSharedPreferences(
            "Settings",
            Context.MODE_PRIVATE
        )
        val value = mSettings.getLong(id, 0)
        Log.d("VALUE_DATE_TO_WATCH", value.toString())
        return value
    }

    fun addFilm(filmsItem: FilmsItem) {
        filmsRepository.addFilm(filmsItem)
    }
}