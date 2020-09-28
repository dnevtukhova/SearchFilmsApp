package com.example.dnevtukhova.searchfilmsapp.presentation.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.dnevtukhova.searchfilmsapp.App
import com.example.dnevtukhova.searchfilmsapp.R
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants
import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmsItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject


class MainActivity : AppCompatActivity(),
    FilmsListFragment.FilmsListListener,
    FavoriteFragment.FilmsFavoriteAdapter.OnFavoriteFilmsClickListener, HasSupportFragmentInjector {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    var filmsItem: FilmsItem? = null

    companion object {
        const val FILM_FROM_NOTIFICATION = "film from notification"
        const val TAG = "MainActivity"
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mSettings = App.instance.applicationContext.getSharedPreferences(
            "Settings",
            Context.MODE_PRIVATE
        )
        val theme = mSettings.getBoolean(NetworkConstants.THEME, true)
        if (theme) {
            setTheme(R.style.LightTheme)
        } else {
            setTheme(R.style.DarkTheme)
        }
        filmsItem = intent.getParcelableExtra(FILM_FROM_NOTIFICATION)
        openFragment(filmsItem)
        setBottomNavigation()
    }

    override fun onBackPressed() {
        supportActionBar?.show()
        if (supportFragmentManager.fragments.last() == supportFragmentManager.findFragmentByTag(
                DetailFragment.TAG
            )
        ) {
            if (App.favoriteF) {
                supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        FavoriteFragment(),
                        FavoriteFragment.TAG
                    )
                    .commit()
            }
            if (App.listF) {
                supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        FilmsListFragment(),
                        FilmsListFragment.TAG
                    )
                    .commit()
            }
        } else if (App.settingsF) {
            App.settingsF = false
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    FilmsListFragment(),
                    FilmsListFragment.TAG
                )
                .commit()
        } else {
            showExitDialog()
        }
    }

    private fun showExitDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.custom_dialog)
        val yesBtn = dialog.findViewById<Button>(R.id.button_yes)
        val noBtn = dialog.findViewById<Button>(R.id.button_no)
        yesBtn.setOnClickListener {
            finish()
        }
        noBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun openDetailed() {
        //скрыть toolbar при вызове фрагмента
        supportActionBar?.hide()
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                DetailFragment.newInstance(),
                DetailFragment.TAG
            )
            .addToBackStack(DetailFragment.TAG)
            .commit()
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        if (fragment is FilmsListFragment) {
            fragment.listener = this
        }
        if (fragment is FavoriteFragment) {
            fragment.listener = this
        }
    }

    override fun onFilmsSelected(filmsItem: FilmsItem) {
        openDetailed()
    }

    private fun setBottomNavigation() {
        val bar: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bar.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.film_favorite -> {
                    App.favoriteF = true
                    App.listF = false
                    App.watchLaterF = false
                    App.settingsF = false
                    supportFragmentManager
                        .beginTransaction()
                        .replace(
                            R.id.fragmentContainer,
                            FavoriteFragment(),
                            FavoriteFragment.TAG
                        )
                        .commit()
                }
                R.id.all_films -> {
                    supportActionBar?.show()
                    App.listF = true
                    App.favoriteF = false
                    App.watchLaterF = false
                    App.settingsF = false
                    supportFragmentManager
                        .beginTransaction()
                        .replace(
                            R.id.fragmentContainer,
                            FilmsListFragment(),
                            FilmsListFragment.TAG
                        )
                        .commit()
                }
                R.id.filmsWatchLater -> {
                    supportActionBar?.show()
                    App.listF = false
                    App.favoriteF = false
                    App.watchLaterF = true
                    App.settingsF = false
                    supportFragmentManager
                        .beginTransaction()
                        .replace(
                            R.id.fragmentContainer,
                            WatchLaterFragment(),
                            WatchLaterFragment.TAG
                        )
                        .commit()
                }
            }
            true
        }
    }

    override fun onFavoriteFilmsLongClick(filmsItem: FilmsItem, position: Int): Boolean {
        return true
    }

    override fun onFavoriteFilmsFClick(filmsItem: FilmsItem, position: Int) {
        openDetailed()
    }

    private fun openFragment(filmsItem: FilmsItem?) {
        if (filmsItem != null) {
            supportActionBar?.hide()
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    DetailFragment.newInstance(filmsItem),
                    DetailFragment.TAG
                )
                .addToBackStack(DetailFragment.TAG)
                .commit()
        } else {
            if (App.listF) {
                supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        FilmsListFragment(),
                        FilmsListFragment.TAG
                    )
                    .commit()
            }
            if (App.favoriteF) {
                supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        FavoriteFragment(),
                        FavoriteFragment.TAG
                    )
                    .commit()
            }
            if (App.watchLaterF) {
                supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        WatchLaterFragment(),
                        WatchLaterFragment.TAG
                    )
                    .commit()
            }
        }
    }

    override fun supportFragmentInjector() = dispatchingAndroidInjector
}



