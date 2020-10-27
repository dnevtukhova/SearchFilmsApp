package com.example.dnevtukhova.searchfilmsapp.presentation.view

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.dnevtukhova.searchfilmsapp.R
import com.example.dnevtukhova.searchfilmsapp.presentation.view.adapter.FilmsItemViewHolder
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class FilmsListFragmentTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun loadFilms() {
        onView(withId(R.id.all_films)).perform(
            click()
        )
        val action = scrollToPosition<RecyclerView.ViewHolder>(12)
        onView(withId(R.id.recyclerViewFragment)).perform(action)
        onView(withId(R.id.recyclerViewFragment)).check(matches(isDisplayed()))
    }

    @Test
    fun openDetailFilm() {
        onView(withId(R.id.recyclerViewFragment)).perform(
            RecyclerViewActions.actionOnItemAtPosition<FilmsItemViewHolder>(
                3,
                click()
            )
        )
        onView(withId(R.id.detailFragment)).check(matches(isDisplayed()))
    }

    @Test
    fun addToFavorite() {
        onView(withId(R.id.recyclerViewFragment)).perform(
            RecyclerViewActions.actionOnItemAtPosition<FilmsItemViewHolder>(
                0,
                onClickView(R.id.imageFavourite)
            )
        )
        onView(withId(R.id.film_favorite)).perform(
            click()
        )
        onView(withId(R.id.film_favorite)).check(matches(isDisplayed()))
    }

    private fun onClickView(id: Int): ViewAction {
        return object : ViewAction {
            override fun getDescription(): String {
                return "Add to favorite clicked"
            }

            override fun getConstraints(): Matcher<View>? {
                return null
            }

            override fun perform(uiController: UiController?, view: View?) {
                val addTOFavoritesButton = view?.findViewById<View>(id)
                addTOFavoritesButton?.performClick()
            }
        }
    }
}