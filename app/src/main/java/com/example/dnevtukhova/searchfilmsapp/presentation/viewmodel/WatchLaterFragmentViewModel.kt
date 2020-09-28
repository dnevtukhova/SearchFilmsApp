package com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel
import com.example.dnevtukhova.searchfilmsapp.App
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants
import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmsItem
import com.example.dnevtukhova.searchfilmsapp.domain.FilmsInteractor
import io.reactivex.schedulers.Schedulers
import java.util.HashSet
import javax.inject.Inject

class WatchLaterFragmentViewModel @Inject constructor(val filmsInteractor: FilmsInteractor) :
    ViewModel() {
    private val watchLaterLiveData: LiveData<List<FilmsItem>>? =
        LiveDataReactiveStreams.fromPublisher(
            filmsInteractor.getWatchLater()!!.subscribeOn(
                Schedulers.io()
            )
        )
    lateinit var mSettings: SharedPreferences

    val watchLaterFilms: LiveData<List<FilmsItem>>?
        get() = watchLaterLiveData

    fun setDateToWatch(watchLaterItem: FilmsItem) {
       filmsInteractor.setDateToWatch(watchLaterItem)
       mSettings = App.instance.applicationContext.getSharedPreferences(
            "Settings",
            Context.MODE_PRIVATE
        )
        if (!watchLaterItem.watchLater) {
            val set = mSettings.getStringSet(NetworkConstants.WATCHLATER, HashSet<String>())
            set.add(watchLaterItem.id.toString())
            mSettings.edit { putStringSet(NetworkConstants.WATCHLATER, set) }
            mSettings.edit { putLong(watchLaterItem.id.toString(), watchLaterItem.dateToWatch!!) }
        } else {
            val set = mSettings.getStringSet(NetworkConstants.WATCHLATER, HashSet<String>())
            set.remove(watchLaterItem.id.toString())
            mSettings.edit { putStringSet(NetworkConstants.WATCHLATER, set) }
        }
    }
}