package com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel
import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmsItem
import com.example.dnevtukhova.searchfilmsapp.domain.FilmsInteractor
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class WatchLaterFragmentViewModel @Inject constructor(val filmsInteractor: FilmsInteractor) :
    ViewModel() {
    private val watchLaterLiveData: LiveData<List<FilmsItem>>? =
        LiveDataReactiveStreams.fromPublisher(
            filmsInteractor.getWatchLater()!!.subscribeOn(
                Schedulers.io()
            )
        )

    val watchLaterFilms: LiveData<List<FilmsItem>>?
        get() = watchLaterLiveData

    fun setDateToWatch(watchLaterItem: FilmsItem) {
        filmsInteractor.setDateToWatch(watchLaterItem)
    }
}