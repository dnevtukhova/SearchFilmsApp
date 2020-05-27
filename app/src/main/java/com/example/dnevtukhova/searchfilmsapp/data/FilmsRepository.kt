package com.example.dnevtukhova.searchfilmsapp.data

class FilmsRepository {
    private var itemsFavorite = mutableListOf<FilmsItem>()
    private var itemsFilms = mutableListOf<FilmsItem>()

    fun getFilms(): List<FilmsItem> {
        return itemsFilms
    }

    fun getFavoriteFilms(): List<FilmsItem> {
        return itemsFavorite
    }

    fun addToCache(films: List<FilmsItem>) {
        this.itemsFilms.addAll(films)
    }

    fun addToFavorite(itemFilm: FilmsItem) {
        itemsFavorite.add(itemFilm)
    }


    fun removeFromFavorite(itemFilm: FilmsItem) {
        itemsFavorite.remove(itemFilm)
    }

    fun setFilms(itemFilm: FilmsItem, position: Int) {
        itemsFilms[position] = itemFilm
    }

    fun setFilms(itemFilm: FilmsItem, favorite: Boolean) {
        for (i in itemsFilms.indices) {
            if (itemsFilms[i].id == itemFilm.id) {
                itemsFilms[i].favorite = favorite
            }
        }
    }
}