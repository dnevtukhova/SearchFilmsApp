package com.example.dnevtukhova.searchfilmsapp.domain

import com.example.dnevtukhova.searchfilmsapp.data.FilmsRepository
import com.example.dnevtukhova.searchfilmsapp.data.api.ServerApi
import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmsItem
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

internal class FilmsInteractorTest {
    val repo = mock(FilmsRepository::class.java)
    val serverApi = mock(ServerApi::class.java)
    val filmsInteractor = FilmsInteractor(serverApi, repo)
    val filmsItem = FilmsItem(1, "filmTest", "description", "image", true, true, null, 1f)

    @Test
    fun selectFavorite() {
        filmsInteractor.selectFavorite(filmsItem)
        verify(repo).setFilms(filmsItem)
        assertThat(filmsItem.favorite, `is`(false))
    }

    @Test
    fun changeWatchLater() {
        filmsInteractor.changeWatchLater(filmsItem)
        verify(repo).setFilms(filmsItem)
    }

    @Test
    fun setDateToWatch() {
        filmsInteractor.setDateToWatch(filmsItem)
        verify(repo).setDateToWatch(filmsItem)
    }

    @Test
    fun getFilms() {
        filmsInteractor.getFilms()
        verify(repo).films
    }

    @Test
    fun getFavorite() {
        filmsInteractor.getFavorite()
        verify(repo).favoriteFilms
    }


}