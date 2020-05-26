package com.example.dnevtukhova.searchfilmsapp

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar

class FavoriteFragment : Fragment() {
    var listener: FilmsFavoriteAdapter.OnFavoriteFilmsClickListener? = null

    companion object {
        const val TAG = "FavoriteFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler(view)
    }

    private fun initRecycler(view: View) {
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerViewFavorite)
        recycler.adapter = FilmsFavoriteAdapter(context!!,
            LayoutInflater.from(context),
            App.itemsFavorite,
            //по долгому клику удаление элемента
            object : FilmsFavoriteAdapter.OnFavoriteFilmsClickListener {
                override fun onFavoriteFilmsLongClick(
                    filmsItem: FilmsItem,
                    position: Int
                ): Boolean {
                    App.itemsFavorite.remove(filmsItem)
                    filmsItem.favorite = true
                    for (i in App.itemsFilms.indices) {
                        if (App.itemsFilms[i].id == filmsItem.id) {
                            App.itemsFilms[i] = filmsItem
                        }
                    }
                    addSnackBar(filmsItem, position, recycler)
                    recycler.adapter?.notifyDataSetChanged()
                    return true
                }

                override fun onFavoriteFilmsFClick(filmsItem: FilmsItem, position: Int) {
                    listener?.onFavoriteFilmsFClick(filmsItem, position)
                }
            })

        val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        AppCompatResources.getDrawable(context!!, R.drawable.white_line)
            ?.let { itemDecoration.setDrawable(it) }
        recycler.addItemDecoration(itemDecoration)
    }

    private fun addSnackBar(filmsItem: FilmsItem, position: Int, recycler: RecyclerView) {
        // Создание экземпляра Snackbar
        val snackBar =
            Snackbar.make(view!!, "Удален элемент № $position", Snackbar.LENGTH_LONG)
        // Устанавливаем цвет текста кнопки действий
        snackBar.setActionTextColor(ContextCompat.getColor(context!!, R.color.colorRed))
        // Получение snackbar view
        val snackBarView = snackBar.view
        // Изменение цвета текста
        val snackbarTextId = com.google.android.material.R.id.snackbar_text
        val textView = snackBarView.findViewById<View>(snackbarTextId) as TextView
        textView.setTextColor(ContextCompat.getColor(context!!, android.R.color.white))
        // Изменение цвета фона
        snackBarView.setBackgroundColor(Color.GRAY)
        snackBar.setAnchorView(R.id.bottomNavigation)
        snackBar.setAction("Отменить") {
            App.itemsFavorite.add(filmsItem)
            filmsItem.favorite = false
            for (i in App.itemsFilms.indices) {
                if (App.itemsFilms[i].id == filmsItem.id) {
                    App.itemsFilms[i] = filmsItem
                }
            }
            recycler.adapter?.notifyDataSetChanged()
        }
            .show()
    }

    //region adapter and holder
    class FilmsFavouriteItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTv: TextView = itemView.findViewById(R.id.titleTv)
        private val subtitleTv: TextView = itemView.findViewById(R.id.descriptionFilm)
        private val imageFilm: ImageView = itemView.findViewById(R.id.image)
        var container: ConstraintLayout = itemView.findViewById(R.id.container)

        fun bind(item: FilmsItem) {
            titleTv.text = item.title
            subtitleTv.text = item.description
            Glide.with(imageFilm.context)
                .load(FilmsListFragment.PICTURE + item.image)
                .into(imageFilm)
        }
    }

    class FilmsFavoriteAdapter(
        private val context: Context,
        private val inflater: LayoutInflater,
        private val items: List<FilmsItem>,
        private val listener: OnFavoriteFilmsClickListener
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
            holder.container.animation =
                AnimationUtils.loadAnimation(
                    context,
                    R.anim.my_animation
                )

            val item = items[position]
            holder.bind(item)
            holder.itemView.setOnLongClickListener {
                listener.onFavoriteFilmsLongClick(
                    item,
                    position
                )
            }
            holder.itemView.setOnClickListener { listener.onFavoriteFilmsFClick(item, position) }
        }

        interface OnFavoriteFilmsClickListener {
            fun onFavoriteFilmsLongClick(filmsItem: FilmsItem, position: Int): Boolean
            fun onFavoriteFilmsFClick(filmsItem: FilmsItem, position: Int)
        }
    }
    //endregion
}