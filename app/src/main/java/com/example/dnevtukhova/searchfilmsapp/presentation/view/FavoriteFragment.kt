package com.example.dnevtukhova.searchfilmsapp.presentation.view

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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.dnevtukhova.searchfilmsapp.App
import com.example.dnevtukhova.searchfilmsapp.R
import com.example.dnevtukhova.searchfilmsapp.data.FavoriteItem
import com.example.dnevtukhova.searchfilmsapp.data.FilmsItem
import com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel.FilmsListViewModel
import com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel.FilmsViewModelFactory
import com.google.android.material.snackbar.Snackbar
import java.util.*

class FavoriteFragment : Fragment() {
    var listener: FilmsFavoriteAdapter.OnFavoriteFilmsClickListener? = null
    private lateinit var favoriteViewModel: FilmsListViewModel
    private lateinit var adapterFavoriteFilms: FilmsFavoriteAdapter

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
        val myViewModelFactory = FilmsViewModelFactory(App.instance.filmsInteractor)
        favoriteViewModel = ViewModelProvider(
            requireActivity(),
            myViewModelFactory
        ).get(FilmsListViewModel::class.java)
        favoriteViewModel.favoriteFilms?.observe(
            this.viewLifecycleOwner,
            Observer<List<FavoriteItem>> { films -> adapterFavoriteFilms.setItems(films) })
        //   favoriteViewModel.getFavorite()
    }

    private fun initRecycler(view: View) {
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerViewFavorite)
        adapterFavoriteFilms =
            FilmsFavoriteAdapter(
                context!!,
                LayoutInflater.from(context),
                //по долгому клику удаление элемента
                object :
                    FilmsFavoriteAdapter.OnFavoriteFilmsClickListener {
                    override fun onFavoriteFilmsLongClick(
                        filmsItem: FavoriteItem,
                        position: Int
                    ): Boolean {
                        favoriteViewModel.removeFromFavorite(filmsItem, true)
                        addSnackBar(filmsItem)
                        adapterFavoriteFilms.notifyDataSetChanged()
                        return true
                    }

                    override fun onFavoriteFilmsFClick(filmsItem: FavoriteItem, position: Int) {
                        val f = FilmsItem(
                            filmsItem.id,
                            filmsItem.title,
                            filmsItem.description,
                            filmsItem.image,
                            filmsItem.favorite
                        )
                        favoriteViewModel.selectFilm(f)
                        listener?.onFavoriteFilmsFClick(filmsItem, position)
                    }
                })
        recycler.adapter = adapterFavoriteFilms
        val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        AppCompatResources.getDrawable(
            context!!,
            R.drawable.white_line
        )
            ?.let { itemDecoration.setDrawable(it) }
        recycler.addItemDecoration(itemDecoration)
    }

    private fun addSnackBar(filmsItem: FavoriteItem) {
        // Создание экземпляра Snackbar
        val snackBar =
            Snackbar.make(view!!, "Удален фильм '${filmsItem.title}'", Snackbar.LENGTH_LONG)
        // Устанавливаем цвет текста кнопки действий
        snackBar.setActionTextColor(
            ContextCompat.getColor(
                context!!,
                R.color.colorRed
            )
        )
        // Получение snackbar
        val snackBarView = snackBar.view
        // Изменение цвета текста
        val snackbarTextId = com.google.android.material.R.id.snackbar_text
        val textView = snackBarView.findViewById<View>(snackbarTextId) as TextView
        textView.setTextColor(ContextCompat.getColor(context!!, android.R.color.white))
        // Изменение цвета фона
        snackBarView.setBackgroundColor(Color.GRAY)
        snackBar.setAnchorView(R.id.bottomNavigation)
        snackBar.setAction("Отменить") {
            favoriteViewModel.addToFavorite(filmsItem, false)
            //   favoriteViewModel.getFavorite()
            adapterFavoriteFilms.notifyDataSetChanged()
        }
            .show()
    }

    //region adapter and holder
    class FilmsFavouriteItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTv: TextView = itemView.findViewById(R.id.titleTv)
        private val subtitleTv: TextView = itemView.findViewById(R.id.descriptionFilm)
        private val imageFilm: ImageView = itemView.findViewById(R.id.image)
        var container: ConstraintLayout = itemView.findViewById(R.id.container)

        fun bind(item: FavoriteItem) {
            titleTv.text = item.title
            subtitleTv.text = item.description
            Glide.with(imageFilm.context)
                .load(FilmsListFragment.PICTURE + item.image)
                .placeholder(R.drawable.ic_photo_black_24dp)
                .error(R.drawable.ic_error_outline_black_24dp)
                .centerCrop()
                .transform(RoundedCorners(30))
                .into(imageFilm)
        }
    }

    class FilmsFavoriteAdapter(
        private val context: Context,
        private val inflater: LayoutInflater,
        private val listener: OnFavoriteFilmsClickListener
    ) :
        RecyclerView.Adapter<FilmsFavouriteItemViewHolder>() {
        private val items = ArrayList<FavoriteItem>()

        fun setItems(films: List<FavoriteItem>) {
            items.clear()
            items.addAll(films)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): FilmsFavouriteItemViewHolder {
            return FilmsFavouriteItemViewHolder(
                inflater.inflate(
                    R.layout.item_news,
                    parent,
                    false
                )
            )
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
            fun onFavoriteFilmsLongClick(filmsItem: FavoriteItem, position: Int): Boolean
            fun onFavoriteFilmsFClick(filmsItem: FavoriteItem, position: Int)
        }
    }
    //endregion
}