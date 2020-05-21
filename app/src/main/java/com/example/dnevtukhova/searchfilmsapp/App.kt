package com.example.dnevtukhova.searchfilmsapp

import android.app.Application
import com.example.dnevtukhova.searchfilmsapp.Retrofit.ServerApi
import com.squareup.picasso.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class App : Application() {
    companion object {
        var itemsFilms = mutableListOf<FilmsItem>()
        const val BASE_URL = "https://api.themoviedb.org/3/movie/"
        lateinit var instance: App
            private set
        var itemsFavorite = ArrayList<FilmsItem>()
    }

    lateinit var api: ServerApi

    override fun onCreate() {
        super.onCreate()
        instance = this
        initRetrofit()
    }

    private fun initRetrofit() {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                return@addInterceptor chain.proceed(
                    chain
                        .request()
                        .newBuilder()
                        .build()
                )
            }
            .addInterceptor(
                HttpLoggingInterceptor()
                    .apply {
                        if (BuildConfig.DEBUG) {
                            level = HttpLoggingInterceptor.Level.BASIC
                        }
                    })
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        api = retrofit.create(ServerApi::class.java)
    }
}