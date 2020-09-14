package com.example.dnevtukhova.searchfilmsapp.presentation.view

//import androidx.appcompat.widget.SearchView.OnQueryTextListener
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.dnevtukhova.searchfilmsapp.R
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants.PAGE_NUMBER
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants.PICTURE
import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmsItem
import com.example.dnevtukhova.searchfilmsapp.di.Injectable
import com.example.dnevtukhova.searchfilmsapp.presentation.view.adapter.FilmsAdapter

import com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel.DetailFragmentViewModel
import com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel.FilmsListViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_film_list.*
import testing.OpenForTesting
import java.util.*
import javax.inject.Inject

open class FilmsListFragment : Fragment(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener, Injectable {
    var listener: FilmsListListener? = null
    private lateinit var recycler: RecyclerView
    lateinit var adapterFilms: FilmsAdapter
    lateinit var adapterFilmsForSearch: FilmsAdapter

    @Inject
    lateinit var filmsViewModelFactory: ViewModelProvider.Factory

    lateinit var filmsViewModel: FilmsListViewModel
    lateinit var layoutManager: LinearLayoutManager
    var isScrolling: Boolean = false

    //    by viewModels {
//         filmsViewModelFactory
//     }
    lateinit var detailViewModel: DetailFragmentViewModel
    lateinit var searchView: SearchView
    //  lateinit var queryTextListener: SearchView.OnQueryTextListener



    private val calendar: Calendar = Calendar.getInstance()
    private var pIntentOnce: PendingIntent? = null
    private var am: AlarmManager? = null
    private var intent: Intent? = null
    private var dateNotification: Long? = null
    private var myFilmsItem: FilmsItem? = null
    private var myPosition: Int? = null

    companion object {
        const val TAG = "FilmsListFragment"
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
        progressbar.visibility = View.VISIBLE

        filmsViewModel = ViewModelProvider(
            requireActivity(),
            filmsViewModelFactory
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
                requireView().showSnackbar("Ошибка $error", Snackbar.LENGTH_LONG, "Обновить") {
                    filmsViewModel.refreshAllFilms()
                }
            })

        filmsViewModel.searchFilms?.observe(
            this.viewLifecycleOwner,
            Observer<MutableList<FilmsItem>> { films ->
                adapterFilmsForSearch.setItems(films)
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

        detailViewModel = ViewModelProvider(
            requireActivity(),
            filmsViewModelFactory
        ).get(DetailFragmentViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        retainInstance = true
    }

    override fun onAttach(context: Context) {
        injectMembers()
        super.onAttach(context)
    }

    protected open fun injectMembers() =
        AndroidSupportInjection.inject(this)

    private fun initRecycler(view: View, progressBar: ProgressBar) {
        recycler = view.findViewById(R.id.recyclerViewFragment)
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recycler.layoutManager = layoutManager
        adapterFilms =
            FilmsAdapter(
                progressBar,
                requireContext(),
                LayoutInflater.from(context),
                object :
                    FilmsAdapter.OnFilmsClickListener {
                    override fun onFilmsClick(filmsItem: FilmsItem, position: Int) {
                        detailViewModel.selectFilm(filmsItem)
                        listener?.onFilmsSelected(filmsItem)
                    }

                    override fun onFavouriteClick(filmsItem: FilmsItem, position: Int) {
                        Log.d(
                            TAG,
                            "${filmsItem.id} ${filmsItem.title} ${filmsItem.description} ${filmsItem.image}"
                        )
                        filmsViewModel.selectFavorite(filmsItem)
                        adapterFilms.notifyItemChanged(position)
                    }

                    @RequiresApi(Build.VERSION_CODES.M)
                    override fun onWatchLaterClick(filmsItem: FilmsItem, position: Int) {
                        intent = createIntent(filmsItem)
                        if (filmsItem.watchLater) {
                            myFilmsItem = filmsItem
                            myFilmsItem!!.watchLater = false
                            myPosition = position
                            selectDateAndTime()
                        } else {
                            filmsItem.watchLater = true
                            filmsViewModel.changeWatchLater(filmsItem)
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
super.onScrolled(recyclerView, dx, dy)
                val currentItems = layoutManager.childCount
                val totalItems = layoutManager.itemCount
                val scrollOutItems = layoutManager.findLastVisibleItemPosition()
                if(isScrolling&&(currentItems+scrollOutItems==totalItems)) {
                    isScrolling = false
                    fetchData()
                }

                //                if (layoutManager.findLastVisibleItemPosition() == layoutManager.itemCount - 2) {
//                    val page = filmsViewModel.mSettings.getInt(PAGE_NUMBER, 0)
//                    Log.d("page", "$page")
//                    val page2 = page + 1
//                    filmsViewModel.mSettings.edit { putInt(PAGE_NUMBER, page2) }.apply {
//                    }
//                    Log.d("page2", "$page2")
//                    filmsViewModel.refreshAllFilms()
//                    recycler.post {
//                        adapterFilms.notifyItemRangeChanged(
//                            layoutManager.itemCount,
//                            20
//                        )
//                    }
//                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(newState==AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true
                }
            }
        })

        val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        getDrawable(
            requireContext(),
            R.drawable.white_line
        )?.let { itemDecoration.setDrawable(it) }
        recycler.addItemDecoration(itemDecoration)

        //for search
        val layoutManagerSearch = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerViewFragmentSearch.layoutManager = layoutManagerSearch
        adapterFilmsForSearch = FilmsAdapter(progressBar,
            requireContext(),
            LayoutInflater.from(context),
            object : FilmsAdapter.OnFilmsClickListener {
                override fun onFilmsClick(filmsItem: FilmsItem, position: Int) {
                    detailViewModel.selectFilm(filmsItem)
                    listener?.onFilmsSelected(filmsItem)
                }

                override fun onFavouriteClick(filmsItem: FilmsItem, position: Int) {
                    Log.d(
                        TAG,
                        "${filmsItem.id} ${filmsItem.title} ${filmsItem.description} ${filmsItem.image}"
                    )
                    filmsViewModel.addFilm(filmsItem)
                    filmsViewModel.selectFavorite(filmsItem)
                    adapterFilmsForSearch.notifyItemChanged(position)
                }

                @RequiresApi(Build.VERSION_CODES.M)
                override fun onWatchLaterClick(filmsItem: FilmsItem, position: Int) {
                    intent = createIntent(filmsItem)

                    if (filmsItem.watchLater) {
                        myFilmsItem = filmsItem
                        myFilmsItem!!.watchLater = false
                        myPosition = position
                        selectDateAndTime()
                    } else {
                        filmsItem.watchLater = true
                        filmsViewModel.changeWatchLater(filmsItem)
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

        recyclerViewFragmentSearch.adapter = adapterFilmsForSearch
        recyclerViewFragmentSearch.addItemDecoration(itemDecoration)
    }

    private fun fetchData() {
        progressbar.visibility = View.VISIBLE
//        Completable.fromRunnable {
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
             //   adapterFilms.notifyDataSetChanged()
            }
            progressbar.visibility = View.GONE
//        }.subscribeOn(Schedulers.computation())
//            .subscribe()

    }

    interface FilmsListListener {
        fun onFilmsSelected(filmsItem: FilmsItem)
    }

    //region adapter and holder


    //endregion

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
        myFilmsItem!!.dateToWatch = dateNotification
        filmsViewModel.addFilm(myFilmsItem!!)
        filmsViewModel.changeWatchLater(myFilmsItem!!)
        adapterFilms.notifyItemChanged(myPosition!!)
        adapterFilmsForSearch.notifyDataSetChanged()
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
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description

            val notificationManager =
                requireActivity().getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }

    //    override fun onCreateOptionsMenu(menu: Menu?, MenuInflater inflater): Boolean {
//        val inflater = menuInflater
//        inflater.inflate(R.menu.films_menu, menu)
//        return true
//    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.films_menu, menu)
        val searchItem: MenuItem = menu.findItem(R.id.action_search)
        val searchManager =
            requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager

        searchView = searchItem.actionView as SearchView
        searchView.setOnClickListener { searchView.isIconified = false }
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
//        queryTextListener =
//           object: SearchView.OnQueryTextListener {
//               override fun onQueryTextSubmit(query: String?): Boolean {
//                   Log.i("onQueryTextSubmit", query)
//                   return true
//               }
//
//               override fun onQueryTextChange(newText: String?): Boolean {
//                   Log.i("onQueryTextChange", newText)
//                   return true
//               }
//
//           }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.i("onQueryTextSubmit", query)

                filmsViewModel.getFilmsFromSearch(query!!)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.i("onQueryTextChange", newText)
                if (newText!!.length > 2) {
                    recycler.isVisible = false
                    recyclerViewFragmentSearch.isVisible = true
                    filmsViewModel.getFilmsFromSearch(newText)
                    adapterFilmsForSearch.notifyDataSetChanged()
//                    recycler.isVisible = false
//                    recyclerViewFragmentSearch.isVisible = true
                }

                return false
            }

        })
        searchView.setOnCloseListener {
            recyclerViewFragmentSearch.isVisible = false
            recycler.isVisible = true
            true
        }

//    recyclerViewFragmentSearch.isVisible = false
//    recycler.isVisible = true

        // searchView.setOnSe
        //super.onCreateOptionsMenu(menu, inflater)
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item?.itemId) {
//            R.id.action_search -> {
////                val i = Intent(Intent.ACTION_SEND)
////                i.type = "text/plain"
////                i.putExtra(Intent.EXTRA_TEXT, getString(R.string.invite))
////                startActivity(i)
//                return false
//            }
//    }
//      //  searchView.setOnQueryTextListener(queryTextListener)
//        return super.onOptionsItemSelected(item)
//    }
}
