package com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Environment
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.dnevtukhova.searchfilmsapp.App
import com.example.dnevtukhova.searchfilmsapp.R
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants
import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmsItem
import com.example.dnevtukhova.searchfilmsapp.domain.FilmsInteractor
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.schedulers.Schedulers
import testing.OpenForTesting
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.HashSet
import javax.inject.Inject

@OpenForTesting
class DetailFragmentViewModel @Inject constructor(val filmsInteractor: FilmsInteractor) : ViewModel() {
    private val filmsDetailLiveData = MutableLiveData<FilmsItem>()
    private val loadImageLiveData = MutableLiveData<State>()
    lateinit var mSettings: SharedPreferences

    val filmsDetail: LiveData<FilmsItem>
        get() = filmsDetailLiveData

    val loadImageLiveD: LiveData<State>
        get() = loadImageLiveData

    fun selectFilm(filmsItem: FilmsItem) {
        filmsDetailLiveData.postValue(filmsItem)
    }

    fun loadImage(filmsDetailItem: FilmsItem, context: Context) {
        Completable.fromRunnable {
            saveGallery(filmsDetailItem, context)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableCompletableObserver() {
                override fun onComplete() {
                }

                override fun onError(e: Throwable) {
                    loadImageLiveData.postValue(State.Error("${
                        R.string.errorText} $e"))
                }
            })
    }

    private fun saveGallery(filmsDetailItem: FilmsItem, context: Context) {
        Glide.with(context)
            .asBitmap()
            .load(NetworkConstants.PICTURE + filmsDetailItem.image)
            .into(object : CustomTarget<Bitmap>(500, 500) {
                override fun onLoadCleared(placeholder: Drawable?) {
                    loadImageLiveData.postValue(State.Error("onLoadCleared"))
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    saveImage(resource, filmsDetailItem)
                }
            })
    }

    private fun saveImage(image: Bitmap, filmsDetailItem: FilmsItem) {
        val imageFileName = "JPEG_ " + filmsDetailItem.title + ".jpg"
        val storageDir =
            Environment.getExternalStoragePublicDirectory("${Environment.DIRECTORY_PICTURES}/SearchFilmsApp")
        var success = true
        if (!storageDir.exists()) {
            success = storageDir.mkdirs()
        }
        if (success) {
            val imageFile = File(storageDir, imageFileName)
            val savedImagePath = imageFile.absolutePath
            try {
                val fOut: OutputStream = FileOutputStream(imageFile)
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // Add the image to the system gallery
            loadImageLiveData.postValue(State.Success(savedImagePath))
        }
    }

    fun selectFavorite(filmsItem: FilmsItem) {
        filmsInteractor.selectFavorite(filmsItem)
        mSettings = App.instance.applicationContext.getSharedPreferences(
            FilmsListViewModel.SETTINGS,
            Context.MODE_PRIVATE
        )
        if (!filmsItem.favorite) {
            val set = mSettings.getStringSet(NetworkConstants.FAVORITE, HashSet<String>())
            set?.add(filmsItem.id.toString())
            Log.d("set size", set?.size.toString())
            mSettings.edit { putStringSet(NetworkConstants.FAVORITE, set) }

        } else {
            val set = mSettings.getStringSet(NetworkConstants.FAVORITE, HashSet<String>())
            Log.d("set size delete", set.toString())
            set?.remove(filmsItem.id.toString())
            Log.d("set size delete", set!!.size.toString())
            mSettings.edit {
                putStringSet(NetworkConstants.FAVORITE, set)
            }
        }
    }

    fun changeWatchLater(filmsItem: FilmsItem) {
        filmsInteractor.changeWatchLater(filmsItem)
        mSettings = App.instance.applicationContext.getSharedPreferences(
            FilmsListViewModel.SETTINGS,
            Context.MODE_PRIVATE
        )
        if (!filmsItem.watchLater) {
            val set = mSettings.getStringSet(NetworkConstants.WATCHLATER, HashSet<String>())
            set?.add(filmsItem.id.toString())
            mSettings.edit { putStringSet(NetworkConstants.WATCHLATER, set) }
            mSettings.edit { putLong(filmsItem.id.toString(), filmsItem.dateToWatch!!) }
        } else {
            val set = mSettings.getStringSet(NetworkConstants.WATCHLATER, HashSet<String>())
            set?.remove(filmsItem.id.toString())
            mSettings.edit { putStringSet(NetworkConstants.WATCHLATER, set) }
        }

    }

    sealed class State {
        data class Error(val error: String?) : State()
        data class Success(val imagePath: String?) : State()
    }
}