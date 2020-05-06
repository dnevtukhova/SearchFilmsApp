package com.example.dnevtukhova.searchfilmsapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_film_list.*
import java.lang.Math.random

class FilmsListFragment : Fragment() {
    var listener: FilmsListListener? = null
    lateinit var recycler: RecyclerView

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_film_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler(view)
        onClickFloatingActionButton()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    private fun initRecycler(view: View) {
        recycler = view.findViewById(R.id.recyclerViewFragment)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recycler.layoutManager = layoutManager
        recycler.adapter = FilmsAdapter(context!!,
            LayoutInflater.from(context),
            App.items,
            object : FilmsAdapter.OnFilmsClickListener {
                override fun onFilmsClick(filmsItem: FilmsItem, position: Int) {
                    listener?.onFilmsSelected(filmsItem)
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

        val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        getDrawable(context!!, R.drawable.white_line)?.let { itemDecoration.setDrawable(it) }
        recycler.addItemDecoration(itemDecoration)
    }

    interface FilmsListListener {
        fun onFilmsSelected(filmsItem: FilmsItem)
        //  fun updateAdapter(adapter: RecyclerView)
    }

    private fun onClickFloatingActionButton () {
        floatingActionButton.setOnClickListener {
            val filmsItem = FilmsItem(random().toInt(),R.string.name_film_cloud_atlas,
                R.string.description_cloud_atlas,
                R.drawable.cloud_atlas,
                true)
            App.items.add(filmsItem)
            recycler.adapter?.notifyDataSetChanged()
            addSnackBar(filmsItem, recycler)
        }
    }

    private fun addSnackBar(filmsItem: FilmsItem, recycler: RecyclerView) {
        // Создание экземпляра Snackbar
        val snackBar =
            Snackbar.make(view!!, "Фильм добавлен в список", Snackbar.LENGTH_LONG)
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
            App.items.remove(filmsItem)
            recycler.adapter?.notifyDataSetChanged()
        }
            .show()
    }

    //region adapter and holder
    class FilmsItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTv: TextView = itemView.findViewById(R.id.titleTv)
        private val subtitleTv: TextView = itemView.findViewById(R.id.descriptionFilm)
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

    companion object {
        const val TAG = "NewsListFragment"
    }
}