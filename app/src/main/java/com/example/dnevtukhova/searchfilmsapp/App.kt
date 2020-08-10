package com.example.dnevtukhova.searchfilmsapp

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.dnevtukhova.searchfilmsapp.data.FilmsDb
import com.example.dnevtukhova.searchfilmsapp.data.FilmsRepository
import com.example.dnevtukhova.searchfilmsapp.data.ServerApi
import com.example.dnevtukhova.searchfilmsapp.domain.FilmsInteractor
import com.squareup.picasso.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class App : Application() {
    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/movie/"
        lateinit var instance: App
            private set
        const val API_KEY = "79c459d10744203ee914c139f789d1e8"
        const val LANGUAGE = "ru-RUS"
        const val CURRENT_DATE = "current date"
        const val PAGE_NUMBER = "page number"
        var favoriteF: Boolean = false
        var listF: Boolean = true
        var watchLaterF: Boolean = false
    }

    lateinit var api: ServerApi
    lateinit var filmsInteractor: FilmsInteractor
    private lateinit var filmsRepository: FilmsRepository
    private var INSTANCE: FilmsDb? = null
    private val MIGRATION_1_2 = object: Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE 'watchLater_table' ( 'id' INTEGER, " +
                    "'title' TEXT, " +
                    "'description' TEXT, " +
                    "'image' TEXT, " +
                    "'favorite' INTEGER, " +
                    "'watchLater' INTEGER, " +
                    " 'dateToWatch' INTEGER, " +
                    "PRIMARY KEY('id'))")
            database.execSQL("ALTER TABLE films_table ADD COLUMN watchLater DEFAULT 1 NOT NULL")
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        initDb()
        filmsRepository = FilmsRepository(INSTANCE)
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
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(client)
            .build()
            .create(ServerApi::class.java)
    }

    private fun initInteractor() {
        filmsInteractor = FilmsInteractor(api, filmsRepository)
    }

    private fun initDb(): FilmsDb? {
        if (INSTANCE == null) {
            synchronized(FilmsDb::class) {

                INSTANCE = Room.databaseBuilder(
                    applicationContext,
                    FilmsDb::class.java,
                    "films_database"

                )
                  //  .allowMainThreadQueries()
                    .addMigrations(MIGRATION_1_2)
                    .build()
            }
        }
        return INSTANCE
    }
}