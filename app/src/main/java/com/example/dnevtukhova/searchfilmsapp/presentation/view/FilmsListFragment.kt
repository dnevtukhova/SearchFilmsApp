package com.example.dnevtukhova.searchfilmsapp.presentation.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.dnevtukhova.searchfilmsapp.App
import com.example.dnevtukhova.searchfilmsapp.App.Companion.PAGE_NUMBER
import com.example.dnevtukhova.searchfilmsapp.R
import com.example.dnevtukhova.searchfilmsapp.data.FilmsItem
import com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel.FilmsListViewModel
import com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel.FilmsViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_film_list.*
import java.util.*

class FilmsListFragment : Fragment() {
    var listener: FilmsListListener? = null
    private lateinit var recycler: RecyclerView
    private lateinit var adapterFilms: FilmsAdapter
    private lateinit var filmsViewModel: FilmsListViewModel

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
        initRecycler(view, progressbar)
        val myViewModelFactory = FilmsViewModelFactory(App.instance.filmsInteractor)

        filmsViewModel = ViewModelProvider(
            requireActivity(),
            myViewModelFactory
        ).get(FilmsListViewModel::class.java)
        filmsViewModel.films?.observe(
            this.viewLifecycleOwner,
            Observer<List<FilmsItem>> { films ->
                filmsViewModel.initSharedPref()
                adapterFilms.setItems(films)
            })
        filmsViewModel.error.observe(
            this.viewLifecycleOwner,
            Observer<String> { error ->
                if (progressbar != null) {
                    progressbar.visibility = View.GONE
                }
                addSnackBar(error)
            })
        swipeRefreshLayout.setOnRefreshListener {
            if (progressbar != null) {
                progressbar.visibility = View.GONE
            }
            filmsViewModel.mSettings.edit {
                putInt(PAGE_NUMBER, 1)
            }.apply { }
            filmsViewModel.removeAllFilms()
            filmsViewModel.refreshAllFilms()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    private fun initRecycler(view: View, progressBar: ProgressBar) {
        recycler = view.findViewById(R.id.recyclerViewFragment)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recycler.layoutManager = layoutManager
        adapterFilms =
            FilmsAdapter(
                progressBar,
                context!!,
                LayoutInflater.from(context),
                object :
                    FilmsAdapter.OnFilmsClickListener {
                    override fun onFilmsClick(filmsItem: FilmsItem, position: Int) {
                        filmsViewModel.selectFilm(filmsItem)
                        listener?.onFilmsSelected(filmsItem)
                    }

                    override fun onFavouriteClick(filmsItem: FilmsItem, position: Int) {
                        filmsViewModel.selectFavorite(filmsItem)
                        adapterFilms.notifyItemChanged(position)
                    }
                })

        recycler.adapter = adapterFilms

        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (layoutManager.findLastVisibleItemPosition() == layoutManager.itemCount - 2) {
                    val page = filmsViewModel.mSettings.getInt(PAGE_NUMBER, 0)
                    Log.d("page", "$page")
                    val page2 = page + 1
                    filmsViewModel.mSettings.edit { putInt(PAGE_NUMBER, page2) }.apply {
                    }
                    Log.d("page2", "$page2")
                    filmsViewModel.refreshAllFilms()
                    recycler.post {
                        adapterFilms.notifyItemRangeChanged(
                            layoutManager.itemCount,
                            20
                        )
                    }
                }
            }
        })

        val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        getDrawable(
            context!!,
            R.drawable.white_line
        )?.let { itemDecoration.setDrawable(it) }
        recycler.addItemDecoration(itemDecoration)
    }

    private fun addSnackBar(error: String) {
        // Создание экземпляра Snackbar
        val snackBar =
            Snackbar.make(view!!, "Ошибка $error", Snackbar.LENGTH_LONG)
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
        snackBar.setAction("Обновить") {
            filmsViewModel.refreshAllFilms()
        }
            .show()
    }

    interface FilmsListListener {
        fun onFilmsSelected(filmsItem: FilmsItem)
    }

    //region adapter and holder
    class FilmsItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTv: TextView = itemView.findViewById(R.id.titleTv)
        private val subtitleTv: TextView = itemView.findViewById(R.id.descriptionFilm)
        private val imageFilm: ImageView = itemView.findViewById(R.id.image)
        private val imageFavourite: ImageView = itemView.findViewById(R.id.imageFavourite)
        var container: ConstraintLayout = itemView.findViewById(R.id.container)

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

            if (item.favorite) {
                imageFavourite.setImageResource(R.drawable.ic_favorite_border_black_24dp)
            } else {
                imageFavourite.setImageResource(R.drawable.ic_favorite_red_24dp)
            }

        }
    }

    class FilmsAdapter(
        private val progressBar: ProgressBar,
        private val context: Context,
        private val inflater: LayoutInflater,
        private val listener: OnFilmsClickListener
    ) :
        RecyclerView.Adapter<FilmsItemViewHolder>() {
        private val items = ArrayList<FilmsItem>()

        fun setItems(films: List<FilmsItem>) {
            items.clear()
            items.addAll(films)
            if (progressBar != null) {
                progressBar.visibility = View.GONE
            }
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmsItemViewHolder {
            return FilmsItemViewHolder(
                inflater.inflate(
                    R.layout.item_news,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: FilmsItemViewHolder, position: Int) {
            holder.container.animation =
                AnimationUtils.loadAnimation(
                    context,
                    R.anim.my_animation
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
        const val TAG = "FilmsListFragment"
        const val PICTURE = "https://image.tmdb.org/t/p/w500/"
    }
}