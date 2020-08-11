package com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel
import com.example.dnevtukhova.searchfilmsapp.App
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants.API_KEY
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants.CURRENT_DATE
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants.LANGUAGE
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants.PAGE_NUMBER
import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmsItem
import com.example.dnevtukhova.searchfilmsapp.domain.FilmsInteractor
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import java.util.*

class FilmsListViewModel(private val filmsInteractor: FilmsInteractor) : ViewModel() {
    private var filmsLiveData: LiveData<List<FilmsItem>> =
        LiveDataReactiveStreams.fromPublisher(filmsInteractor.getFilms()!!.subscribeOn(Schedulers.io()))
    private val errorLiveData = SingleLiveEvent<String>()
    lateinit var mSettings: SharedPreferences

    init {
        initSharedPref()
    }

    val films: LiveData<List<FilmsItem>>?
        get() = filmsLiveData

    val error: LiveData<String>
        get() = errorLiveData

    fun refreshAllFilms() {
        filmsInteractor.getFilms(
            API_KEY,
            LANGUAGE,
            mSettings.getInt(PAGE_NUMBER, 0),
            object : FilmsInteractor.GetFilmsCallback {
                override fun onSuccess(films: Flowable<List<FilmsItem>>?) {
                }

                override fun onError(error: String) {
                    errorLiveData.postValue(error)
                }
            })
    }

    fun selectFavorite(filmsItem: FilmsItem) {
        filmsInteractor.selectFavorite(filmsItem)
    }

    fun changeWatchLater(filmsItem: FilmsItem) {
        filmsInteractor.changeWatchLater(filmsItem)
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
            //    removeAllFilms()
            refreshAllFilms()
        }
    }

    companion object {
        const val TAG = "FilmListViewModel"
    }
}