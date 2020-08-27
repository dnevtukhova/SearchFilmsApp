package com.example.dnevtukhova.searchfilmsapp.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel.*
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(FilmsListViewModel::class)
    abstract fun filmsViewModel(viewModel: FilmsListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DetailFragmentViewModel::class)
    abstract fun detailFragmentViewModel(viewModel: DetailFragmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FavoriteFragmentViewModel::class)
    abstract fun favoriteFragmentViewModel(viewModel: FavoriteFragmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(WatchLaterFragmentViewModel::class)
    abstract fun watchLaterFragmentiewModel(viewModel: WatchLaterFragmentViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: FilmsViewModelFactory): ViewModelProvider.Factory
}