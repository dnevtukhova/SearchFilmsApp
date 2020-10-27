package com.example.dnevtukhova.searchfilmsapp.data

import com.example.dnevtukhova.searchfilmsapp.data.db.FilmsDao
import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmsItem
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify


@RunWith(JUnit4::class)
class FilmsRepositoryTest {

    private lateinit var filmsDao: FilmsDao
    private lateinit var repo: FilmsRepository
    private var filmsTest = mutableListOf<FilmsItem>()
    private lateinit var film: FilmsItem

    @Before
    fun initializeFilmsListTest() {
        filmsDao = spy(FilmsDao::class.java)
        repo = FilmsRepository(filmsDao)
        filmsTest = mutableListOf<FilmsItem>()
        film = FilmsItem(2, "filmTest2", "description2", "image2", true, true, 1212121212121L, 1f)
        filmsTest.add(FilmsItem(1, "filmTest", "description", "image", true, true, null, 1f))
    }

    @Test
    fun getFilmsDao() {
        repo.films
        verify(filmsDao).getFilms()
    }

    @Test
    fun getFilms() {
        repo.films
        verify(filmsDao).getFilms()
    }

    @Test
    fun getFavoriteFilms() {
        repo.favoriteFilms
        verify(filmsDao).getAllFavorite()
    }

    @Test
    fun getWatchLaterFilms() {
        repo.watchLaterFilms
        verify(filmsDao).getAllWatchLater()
    }
}