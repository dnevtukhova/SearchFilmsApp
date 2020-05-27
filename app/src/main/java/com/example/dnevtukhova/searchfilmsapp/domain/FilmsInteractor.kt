package com.example.dnevtukhova.searchfilmsapp.domain

import com.example.dnevtukhova.searchfilmsapp.data.FilmsItem
import com.example.dnevtukhova.searchfilmsapp.data.FilmsRepository
import com.example.dnevtukhova.searchfilmsapp.data.PopularFilms
import com.example.dnevtukhova.searchfilmsapp.data.ServerApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FilmsInteractor(
    private val serverApi: ServerApi,
    private val filmsRepository: FilmsRepository
) {
    fun getFilms(key: String, language: String, page: Int, callback: GetFilmsCallback) {
        serverApi.getFilms(key, language, page).enqueue(object : Callback<PopularFilms> {
            override fun onResponse(call: Call<PopularFilms>, response: Response<PopularFilms>) {
                if (response.isSuccessful) {
                    val filmsList = mutableListOf<FilmsItem>()
                    response.body()?.results
                        ?.forEach {
                            filmsList.add(
                                FilmsItem(
                                    it.id,
                                    it.title,
                                    it.description,
                                    it.image,
                                    isFavorite(it.id)
                                )
                            )
                        }
                    filmsRepository.addToCache(filmsList)
                    callback.onSuccess(filmsRepository.getFilms())
                } else {
                    callback.onError("!!! произошла ошибка ${response.code()}")
                }
            }

            override fun onFailure(call: Call<PopularFilms>, t: Throwable) {
                callback.onError("!!! произошла ошибка $t")
            }
        })
    }

    fun getFavorite(callback: GetFavoriteCallback) {
        callback.getFavorite(filmsRepository.getFavoriteFilms())
    }

    interface GetFilmsCallback {
        fun onSuccess(films: List<FilmsItem>)
        fun onError(error: String)
    }

    interface GetFavoriteCallback {
        fun getFavorite(films: List<FilmsItem>)
    }

    private fun isFavorite(id: Int): Boolean {
        var isLike = true
        for (item in filmsRepository.getFavoriteFilms()) {
            if (id == item.id) {
                isLike = false
                break
            }
        }
        return isLike
    }

    fun selectFavorite(filmsItem: FilmsItem, position: Int) {
        val f: FilmsItem = filmsRepository.getFilms()[position]
        if (filmsItem.favorite) {

            f.favorite = false
            filmsRepository.setFilms(f, position)
            filmsRepository.addToFavorite(f)

        } else {
            f.favorite = true
            filmsRepository.setFilms(f, position)
            filmsRepository.removeFromFavorite(f)
        }
    }

    fun removeFromFavorite(filmsItem: FilmsItem, favorite: Boolean) {
        filmsRepository.removeFromFavorite(filmsItem)
        filmsRepository.setFilms(filmsItem, favorite)
    }

    fun addToFavorite(filmsItem: FilmsItem, favorite: Boolean) {
        filmsRepository.addToFavorite(filmsItem)
        filmsRepository.setFilms(filmsItem, favorite)
    }
}