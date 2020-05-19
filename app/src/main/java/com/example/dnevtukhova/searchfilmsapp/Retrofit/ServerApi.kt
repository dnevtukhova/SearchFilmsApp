package com.example.dnevtukhova.searchfilmsapp.Retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ServerApi {

    @GET ("popular")
    fun getFilms(
        @Query("api_key") key: String,
        @Query("language") language:String,
        @Query("page") page:Int
    ): Call<PopularFilms>


}