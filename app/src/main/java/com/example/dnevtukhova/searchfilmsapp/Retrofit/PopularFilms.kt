package com.example.dnevtukhova.searchfilmsapp.Retrofit

data class PopularFilms(
    val page: Int,
    val results: Array<FilmModel>
)