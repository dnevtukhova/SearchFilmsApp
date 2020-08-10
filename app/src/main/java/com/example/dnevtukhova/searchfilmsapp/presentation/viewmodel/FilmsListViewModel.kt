package com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dnevtukhova.searchfilmsapp.App
import com.example.dnevtukhova.searchfilmsapp.App.Companion.CURRENT_DATE
import com.example.dnevtukhova.searchfilmsapp.App.Companion.PAGE_NUMBER
import com.example.dnevtukhova.searchfilmsapp.data.FavoriteItem
import com.example.dnevtukhova.searchfilmsapp.data.FilmsItem
import com.example.dnevtukhova.searchfilmsapp.data.WatchLaterItem
import com.example.dnevtukhova.searchfilmsapp.domain.FilmsInteractor
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import java.util.*

class FilmsListViewModel(private val filmsInteractor: FilmsInteractor) : ViewModel() {
    private var filmsLiveData: LiveData<List<FilmsItem>> =
        LiveDataReactiveStreams.fromPublisher(filmsInteractor.getFilms()!!.subscribeOn(Schedulers.io()))
    private val favoriteLiveData: LiveData<List<FavoriteItem>>? =
        LiveDataReactiveStreams.fromPublisher(filmsInteractor.getFavorite()!!.subscribeOn(Schedulers.io()))
    private val filmsDetailLiveData = MutableLiveData<FilmsItem>()
    private val watchLaterLiveData: LiveData<List<WatchLaterItem>>? =
        LiveDataReactiveStreams.fromPublisher(
            filmsInteractor.getWatchLater()!!.subscribeOn(
                Schedulers.io()
            )
        )
    private val errorLiveData = SingleLiveEvent<String>()
    lateinit var mSettings: SharedPreferences

    init {
        initSharedPref()
    }

    val films: LiveData<List<FilmsItem>>?
        get() = filmsLiveData

    val favoriteFilms: LiveData<List<FavoriteItem>>?
        get() = favoriteLiveData

    val watchLaterFilms: LiveData<List<WatchLaterItem>>?
        get() = watchLaterLiveData

    val filmsDetail: LiveData<FilmsItem>
        get() = filmsDetailLiveData

    val error: LiveData<String>
        get() = errorLiveData

    //films
    fun refreshAllFilms() {
        filmsInteractor.getFilms(
            App.API_KEY,
            App.LANGUAGE,
            mSettings.getInt(PAGE_NUMBER, 0),
            object : FilmsInteractor.GetFilmsCallback {
                override fun onSuccess(films: Flowable<List<FilmsItem>>?) {
                }

                override fun onError(error: String) {
                    errorLiveData.postValue(error)
                }
            })
    }

    fun selectFilm(filmsItem: FilmsItem) {
        filmsDetailLiveData.postValue(filmsItem)
    }

    fun removeAllFilms() {
        filmsInteractor.removeAllFilms()
    }

    //favoriteFilms
    fun selectFavorite(filmsItem: FilmsItem) {
        filmsInteractor.selectFavorite(filmsItem)
    }

    fun removeFromFavorite(filmsItem: FavoriteItem, favorite: Boolean) {
        filmsInteractor.removeFromFavorite(filmsItem, favorite)
    }

    fun addToFavorite(filmsItem: FavoriteItem, favorite: Boolean) {
        filmsInteractor.addToFavorite(filmsItem, favorite)
    }

    //watchLaterFilms
    fun selectWatchLater(filmsItem: FilmsItem, dateToWatch: Long) {
        filmsInteractor.selectWatchLater(filmsItem, dateToWatch)
    }

    fun setDateToWatch(watchLaterItem: WatchLaterItem) {
        filmsInteractor.setDateToWatch(watchLaterItem)
    }

    fun initSharedPref() {
        mSettings = App.instance.applicationContext.getSharedPreferences(
            "Settings",
            Context.MODE_PRIVATE
        )
        val currentDate = GregorianCalendar().timeInMillis
        Log.d("currentDate", "$currentDate")
        val dateInPref = mSettings.getLong(CURRENT_DATE, 0)
        Log.d("dateInPref", "$dateInPref")
        if (dateInPref == 0L || ((currentDate - dateInPref) >= 1200000L)) {
            mSettings.edit {
                putLong(CURRENT_DATE, currentDate)
            }
            Log.d(TAG, "обновили SharedPref")
            mSettings.edit {
                putInt(PAGE_NUMBER, 1)
            }
            removeAllFilms()
            refreshAllFilms()
        }
    }

    companion object {
        const val TAG = "FilmListViewModel"
    }
}