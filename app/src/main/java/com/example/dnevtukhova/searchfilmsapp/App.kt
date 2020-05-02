package com.example.dnevtukhova.searchfilmsapp

import android.app.Application

class App: Application() {
    companion object {
        var items = mutableListOf(
            FilmsItem(
                1,
                R.string.name_film_shining,
                R.string.description_shining,
                R.drawable.shining,
                true
            ),
            FilmsItem(
                2,
                R.string.inception,
                R.string.inception_description,
                R.drawable.inception,
                true
            ),
            FilmsItem(
                3,
                R.string.maleficent,
                R.string.maleficent_description,
                R.drawable.maleficenta,
                true
            ),
            FilmsItem(
                4,
                R.string.name_film_cloud_atlas,
                R.string.description_cloud_atlas,
                R.drawable.cloud_atlas,
                true
            ),
            FilmsItem(
                5,
                R.string.ford,
                R.string.ford_description,
                R.drawable.ford_ferrari,
                true
            ),
            FilmsItem(
                6,
                R.string.joker,
                R.string.joker_description,
                R.drawable.joker,
                true
            ),
            FilmsItem(
                7,
                R.string.green_book,
                R.string.green_book_description,
                R.drawable.green_book,
                true
            )
        )
        var itemsFavorite = ArrayList<FilmsItem>()
    }
}