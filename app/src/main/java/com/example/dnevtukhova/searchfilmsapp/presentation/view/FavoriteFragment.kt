package com.example.dnevtukhova.searchfilmsapp.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.dnevtukhova.searchfilmsapp.R
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants.PICTURE
import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmsItem
import com.example.dnevtukhova.searchfilmsapp.di.Injectable
import com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel.DetailFragmentViewModel
import com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel.FavoriteFragmentViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.item_film.view.*
import java.util.*
import javax.inject.Inject

class FavoriteFragment : Fragment(), Injectable {
    var listener: FilmsFavoriteAdapter.OnFavoriteFilmsClickListener? = null

    @Inject
    lateinit var filmsViewModelFactory: ViewModelProvider.Factory
    val favoriteViewModel: FavoriteFragmentViewModel by viewModels {
        filmsViewModelFactory
    }
    lateinit var detailViewModel: DetailFragmentViewModel
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
        favoriteViewModel.favoriteFilms?.observe(
            this.viewLifecycleOwner,
            { films ->
                adapterFavoriteFilms.setItems(films)
            })

        detailViewModel = ViewModelProvider(
            requireActivity(),
            filmsViewModelFactory
        ).get(DetailFragmentViewModel::class.java)
    }

    private fun initRecycler(view: View) {
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerViewFavorite)
        adapterFavoriteFilms =
            FilmsFavoriteAdapter(
                LayoutInflater.from(context),
                //по долгому клику удаление элемента
                object :
                    FilmsFavoriteAdapter.OnFavoriteFilmsClickListener {
                    override fun onFavoriteFilmsLongClick(
                        filmsItem: FilmsItem,
                        position: Int
                    ): Boolean {
                        favoriteViewModel.changeFavorite(filmsItem, true)
                        requireView().showSnackbar(
                            "${requireContext().getString(R.string.deleteFilm)} ${filmsItem.title}",
                            Snackbar.LENGTH_LONG,
                            requireContext().getString(R.string.cancelText)
                        ) {
                            favoriteViewModel.changeFavorite(filmsItem, false)
                            adapterFavoriteFilms.notifyItemChanged(position)
                        }
                        adapterFavoriteFilms.notifyItemChanged(position)
                        return true
                    }

                    override fun onFavoriteFilmsFClick(filmsItem: FilmsItem, position: Int) {
                        detailViewModel.selectFilm(filmsItem)
                        listener?.onFavoriteFilmsFClick(filmsItem, position)
                        adapterFavoriteFilms.notifyItemChanged(position)
                    }
                })
        recycler.adapter = adapterFavoriteFilms
        val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        AppCompatResources.getDrawable(
            requireContext(),
            R.drawable.white_line
        )
            ?.let { itemDecoration.setDrawable(it) }
        recycler.addItemDecoration(itemDecoration)
        adapterFavoriteFilms.notifyDataSetChanged()
    }

    //region adapter and holder
    class FilmsFavouriteItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTv: TextView = itemView.findViewById(R.id.titleTv)
        private val subtitleTv: TextView = itemView.findViewById(R.id.descriptionFilm)
        private val imageFilm: ImageView = itemView.findViewById(R.id.image)

        fun bind(item: FilmsItem) {
            titleTv.text = item.title
            subtitleTv.text = item.description
            Glide.with(imageFilm.context)
                .load(PICTURE + item.image)
                .placeholder(R.drawable.ic_photo_black_24dp)
                .error(R.drawable.ic_error_outline_black_24dp)
                .centerCrop()
                .transform(RoundedCorners(30))
                .into(imageFilm)
            itemView.container.animation =
                AnimationUtils.loadAnimation(
                    itemView.context,
                    R.anim.my_animation
                )
        }
    }

    class FilmsFavoriteAdapter(
        private val inflater: LayoutInflater,
        private val listener: OnFavoriteFilmsClickListener
    ) : RecyclerView.Adapter<FilmsFavouriteItemViewHolder>() {
        private val items = ArrayList<FilmsItem>()

        fun setItems(films: List<FilmsItem>) {
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
                    R.layout.item_film,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: FilmsFavouriteItemViewHolder, position: Int) {
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