package com.example.dnevtukhova.searchfilmsapp.data.api

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface ServerApi {
    @GET ("popular")
    fun getFilms(
        @Query("api_key") key: String,
        @Query("language") language: String,
        @Query("page") page: Int
    ): Single <PopularFilms>
}