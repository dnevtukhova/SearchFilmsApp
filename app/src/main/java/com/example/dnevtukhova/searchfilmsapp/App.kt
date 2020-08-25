package com.example.dnevtukhova.searchfilmsapp

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.dnevtukhova.searchfilmsapp.data.FilmsRepository
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants.BASE_URL
import com.example.dnevtukhova.searchfilmsapp.data.api.ServerApi
import com.example.dnevtukhova.searchfilmsapp.data.db.FilmsDb
import com.example.dnevtukhova.searchfilmsapp.domain.FilmsInteractor
import com.squareup.picasso.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class App : Application() {
    companion object {
        lateinit var instance: App
            private set
        var favoriteF: Boolean = false
        var listF: Boolean = true
        var watchLaterF: Boolean = false
    }

    lateinit var api: ServerApi
    lateinit var filmsInteractor: FilmsInteractor
    private lateinit var filmsRepository: FilmsRepository
    private var DBInstance: FilmsDb? = null
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
    private val MIGRATION_2_3 = object: Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE films_table ADD COLUMN dateToWatch DEFAULT 1 NOT NULL")
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        initDb()
        filmsRepository = FilmsRepository(DBInstance)
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
        if (DBInstance == null) {
            synchronized(FilmsDb::class) {

                DBInstance = Room.databaseBuilder(
                    applicationContext,
                    FilmsDb::class.java,
                    "films_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .build()
            }
        }
        return DBInstance
    }
}