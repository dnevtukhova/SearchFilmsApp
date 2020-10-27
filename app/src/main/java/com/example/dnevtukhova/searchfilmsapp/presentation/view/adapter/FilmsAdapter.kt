package com.example.dnevtukhova.searchfilmsapp.presentation.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.dnevtukhova.searchfilmsapp.R
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants
import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmsItem
import kotlinx.android.synthetic.main.item_film.view.*
import testing.OpenForTesting
import java.util.*

@OpenForTesting
class FilmsAdapter(
    private val inflater: LayoutInflater,
    private val listener: OnFilmsClickListener
) : RecyclerView.Adapter<FilmsItemViewHolder>() {
    private val items = ArrayList<FilmsItem>()

    fun setItems(films: List<FilmsItem>) {
        items.clear()
        items.addAll(films)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmsItemViewHolder {
        return FilmsItemViewHolder(
            inflater.inflate(
                R.layout.item_film,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: FilmsItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.itemView.setOnClickListener { listener.onFilmsClick(item, position) }
        val imageFavourite: ImageView = holder.itemView.findViewById(R.id.imageFavourite)
        imageFavourite.setOnClickListener { listener.onFavouriteClick(item, position) }
        val imageWatchLater: ImageView = holder.itemView.findViewById(R.id.watchLater)
        imageWatchLater.setOnClickListener { listener.onWatchLaterClick(item, position) }
    }

    interface OnFilmsClickListener {
        fun onFilmsClick(filmsItem: FilmsItem, position: Int)
        fun onFavouriteClick(filmsItem: FilmsItem, position: Int)
        fun onWatchLaterClick(filmsItem: FilmsItem, position: Int)
    }
}

class FilmsItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val titleTv: TextView = itemView.findViewById(R.id.titleTv)
    private val subtitleTv: TextView = itemView.findViewById(R.id.descriptionFilm)
    private val imageFilm: ImageView = itemView.findViewById(R.id.image)
    private val imageFavourite: ImageView = itemView.findViewById(R.id.imageFavourite)
    private val imageWatchLater: ImageView = itemView.findViewById(R.id.watchLater)

    fun bind(item: FilmsItem) {
        titleTv.text = item.title
        subtitleTv.text = item.description
        Glide.with(imageFilm.context)
            .load(NetworkConstants.PICTURE + item.image)
            .placeholder(R.drawable.ic_photo_black_24dp)
            .error(R.drawable.ic_error_outline_black_24dp)
            .centerCrop()
            .transform(RoundedCorners(30))
            .into(imageFilm)

        if (item.favorite) {
            imageFavourite.setImageResource(R.drawable.ic_favorite_border_black_24dp)
        } else {
            imageFavourite.setImageResource(R.drawable.ic_favorite_red_24dp)
        }
        if (item.watchLater) {
            imageWatchLater.setImageResource(R.drawable.ic_notifications_none_black_24dp)
        } else {
            imageWatchLater.setImageResource(R.drawable.ic_notifications_burgundy_24dp)
        }
        itemView.container.animation =
            AnimationUtils.loadAnimation(
                itemView.context,
                R.anim.my_animation
            )
    }
}