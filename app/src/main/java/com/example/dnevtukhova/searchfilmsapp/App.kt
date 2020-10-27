package com.example.dnevtukhova.searchfilmsapp

import android.app.Activity
import android.app.Application
import com.example.dnevtukhova.searchfilmsapp.di.AppInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject


class App : Application(), HasActivityInjector {

    companion object {
        lateinit var instance: App
        var isFavoriteFragmentToOpen: Boolean = false
        var isListFragmentToOpen: Boolean = true
        var isSettingsFragmentToOpen: Boolean = false
        var isDetailFragmentToOpen: Boolean = false
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()
        instance = this
        AppInjector.init(this)
    }

    override fun activityInjector() = dispatchingAndroidInjector
}