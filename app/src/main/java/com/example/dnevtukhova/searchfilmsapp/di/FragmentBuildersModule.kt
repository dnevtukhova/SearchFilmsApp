package com.example.dnevtukhova.searchfilmsapp.di

import com.example.dnevtukhova.searchfilmsapp.presentation.view.DetailFragment
import com.example.dnevtukhova.searchfilmsapp.presentation.view.FavoriteFragment
import com.example.dnevtukhova.searchfilmsapp.presentation.view.FilmsListFragment
import com.example.dnevtukhova.searchfilmsapp.presentation.view.WatchLaterFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeFilmsListFragment(): FilmsListFragment

    @ContributesAndroidInjector
    abstract fun contributeFavoriteFragment(): FavoriteFragment

    @ContributesAndroidInjector
    abstract fun contributeDetailFragment(): DetailFragment

    @ContributesAndroidInjector
    abstract fun contributeWatchLaterFragment(): WatchLaterFragment
}