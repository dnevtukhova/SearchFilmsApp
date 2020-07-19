package com.example.dnevtukhova.searchfilmsapp.presentation.view

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
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

class FilmsListFragment : Fragment(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    var listener: FilmsListListener? = null
    private lateinit var recycler: RecyclerView
    private lateinit var adapterFilms: FilmsAdapter
    private lateinit var filmsViewModel: FilmsListViewModel
    private val calendar: Calendar = Calendar.getInstance()
    private var pIntentOnce: PendingIntent? = null
    private var am: AlarmManager? = null
    private var intent: Intent? = null
    private var dateNotification: Long? = null
    private var myFilmsItem: FilmsItem? = null
    private var myPosition: Int? = null

    companion object {
        const val TAG = "FilmsListFragment"
        const val PICTURE = "https://image.tmdb.org/t/p/w500/"
        const val CHANNEL_ID = "channel"
        const val FILMS_ITEM_EXTRA = "filmsItemExtra"
        const val BUNDLE = "bundle"
    }

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
        createNotificationChannel()
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
        am = getSystemService(requireContext(), AlarmManager::class.java)
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
                        Log.d(TAG, "${filmsItem.id} ${filmsItem.title} ${filmsItem.description} ${filmsItem.image}")
                        filmsViewModel.selectFavorite(filmsItem)
                        adapterFilms.notifyItemChanged(position)
                    }

                    @RequiresApi(Build.VERSION_CODES.M)
                    override fun onWatchLaterClick(filmsItem: FilmsItem, position: Int) {
                        intent = createIntent(filmsItem)
                        if (filmsItem.watchLater) {
                            myFilmsItem = filmsItem
                            myPosition = position
                            selectDateAndTime()
                        } else {
                            filmsViewModel.selectWatchLater(
                                filmsItem,
                                Calendar.getInstance().timeInMillis
                            )
                            if (pIntentOnce != null) {
                                pIntentOnce = PendingIntent.getBroadcast(
                                    requireContext(),
                                    0,
                                    intent,
                                    PendingIntent.FLAG_CANCEL_CURRENT
                                )
                                am?.setExact(
                                    AlarmManager.RTC_WAKEUP,
                                    dateNotification!!,
                                    pIntentOnce
                                )
                            }
                        }
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
        private val imageWatchLater: ImageView = itemView.findViewById(R.id.watchLater)
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
            if (item.watchLater) {
                imageWatchLater.setImageResource(R.drawable.ic_notifications_none_black_24dp)
            } else {
                imageWatchLater.setImageResource(R.drawable.ic_notifications_burgundy_24dp)
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
            progressBar.visibility = View.GONE
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
            val imageWatchLater: ImageView = holder.itemView.findViewById(R.id.watchLater)
            imageWatchLater.setOnClickListener { listener.onWatchLaterClick(item, position) }
        }

        interface OnFilmsClickListener {
            fun onFilmsClick(filmsItem: FilmsItem, position: Int)
            fun onFavouriteClick(filmsItem: FilmsItem, position: Int)
            fun onWatchLaterClick(filmsItem: FilmsItem, position: Int)
        }
    }
    //endregion

    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.M)
    fun selectDateAndTime() {
        val datePickerDialog = DatePickerDialog(
            context!!,
            this,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
        val millis: Long = calendar.timeInMillis
        Log.d(TAG, "дата $millis")
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        calendar.set(Calendar.YEAR, p1)
        calendar.set(Calendar.MONTH, p2)
        calendar.set(Calendar.DAY_OF_MONTH, p3)
        val timePickerDialog = TimePickerDialog(
            context, this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        calendar.set(Calendar.HOUR_OF_DAY, p1)
        calendar.set(Calendar.MINUTE, p2)
        setAlarm()
    }

    private fun setAlarm() {
        Log.d(TAG, "setAlarm")
        dateNotification = calendar.timeInMillis
        filmsViewModel.selectWatchLater(myFilmsItem!!, dateNotification!!)
        adapterFilms.notifyItemChanged(myPosition!!)
        pIntentOnce =
            PendingIntent.getBroadcast(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        am?.setExact(
            AlarmManager.RTC_WAKEUP,
            dateNotification!!,
            pIntentOnce
        )
    }

    fun createIntent(filmsItem: FilmsItem): Intent {
        val intent = Intent("${filmsItem.id}", null, context, Receiver::class.java)
        val bundle = Bundle()
        bundle.putParcelable(FILMS_ITEM_EXTRA, filmsItem)
        intent.putExtra(BUNDLE, bundle)
        return intent
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channelName)
            val description = getString(R.string.channelDescription)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description

            val notificationManager = activity!!.getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }

}