package com.example.dnevtukhova.searchfilmsapp.domain

import androidx.lifecycle.LiveData
import com.example.dnevtukhova.searchfilmsapp.data.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FilmsInteractor(
    private val serverApi: ServerApi,
    private var filmsRepository: FilmsRepository

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

                    callback.onSuccess(filmsRepository.films)
                } else {
                    callback.onError("!!! произошла ошибка ${response.code()}")
                }
            }

            override fun onFailure(call: Call<PopularFilms>, t: Throwable) {
                callback.onError("!!! произошла ошибка $t")
            }
        })
    }

    fun getFilms(): LiveData<List<FilmsItem>>? {
        return filmsRepository.films
    }

    fun getFavorite(): LiveData<List<FavoriteItem>>? {
        return filmsRepository.favoriteFilms
    }

    interface GetFilmsCallback {
        fun onSuccess(films: LiveData<List<FilmsItem>>?)
        fun onError(error: String)
    }

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
            filmsItem.favorite
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
        filmsRepository.setFilms(favoriteItem, favorite)
    }

    fun addToFavorite(favoriteItem: FavoriteItem, favorite: Boolean) {
        filmsRepository.addToFavorite(favoriteItem, favorite)
        filmsRepository.setFilms(favoriteItem, favorite)
    }

    fun removeAllFilms() {
        filmsRepository.removeAllFilms()
    }

}