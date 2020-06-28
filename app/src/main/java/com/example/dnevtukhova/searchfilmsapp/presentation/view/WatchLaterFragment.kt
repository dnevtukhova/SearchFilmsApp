package com.example.dnevtukhova.searchfilmsapp.presentation.view

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import android.widget.TimePicker
import androidx.annotation.RequiresApi
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
import com.example.dnevtukhova.searchfilmsapp.data.FilmsItem
import com.example.dnevtukhova.searchfilmsapp.data.WatchLaterItem
import com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel.FilmsListViewModel
import com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel.FilmsViewModelFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class WatchLaterFragment : Fragment(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    private lateinit var adapterWatchLaterFilms: FilmsWatchLaterAdapter
    private lateinit var watchLaterViewModel: FilmsListViewModel
    private val calendar: Calendar = Calendar.getInstance()
    private var watchLaterItem: WatchLaterItem? = null
    private var myPosition: Int? = null
    private var intent: Intent? = null

    companion object {
        const val TAG = "WatchLaterFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_watch_later, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler(view)
        val myViewModelFactory = FilmsViewModelFactory(App.instance.filmsInteractor)
        watchLaterViewModel = ViewModelProvider(
            requireActivity(),
            myViewModelFactory
        ).get(FilmsListViewModel::class.java)
        watchLaterViewModel.watchLaterFilms?.observe(
            this.viewLifecycleOwner,
            Observer<List<WatchLaterItem>> { films -> adapterWatchLaterFilms.setItems(films) })
    }

    private fun initRecycler(view: View) {
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerViewWatchLater)
        adapterWatchLaterFilms =
            FilmsWatchLaterAdapter(
                context!!,
                LayoutInflater.from(context),
                //по долгому клику удаление элемента
                object :
                    FilmsWatchLaterAdapter.OnWatchLaterFilmsClickListener {
                    @RequiresApi(Build.VERSION_CODES.M)
                    override fun onWatchLaterFilmsFClick(filmsItem: WatchLaterItem, position: Int) {
                        watchLaterItem = filmsItem
                        myPosition = position
                        intent = createIntent(filmsItem)
                        selectDateAndTime()
                    }
                }
            )
        recycler.adapter = adapterWatchLaterFilms
        val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        AppCompatResources.getDrawable(
            context!!,
            R.drawable.white_line
        )
            ?.let { itemDecoration.setDrawable(it) }
        recycler.addItemDecoration(itemDecoration)
    }

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
        Log.d(FilmsListFragment.TAG, "дата $millis")
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        calendar.set(Calendar.YEAR, p1)
        calendar.set(Calendar.MONTH, p2)
        calendar.set(Calendar.DAY_OF_MONTH, p3)
        val timePickerDialog = TimePickerDialog(
            context, this, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        calendar.set(Calendar.HOUR, p1)
        calendar.set(Calendar.MINUTE, p2)
        setAlarm()
    }

    private fun setAlarm() {
        Log.d(FilmsListFragment.TAG, "setAlarm")
        val dateNotification = calendar.timeInMillis
        watchLaterItem!!.dateToWatch = calendar.timeInMillis
        watchLaterViewModel.setDateToWatch(watchLaterItem!!)
        adapterWatchLaterFilms.notifyItemChanged(myPosition!!)
        Log.d(TAG, intent.toString())
        val pIntentOnce =
            PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val am: AlarmManager? =
            ContextCompat.getSystemService(requireContext(), AlarmManager::class.java)
        am?.setExact(
            AlarmManager.RTC_WAKEUP,
            dateNotification,
            pIntentOnce
        )
    }

    fun createIntent(filmsItem: WatchLaterItem): Intent {
        val filmsItem1 = FilmsItem(
            filmsItem.id,
            filmsItem.title,
            filmsItem.description,
            filmsItem.image,
            filmsItem.favorite,
            filmsItem.watchLater
        )
        val intent = Intent("${filmsItem.id}", null, context, Receiver::class.java)
        val bundle = Bundle()
        bundle.putParcelable(FilmsListFragment.FILMS_ITEM_EXTRA, filmsItem1)
        intent.putExtra(FilmsListFragment.BUNDLE, bundle)
        return intent
    }

    //region adapter and holder
    class FilmsWatchLaterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTv: TextView = itemView.findViewById(R.id.titleTv)
        private val date: TextView = itemView.findViewById(R.id.descriptionFilm)
        private val imageFilm: ImageView = itemView.findViewById(R.id.image)
        var container: ConstraintLayout = itemView.findViewById(R.id.container)

        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        fun bind(item: WatchLaterItem) {
            titleTv.text = item.title
            val locale = Locale("RU")
            val simpleDateFormat = SimpleDateFormat("EEEE dd MMMM yyyy HH:mm", locale)
            val dateToWatch = simpleDateFormat.format(Date(item.dateToWatch)).toString()
            date.text = "Напомнить посмотреть фильм: $dateToWatch"
            Glide.with(imageFilm.context)
                .load(FilmsListFragment.PICTURE + item.image)
                .placeholder(R.drawable.ic_photo_black_24dp)
                .error(R.drawable.ic_error_outline_black_24dp)
                .centerCrop()
                .transform(RoundedCorners(30))
                .into(imageFilm)
        }
    }

    class FilmsWatchLaterAdapter(
        private val context: Context,
        private val inflater: LayoutInflater,
        private val listener: OnWatchLaterFilmsClickListener
    ) :
        RecyclerView.Adapter<FilmsWatchLaterViewHolder>() {
        private val items = ArrayList<WatchLaterItem>()

        fun setItems(films: List<WatchLaterItem>) {
            items.clear()
            items.addAll(films)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): FilmsWatchLaterViewHolder {
            return FilmsWatchLaterViewHolder(
                inflater.inflate(
                    R.layout.item_news,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: FilmsWatchLaterViewHolder, position: Int) {
            holder.container.animation =
                AnimationUtils.loadAnimation(
                    context,
                    R.anim.my_animation
                )
            val item = items[position]
            holder.bind(item)
            holder.itemView.setOnClickListener { listener.onWatchLaterFilmsFClick(item, position) }
        }

        interface OnWatchLaterFilmsClickListener {
            fun onWatchLaterFilmsFClick(filmsItem: WatchLaterItem, position: Int)
        }
    }
    //endregion
}