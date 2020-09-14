package com.example.dnevtukhova.searchfilmsapp.data.api

data class PopularFilms(
    val page: Int,
    val results: MutableList<FilmModel>
)