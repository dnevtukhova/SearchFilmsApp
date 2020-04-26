package com.example.dnevtukhova.searchfilmsapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavoriteActivity : AppCompatActivity() {
    var itemsFavoyrit = ArrayList<FilmsItem>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppCompatDelegate.getDefaultNightMode() === AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme)
        }
        setContentView(R.layout.activity_favorite)

        var bundle: Bundle = intent.getExtras()
        itemsFavoyrit =
            bundle.getSerializable(FavoriteActivity.EXTRA_ID_FAVORITE) as ArrayList<FilmsItem>
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
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler.layoutManager = layoutManager
        recycler.adapter = FilmsFavoriteAdapter(LayoutInflater.from(this),
            itemsFavoyrit,
            //по долгому клику удаление элемента
            object : FilmsFavoriteAdapter.OnFilmsLongClickListener {
                override fun onFilmsLongFClick(filmsItem: FilmsItem, position: Int): Boolean {
                    itemsFavoyrit.remove(filmsItem)
                    Toast.makeText(
                        applicationContext,
                        "Удален элемент № " + position,
                        Toast.LENGTH_SHORT
                    ).show();
                    recycler.adapter?.notifyDataSetChanged()
                    getAnswer(intent, filmsItem)
                    return true;
                }

            },
            object : FilmsFavoriteAdapter.OnFilmsClickListener {
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
        val titleTv: TextView = itemView.findViewById(R.id.titleTv)
        val subtitleTv: TextView = itemView.findViewById(R.id.subtitleTv)
        val imageFilm: ImageView = itemView.findViewById(R.id.image)


        fun bind(item: FilmsItem) {
            titleTv.setText(item.title)
            subtitleTv.setText(item.description)
            imageFilm.setImageResource(item.image)


        }
    }

    class FilmsFavoriteAdapter(
        val inflater: LayoutInflater,
        val items: List<FilmsItem>,
        val listener: OnFilmsLongClickListener,
        val listener2: OnFilmsClickListener
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
            val item = items[position]
            holder.bind(item)
            holder.itemView.setOnLongClickListener { listener.onFilmsLongFClick(item, position) }
            holder.itemView.setOnClickListener { listener2.onFilmsFClick(item, position) }

        }

        interface OnFilmsLongClickListener {
            fun onFilmsLongFClick(filmsItem: FilmsItem, position: Int): Boolean

        }

        interface OnFilmsClickListener {
            fun onFilmsFClick(filmsItem: FilmsItem, position: Int)

        }

    }
    //endregion

    companion object {
        const val EXTRA_ID_FAVORITE = "com.example.dnevtukhova.searchfilmsapp.extra_id_favorite"
        const val EXTRA_FAVORITE_DELETE =
            "com.example.dnevtukhova.searchfilmsapp.extra_favorite_delete"


        fun newIntentF(packageContext: Context, itemFavorite: ArrayList<FilmsItem>): Intent {
            val intent = Intent(packageContext, FavoriteActivity::class.java)
            val bundle: Bundle = Bundle();
            bundle.putSerializable(FavoriteActivity.EXTRA_ID_FAVORITE, itemFavorite);
            intent.putExtras(bundle)
            return intent
        }
    }
}