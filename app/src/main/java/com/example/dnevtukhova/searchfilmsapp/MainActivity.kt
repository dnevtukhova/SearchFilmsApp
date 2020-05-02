package com.example.dnevtukhova.searchfilmsapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dnevtukhova.searchfilmsapp.DetailActivity.Companion.newIntent
import com.example.dnevtukhova.searchfilmsapp.FavoriteActivity.Companion.EXTRA_FAVORITE_DELETE
import com.example.dnevtukhova.searchfilmsapp.FavoriteActivity.Companion.newIntentF

class MainActivity : AppCompatActivity() {
    private var mCurrentIndex: Int = 0

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme)
        }
        setContentView(R.layout.activity_main)
        initRecycler()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.films_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.film_favorite -> {
                val intent: Intent = newIntentF(this)
                startActivityForResult(intent, CODE_FAVORITE)
            }
            R.id.invite_friend -> {
                val i = Intent(Intent.ACTION_SEND)
                i.setType("text/plain")
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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_INDEX, mCurrentIndex)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODE_FAVORITE) {
            data?.let {
                val item: FilmsItem = it.getSerializableExtra(EXTRA_FAVORITE_DELETE) as FilmsItem
                item.favorite = true
                for (i in App.items.indices) {
                    if (App.items[i].id == item.id) {
                        App.items[i]= item
                    }
                }
                Log.i(TAG, "favorite delete is:$item")
            }
            initRecycler()
        }
    }

    override fun onBackPressed() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.custom_dialog)
        val yesBtn = dialog.findViewById<Button>(R.id.button_yes)
        val noBtn = dialog.findViewById<Button>(R.id.button_no)
        yesBtn.setOnClickListener { super.onBackPressed() }
        noBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    fun clickExplicit(value: FilmsItem) {
        val intent: Intent = newIntent(this, value)
        startActivityForResult(intent, CODE)
    }

    private fun initRecycler() {
        val recycler = findViewById<RecyclerView>(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler.layoutManager = layoutManager
        recycler.adapter = FilmsAdapter(this,
            LayoutInflater.from(this),
            App.items,
            object : FilmsAdapter.OnFilmsClickListener {
                override fun onFilmsClick(filmsItem: FilmsItem, position: Int) {
                    clickExplicit(filmsItem)
                }

                override fun onFavouriteClick(filmsItem: FilmsItem, position: Int) {
                    if (filmsItem.favorite) {
                        App.items[position].favorite = false
                        recycler.adapter?.notifyItemChanged(position)
                        App.itemsFavorite.add(App.items[position])
                    } else {
                        App.items[position].favorite = true
                        recycler.adapter?.notifyItemChanged(position)
                        App.itemsFavorite.remove(App.items[position])
                    }
                }
            })

        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (layoutManager.findLastVisibleItemPosition() == App.items.size) {
                    repeat(4) {
                        App.items.add(
                            FilmsItem(
                                8,
                                R.string.name_film_shining,
                                R.string.description_shining,
                                R.drawable.shining,
                                true
                            )
                        )
                    }
                    recycler.adapter?.notifyItemRangeChanged(App.items.size - 4, App.items.size)
                }
            }
        })

        val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        itemDecoration.setDrawable(getDrawable(R.drawable.white_line))
        recycler.addItemDecoration(itemDecoration)
    }

    companion object {
        const val KEY_INDEX = "INDEX"
        const val CODE = 0
        const val CODE_FAVORITE = 1
        val TAG = MainActivity::class.java.simpleName
    }

    //region adapter and holder
    class FilmsItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTv: TextView = itemView.findViewById(R.id.titleTv)
        private val subtitleTv: TextView = itemView.findViewById(R.id.subtitleTv)
        private val imageFilm: ImageView = itemView.findViewById(R.id.image)
        private val imageFavourite: ImageView = itemView.findViewById(R.id.imageFavourite)
        var container: ConstraintLayout = itemView.findViewById(R.id.container)

        fun bind(item: FilmsItem) {
            titleTv.setText(item.title)
            subtitleTv.setText(item.description)
            imageFilm.setImageResource(item.image)
            if (item.favorite) {
                imageFavourite.setImageResource(R.drawable.ic_favorite_border_black_24dp)
            } else {
                imageFavourite.setImageResource(R.drawable.ic_favorite_red_24dp)
            }

        }
    }

    class FilmsAdapter(
        private val context: Context,
        private val inflater: LayoutInflater,
        private val items: List<FilmsItem>,
        private val listener: OnFilmsClickListener
    ) :
        RecyclerView.Adapter<FilmsItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmsItemViewHolder {
            return FilmsItemViewHolder(inflater.inflate(R.layout.item_news, parent, false))
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: FilmsItemViewHolder, position: Int) {
            holder.container.setAnimation(
                AnimationUtils.loadAnimation(
                    context,
                    R.anim.my_animation
                )
            )
            val item = items[position]
            holder.bind(item)
            holder.itemView.setOnClickListener { listener.onFilmsClick(item, position) }
            val imageFavourite: ImageView = holder.itemView.findViewById(R.id.imageFavourite)
            imageFavourite.setOnClickListener { listener.onFavouriteClick(item, position) }
        }

        interface OnFilmsClickListener {
            fun onFilmsClick(filmsItem: FilmsItem, position: Int)
            fun onFavouriteClick(filmsItem: FilmsItem, position: Int)
        }
    }
    //endregion
}
