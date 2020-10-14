package com.example.dnevtukhova.searchfilmsapp.presentation.view

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.dnevtukhova.searchfilmsapp.R
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants.PICTURE
import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmsItem
import com.example.dnevtukhova.searchfilmsapp.di.Injectable
import com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel.WatchLaterFragmentViewModel
import kotlinx.android.synthetic.main.item_film_watch_later.view.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class WatchLaterFragment : Fragment(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener, Injectable {
    @Inject
    lateinit var filmsViewModelFactory: ViewModelProvider.Factory
    private lateinit var adapterWatchLaterFilms: FilmsWatchLaterAdapter
    lateinit var watchLaterViewModel: WatchLaterFragmentViewModel
    private val calendar: Calendar = Calendar.getInstance()
    private var watchLaterItem: FilmsItem? = null
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

        watchLaterViewModel = ViewModelProvider(
            requireActivity(),
            filmsViewModelFactory
        ).get(WatchLaterFragmentViewModel::class.java)
        watchLaterViewModel.watchLaterFilms?.observe(
            this.viewLifecycleOwner,
            { films -> adapterWatchLaterFilms.setItems(films) })
    }

    private fun initRecycler(view: View) {
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerViewWatchLater)
        adapterWatchLaterFilms =
            FilmsWatchLaterAdapter(
                LayoutInflater.from(context),
                object :
                    FilmsWatchLaterAdapter.OnWatchLaterFilmsClickListener {
                    @RequiresApi(Build.VERSION_CODES.M)
                    override fun onWatchLaterFilmsFClick(filmsItem: FilmsItem, position: Int) {
                        watchLaterItem = filmsItem
                        myPosition = position
                        intent = createIntent(filmsItem)
                        selectDateAndTime()
                    }

                    override fun onCancelClick(filmsItem: FilmsItem, position: Int) {
                        filmsItem.watchLater = true
                        watchLaterViewModel.changeWatchLater(filmsItem)
                        //   if (pIntentOnce != null) {
                        intent = createIntent(filmsItem)
                        val pIntentOnce = PendingIntent.getBroadcast(
                            requireContext(),
                            0,
                            intent,
                            PendingIntent.FLAG_CANCEL_CURRENT
                        )
                        val am = ContextCompat.getSystemService(
                            requireContext(),
                            AlarmManager::class.java
                        )
                        am?.cancel (pIntentOnce)

                        adapterWatchLaterFilms.notifyItemChanged(position)
                    }
                }
            )
        recycler.adapter = adapterWatchLaterFilms
        val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        AppCompatResources.getDrawable(
            requireContext(),
            R.drawable.white_line
        )
            ?.let { itemDecoration.setDrawable(it) }
        recycler.addItemDecoration(itemDecoration)
    }

    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.M)
    fun selectDateAndTime() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
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
        val dateNotification = calendar.timeInMillis
        if (dateNotification < GregorianCalendar().timeInMillis) {
            Toast.makeText(
                requireContext(),
                requireContext().getString(R.string.dateInFutureError),
                Toast.LENGTH_LONG
            ).show()
        } else {
            watchLaterItem!!.dateToWatch = dateNotification
            watchLaterViewModel.setDateToWatch(watchLaterItem!!)
            adapterWatchLaterFilms.notifyItemChanged(myPosition!!)
            Log.d(TAG, intent.toString())
            val pIntentOnce =
                PendingIntent.getBroadcast(
                    requireContext(),
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            val am =
                ContextCompat.getSystemService(requireContext(), AlarmManager::class.java)
            am?.setExact(
                AlarmManager.RTC_WAKEUP,
                dateNotification,
                pIntentOnce
            )
        }
    }

    fun createIntent(filmsItem: FilmsItem): Intent {
        val intent = Intent("${filmsItem.id}", null, context, NotificationReceiver::class.java)
        val bundle = Bundle()
        bundle.putParcelable(FilmsListFragment.FILMS_ITEM_EXTRA, filmsItem)
        intent.putExtra(FilmsListFragment.BUNDLE, bundle)
        return intent
    }

    //region adapter and holder
    class FilmsWatchLaterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTv: TextView = itemView.findViewById(R.id.titleTvWatchLater)
        private val date: TextView = itemView.findViewById(R.id.dateWatchLater)
        private val imageFilm: ImageView = itemView.findViewById(R.id.imageWatchLater)
        private val imageClock: ImageView = itemView.findViewById(R.id.dateToWatchImage)
        private val cancelText: TextView = itemView.findViewById(R.id.cancelNotification)

        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        fun bind(item: FilmsItem) {
            titleTv.text = item.title
            val locale = Locale("RU")
            val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy HH:mm", locale)
            val dateToWatch = simpleDateFormat.format(Date(item.dateToWatch!!)).toString()
            date.text = dateToWatch
            cancelText.text = itemView.context.getString(R.string.cancelText)
            imageClock.setImageResource(R.drawable.ic_baseline_alarm_30)
            Glide.with(imageFilm.context)
                .load(PICTURE + item.image)
                .placeholder(R.drawable.ic_photo_black_24dp)
                .error(R.drawable.ic_error_outline_black_24dp)
                .centerCrop()
                .transform(RoundedCorners(30))
                .into(imageFilm)
            itemView.containerWatchLater.animation =
                AnimationUtils.loadAnimation(
                    itemView.context,
                    R.anim.my_animation
                )
        }

    }

    class FilmsWatchLaterAdapter(
        private val inflater: LayoutInflater,
        private val listener: OnWatchLaterFilmsClickListener

    ) :
        RecyclerView.Adapter<FilmsWatchLaterViewHolder>() {
        private val items = ArrayList<FilmsItem>()

        fun setItems(films: List<FilmsItem>) {
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
                    R.layout.item_film_watch_later,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: FilmsWatchLaterViewHolder, position: Int) {

            val item = items[position]
            holder.bind(item)
            holder.itemView.setOnClickListener { listener.onWatchLaterFilmsFClick(item, position) }
            val cancelText: TextView = holder.itemView.findViewById(R.id.cancelNotification)
            cancelText.setOnClickListener { listener.onCancelClick(item, position) }
        }

        interface OnWatchLaterFilmsClickListener {
            fun onWatchLaterFilmsFClick(filmsItem: FilmsItem, position: Int)
            fun onCancelClick(filmsItem: FilmsItem, position: Int)
        }
    }
    //endregion
}