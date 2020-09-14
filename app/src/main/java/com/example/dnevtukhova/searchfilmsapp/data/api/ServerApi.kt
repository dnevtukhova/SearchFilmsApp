package com.example.dnevtukhova.searchfilmsapp.data.api

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface ServerApi {
    @GET ("movie/popular")
    fun getFilms(
        @Query("api_key") key: String,
        @Query("language") language: String,
        @Query("page") page: Int
    ): Single <PopularFilms>

    @GET("search/movie")
    fun searchFilms(
        @Query("api_key") key: String,
        @Query("language") language: String,
        @Query("page") page: Int,
        @Query("query") query: String
    ): Single<PopularFilms>
}