package com.example.dnevtukhova.searchfilmsapp

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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dnevtukhova.searchfilmsapp.App.Companion.itemsFilms
import com.example.dnevtukhova.searchfilmsapp.Retrofit.PopularFilms
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FilmsListFragment : Fragment() {
    var listener: FilmsListListener? = null
    lateinit var recycler: RecyclerView
    private var pageNumber: Int = 1
    lateinit var adapterFilms: FilmsAdapter
    lateinit var progressBar: ProgressBar

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
        progressBar = view.findViewById(R.id.progressbar)
        progressBar.visibility=View.VISIBLE
        recycler.visibility = View.INVISIBLE


        initRecycler(view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    private fun initRecycler(view: View) {
        recycler = view.findViewById(R.id.recyclerViewFragment)
      //recycler.visibility=View.GONE
        progressBar.visibility=View.VISIBLE
        recycler.visibility = View.INVISIBLE

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recycler.layoutManager = layoutManager
        adapterFilms = FilmsAdapter(

            context!!,
            LayoutInflater.from(context),
            itemsFilms,
            object : FilmsAdapter.OnFilmsClickListener {
                override fun onFilmsClick(filmsItem: FilmsItem, position: Int) {
                    listener?.onFilmsSelected(filmsItem)
                }

                override fun onFavouriteClick(filmsItem: FilmsItem, position: Int) {
                    if (filmsItem.favorite) {
                        itemsFilms[position].favorite = false
                        adapterFilms?.notifyItemChanged(position)
                        App.itemsFavorite.add(itemsFilms[position])
                    } else {
                        itemsFilms[position].favorite = true
                        adapterFilms?.notifyItemChanged(position)
                        App.itemsFavorite.remove(itemsFilms[position])
                    }
                }
            })

        recycler.adapter = adapterFilms
loadData()
//        recycler.visibility=View.VISIBLE
//        adapterFilms.notifyDataSetChanged()

        recycler.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (layoutManager.findLastVisibleItemPosition() == itemsFilms.size-1) {
                    pageNumber += 1
                    App.instance.api.getFilms(API_KEY, LANGUAGE, pageNumber)
                        .enqueue(object : Callback<PopularFilms> {
                            override fun onFailure(call: Call<PopularFilms>, t: Throwable) {
                                Log.d(TAG, " !!! произошла ошибка $t")
                            }

                            override fun onResponse(
                                call: Call<PopularFilms>,
                                response: Response<PopularFilms>
                            ) {

                              //  itemsFilms.clear()
                                if (response.isSuccessful) {
                                    Log.d(
                                        TAG,
                                        response.toString() + " " + response.code() + " " + response.body()
                                    )
                                    response.body()?.results
                                        ?.forEach {
                                            itemsFilms.add(
                                                FilmsItem(
                                                    it.id,
                                                    it.title,
                                                    it.description,
                                                    it.image,
                                                    true
                                                )
                                            )
                                            Log.d(TAG, it.title)
                                        }
//                        for (item in response.body()?.results!!) {
//                            item.favorite = true
//                            itemsFilms.add(item)
//                            Log.d(TAG, item.title)
//                            println(item.image)
//                        }

                                } else {
                                    Log.d(
                                        TAG,
                                        "!!!! response.code " + response.code() + " response.body " + response.body()
                                    )
                                }
                                adapterFilms.notifyItemRangeChanged(itemsFilms.size - 20, 20)
                            }
                        })
                    
                }
            }
        })

        val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        getDrawable(context!!, R.drawable.white_line)?.let { itemDecoration.setDrawable(it) }
        recycler.addItemDecoration(itemDecoration)


    }

    private fun loadData () {
        App.instance.api.getFilms(API_KEY, LANGUAGE, pageNumber)
            .enqueue(object : Callback<PopularFilms> {
                override fun onFailure(call: Call<PopularFilms>, t: Throwable) {
                    Log.d(TAG, " !!! произошла ошибка $t")
                }

                override fun onResponse(
                    call: Call<PopularFilms>,
                    response: Response<PopularFilms>
                ) {

                    itemsFilms.clear()
                    if (response.isSuccessful) {
                        Log.d(
                            TAG,
                            response.toString() + " " + response.code() + " " + response.body()
                        )
                        response.body()?.results
                            ?.forEach {
                                itemsFilms.add(
                                    FilmsItem(
                                        it.id,
                                        it.title,
                                        it.description,
                                        it.image,
                                        true
                                    )
                                )
                                Log.d(TAG, it.title)
                            }
//                        for (item in response.body()?.results!!) {
//                            item.favorite = true
//                            itemsFilms.add(item)
//                            Log.d(TAG, item.title)
//                            println(item.image)
//                        }

                    } else {
                        Log.d(
                            TAG,
                            "!!!! response.code " + response.code() + " response.body " + response.body()
                        )
                    }
                    //  progressBar.visibility = View.INVISIBLE
                    progressBar.visibility= View.GONE
                   // recycler.visibility = View.VISIBLE
                    adapterFilms.notifyDataSetChanged()
                    recycler.visibility = View.VISIBLE
                }
            })
    }

    interface FilmsListListener {
        fun onFilmsSelected(filmsItem: FilmsItem)
        //  fun updateAdapter(adapter: RecyclerView)
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
            Glide.with(imageFilm.context)
                .load(PICTURE + item.image)
                .into(imageFilm)

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
           // progressBar.visibility = false
         //   progressBar.visibility = View.INVISIBLE
          //  progressBar.visibility = View.GONE


        }

        interface OnFilmsClickListener {
            fun onFilmsClick(filmsItem: FilmsItem, position: Int)
            fun onFavouriteClick(filmsItem: FilmsItem, position: Int)
        }
    }
    //endregion

    companion object {
        const val TAG = "FilmsListFragment"
        const val API_KEY = "79c459d10744203ee914c139f789d1e8"
        const val LANGUAGE = "ru-RUS"
        const val PICTURE = "https://image.tmdb.org/t/p/w500/"
    }
}