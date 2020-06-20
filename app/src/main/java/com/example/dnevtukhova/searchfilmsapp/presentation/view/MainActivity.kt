package com.example.dnevtukhova.searchfilmsapp.presentation.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.dnevtukhova.searchfilmsapp.App
import com.example.dnevtukhova.searchfilmsapp.R
import com.example.dnevtukhova.searchfilmsapp.data.FavoriteItem
import com.example.dnevtukhova.searchfilmsapp.data.FilmsItem
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(),
    FilmsListFragment.FilmsListListener,
    FavoriteFragment.FilmsFavoriteAdapter.OnFavoriteFilmsClickListener {

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme)
        }
        setContentView(R.layout.activity_main)
        App.favoriteF = false
        if (App.listF) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    FilmsListFragment(),
                    FilmsListFragment.TAG
                )
                .commit()
        } else {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    FavoriteFragment(),
                    FavoriteFragment.TAG
                )
                .commit()
        }
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
        } else {
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.films_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.invite_friend -> {
                val i = Intent(Intent.ACTION_SEND)
                i.type = "text/plain"
                i.putExtra(Intent.EXTRA_TEXT, getString(R.string.invite))
                startActivity(i)
            }
            R.id.app_theme -> {
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                finish()
                startActivity(Intent(applicationContext, MainActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setBottomNavigation() {
        val bar: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bar.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.film_favorite -> {
                    App.favoriteF = true
                    App.listF = false
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
                    App.listF = true
                    App.favoriteF = false
                    supportFragmentManager
                        .beginTransaction()
                        .replace(
                            R.id.fragmentContainer,
                            FilmsListFragment(),
                            FilmsListFragment.TAG
                        )
                        .commit()
                }
            }
            true
        }
    }

    override fun onFavoriteFilmsLongClick(filmsItem: FavoriteItem, position: Int): Boolean {
        return true
    }

    override fun onFavoriteFilmsFClick(filmsItem: FavoriteItem, position: Int) {
        openDetailed()
    }
}


