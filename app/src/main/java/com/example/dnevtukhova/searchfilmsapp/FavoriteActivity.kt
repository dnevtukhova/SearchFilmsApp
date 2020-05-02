package com.example.dnevtukhova.searchfilmsapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

class FavoriteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme)
        }
        setContentView(R.layout.activity_favorite)
        initRecycler()
    }

    fun clickExplicit(value: FilmsItem) {
        val intent: Intent = DetailActivity.newIntent(this, value)
        startActivityForResult(intent, MainActivity.CODE)
    }

    fun getAnswer(intent: Intent, item: FilmsItem) {
        intent.putExtra(EXTRA_FAVORITE_DELETE, item)
        setResult(Activity.RESULT_OK, intent)
    }

    private fun initRecycler() {
        val recycler = findViewById<RecyclerView>(R.id.recyclerViewFavorite)
        recycler.adapter = FilmsFavoriteAdapter(this,
            LayoutInflater.from(this),
            App.itemsFavorite,
            //по долгому клику удаление элемента
            object : FilmsFavoriteAdapter.OnFilmsLongClickListener {
                override fun onFilmsLongFClick(filmsItem: FilmsItem, position: Int): Boolean {
                    App.itemsFavorite.remove(filmsItem)
                    Toast.makeText(
                        applicationContext,
                        "Удален элемент № $position",
                        Toast.LENGTH_SHORT
                    ).show()
                    recycler.adapter?.notifyDataSetChanged()
                    getAnswer(intent, filmsItem)
                    return true
                }
                override fun onFilmsFClick(filmsItem: FilmsItem, position: Int) {
                    clickExplicit(filmsItem)
                }
            })

        val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        itemDecoration.setDrawable(getDrawable(R.drawable.white_line))
        recycler.addItemDecoration(itemDecoration)
    }

    //region adapter and holder
    class FilmsFavouriteItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTv: TextView = itemView.findViewById(R.id.titleTv)
        private val subtitleTv: TextView = itemView.findViewById(R.id.subtitleTv)
        private val imageFilm: ImageView = itemView.findViewById(R.id.image)
        var container: ConstraintLayout = itemView.findViewById(R.id.container)

        fun bind(item: FilmsItem) {
            titleTv.setText(item.title)
            subtitleTv.setText(item.description)
            imageFilm.setImageResource(item.image)
        }
    }

    class FilmsFavoriteAdapter(
        private val context: Context,
        private val inflater: LayoutInflater,
        private val items: List<FilmsItem>,
        private val listener: OnFilmsLongClickListener
    ) :
        RecyclerView.Adapter<FilmsFavouriteItemViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): FilmsFavouriteItemViewHolder {
            return FilmsFavouriteItemViewHolder(inflater.inflate(R.layout.item_news, parent, false))
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: FilmsFavouriteItemViewHolder, position: Int) {
            holder.container.setAnimation(
                AnimationUtils.loadAnimation(
                    context,
                    R.anim.my_animation
                )
            )
            val item = items[position]
            holder.bind(item)
            holder.itemView.setOnLongClickListener { listener.onFilmsLongFClick(item, position) }
            holder.itemView.setOnClickListener { listener.onFilmsFClick(item, position) }
        }

        interface OnFilmsLongClickListener {
            fun onFilmsLongFClick(filmsItem: FilmsItem, position: Int): Boolean
            fun onFilmsFClick(filmsItem: FilmsItem, position: Int)
        }
    }
    //endregion

    companion object {
        const val EXTRA_FAVORITE_DELETE = "com.example.dnevtukhova.searchfilmsapp.extra_favorite_delete"

        fun newIntentF(packageContext: Context): Intent {
            val intent = Intent(packageContext, FavoriteActivity::class.java)
            return intent
        }
    }
}