package com.example.dnevtukhova.searchfilmsapp

import android.app.Application
import com.example.dnevtukhova.searchfilmsapp.data.FilmsRepository
import com.example.dnevtukhova.searchfilmsapp.data.ServerApi
import com.example.dnevtukhova.searchfilmsapp.domain.FilmsInteractor
import com.squareup.picasso.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class App : Application() {
    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/movie/"
        lateinit var instance: App
            private set

        const val API_KEY = "79c459d10744203ee914c139f789d1e8"
        const val LANGUAGE = "ru-RUS"
        var pageNumber: Int = 1

        var favoriteF: Boolean = false
        var listF: Boolean = true
    }

    lateinit var api: ServerApi
    lateinit var filmsInteractor: FilmsInteractor
    private var filmsRepository = FilmsRepository()

    override fun onCreate() {
        super.onCreate()
        instance = this
        initRetrofit()
        initInteractor()
    }

    private fun initRetrofit() {
        val client = OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor()
                    .apply {
                        if (BuildConfig.DEBUG) {
                            level = HttpLoggingInterceptor.Level.BODY
                        }
                    })
            .build()
        api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ServerApi::class.java)
    }

    private fun initInteractor() {
        filmsInteractor = FilmsInteractor(api, filmsRepository)
    }
}