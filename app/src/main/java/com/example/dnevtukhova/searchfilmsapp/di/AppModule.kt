package com.example.dnevtukhova.searchfilmsapp.di

import android.app.Application
import androidx.room.Room
import com.example.dnevtukhova.searchfilmsapp.BuildConfig
import com.example.dnevtukhova.searchfilmsapp.data.FilmsRepository
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants
import com.example.dnevtukhova.searchfilmsapp.data.api.ServerApi
import com.example.dnevtukhova.searchfilmsapp.data.db.FilmsDao
import com.example.dnevtukhova.searchfilmsapp.data.db.FilmsDb
import com.example.dnevtukhova.searchfilmsapp.domain.FilmsInteractor
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {

    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): ServerApi {
        return Retrofit.Builder()
            .baseUrl(NetworkConstants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ServerApi::class.java)
    }

    @Provides
    fun provideOkHttp(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor()
                    .apply {
                        if (BuildConfig.DEBUG) {
                            level = HttpLoggingInterceptor.Level.BODY
                        }
                    })
            .build()
    }

    @Singleton
    @Provides
    fun providesRoomDatabase(app: Application): FilmsDb {
        return Room
            .databaseBuilder(app, FilmsDb::class.java, "films_database")
            .build()
    }

    @Singleton
    @Provides
    fun providesFilmsRepository(filmsDao: FilmsDao): FilmsRepository {
        return FilmsRepository(filmsDao)
    }

    @Singleton
    @Provides
    fun providesFilmsInteractor(api: ServerApi, filmsRepository: FilmsRepository): FilmsInteractor {
        return FilmsInteractor(api, filmsRepository)
    }

    @Singleton
    @Provides
    fun providesFilmsDAO(filmsDb: FilmsDb): FilmsDao {
        return filmsDb.getFilmsDao()
    }
}