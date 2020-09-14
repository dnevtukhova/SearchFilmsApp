package com.example.dnevtukhova.searchfilmsapp.data.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmsItem
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FilmsDaoTest : FilmsDbTest() {

    private lateinit var filmsTest: MutableList<FilmsItem>

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        filmsTest = mutableListOf<FilmsItem>()
        filmsTest.add(FilmsItem(1, "filmTest", "description", "image", true, true, null))
        filmsTest.add(
            FilmsItem(
                2,
                "filmTest2",
                "description2",
                "image2",
                true,
                false,
                1212121212121L
            )
        )
        db.getFilmsDao().insertAll(filmsTest)
    }

    @Test
    fun insertAndRead() {
        val films = db.getFilmsDao().getFilms().blockingFirst()
        assertThat(films.size, `is`(2))
        assertThat(films, notNullValue())
    }

    @Test
    fun getAllWatchLater() {
        val filmsWithDateToWatch = db.getFilmsDao().getAllWatchLater().blockingFirst()
        assertThat(filmsWithDateToWatch.size, `is`(1))
        assertThat(filmsWithDateToWatch.get(0).dateToWatch, `is`(1212121212121L))
    }

    @Test
    fun setIsFavorite() {
        db.getFilmsDao().setFilms(1, false)
        val films = db.getFilmsDao().getFilms().blockingFirst()
        assertThat(films.get(0).favorite, `is`(false))
    }
}