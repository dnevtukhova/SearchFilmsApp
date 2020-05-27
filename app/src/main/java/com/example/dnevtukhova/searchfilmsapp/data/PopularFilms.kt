package com.example.dnevtukhova.searchfilmsapp.data

import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmModel

data class PopularFilms(
    val page: Int,
    val results: Array<FilmModel>
)