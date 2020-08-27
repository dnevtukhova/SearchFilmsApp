package com.example.dnevtukhova.searchfilmsapp.di

import com.example.dnevtukhova.searchfilmsapp.presentation.view.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class MainActivityModule {
    @ContributesAndroidInjector(modules = [FragmentBuildersModule::class])
    abstract fun contributeMainActivity(): MainActivity
}
