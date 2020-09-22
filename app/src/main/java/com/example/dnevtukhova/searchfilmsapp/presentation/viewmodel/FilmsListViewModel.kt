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
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants.API_KEY
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants.CURRENT_DATE
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants.FAVORITE
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants.LANGUAGE
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants.PAGE_NUMBER
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants.WATCHLATER
import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmsItem
import com.example.dnevtukhova.searchfilmsapp.domain.FilmsInteractor
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import testing.OpenForTesting
import java.util.*
import javax.inject.Inject

@OpenForTesting
class FilmsListViewModel @Inject constructor(val filmsInteractor: FilmsInteractor) : ViewModel() {
    private var filmsLiveData: LiveData<List<FilmsItem>> =
        LiveDataReactiveStreams.fromPublisher(
            filmsInteractor.getFilms()!!.subscribeOn(Schedulers.io())
        )
    private val errorLiveData = SingleLiveEvent<String>()
    private val filmsSearchLiveData = MutableLiveData<MutableList<FilmsItem>>()
    private val progressBarLiveData = MutableLiveData<Boolean>()
    lateinit var mSettings: SharedPreferences

    init {
        initSharedPref()
    }

    val films: LiveData<List<FilmsItem>>?
        get() = filmsLiveData

    val error: LiveData<String>
        get() = errorLiveData

    val searchFilms: MutableLiveData<MutableList<FilmsItem>>?
        get() = filmsSearchLiveData

    val progressBar: MutableLiveData<Boolean>
        get() = progressBarLiveData

    fun refreshAllFilms() {
        progressBarLiveData.postValue(true)
        filmsInteractor.getFilms(
            API_KEY,
            LANGUAGE,
            mSettings.getInt(PAGE_NUMBER, 0),
            object : FilmsInteractor.GetFilmsCallback {
                override fun onSuccess(films: Flowable<List<FilmsItem>>?) {
                    progressBarLiveData.postValue(false)
                }

                override fun onSuccess(films: MutableList<FilmsItem>) {
                    progressBarLiveData.postValue(false)
                }

                override fun onError(error: String) {
                    progressBarLiveData.postValue(false)
                    errorLiveData.postValue(error)
                }
            })
    }

    fun getFilmsFromSearch(query: String) {
        progressBarLiveData.postValue(true)
        filmsInteractor.getSearchFilms(
            API_KEY,
            LANGUAGE,
            1,
            query,
            object : FilmsInteractor.GetFilmsCallback {
                override fun onSuccess(films: Flowable<List<FilmsItem>>?) {
                    progressBarLiveData.postValue(false)
                }

                override fun onSuccess(films: MutableList<FilmsItem>) {
                    filmsSearchLiveData.postValue(films)
                    progressBarLiveData.postValue(false)
                }

                override fun onError(error: String) {
                    progressBarLiveData.postValue(false)
                    errorLiveData.postValue(error)
                }
            }
        )
    }

    fun selectFavorite(filmsItem: FilmsItem) {
        filmsInteractor.selectFavorite(filmsItem)
        mSettings = App.instance.applicationContext.getSharedPreferences(
            "Settings",
            Context.MODE_PRIVATE
        )
        if (!filmsItem.favorite) {
            val set = mSettings.getStringSet(FAVORITE, HashSet<String>())
            set.add(filmsItem.id.toString())
            Log.d("set size", set.size.toString())
            mSettings.edit { putStringSet(FAVORITE, set) }

        } else {
            val set = mSettings.getStringSet(FAVORITE, HashSet<String>())
            Log.d("set size delete", set.toString())
            set.remove(filmsItem.id.toString())
            Log.d("set size delete", set.size.toString())
            mSettings.edit {
                putStringSet(FAVORITE, set)
            }
        }
    }

    fun changeWatchLater(filmsItem: FilmsItem) {
        filmsInteractor.changeWatchLater(filmsItem)
        mSettings = App.instance.applicationContext.getSharedPreferences(
            "Settings",
            Context.MODE_PRIVATE
        )
        if (!filmsItem.favorite) {
            val set = mSettings.getStringSet(WATCHLATER, HashSet<String>())
            set.add(filmsItem.id.toString())
            mSettings.edit { putStringSet(WATCHLATER, set) }
            mSettings.edit { putLong(filmsItem.id.toString(), filmsItem.dateToWatch!!) }
        } else {
            val set = mSettings.getStringSet(WATCHLATER, HashSet<String>())
            set.remove(filmsItem.id.toString())
            mSettings.edit { putStringSet(WATCHLATER, set) }
        }

    }

    fun removeAllFilms() {
        filmsInteractor.removeAllFilms()
    }

    fun addFilm(filmsItem: FilmsItem) {
        filmsInteractor.addFilm(filmsItem)
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