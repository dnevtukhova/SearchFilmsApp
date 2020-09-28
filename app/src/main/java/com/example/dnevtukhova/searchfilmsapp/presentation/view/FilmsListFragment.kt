package com.example.dnevtukhova.searchfilmsapp.presentation.view

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AbsListView
import android.widget.DatePicker
import android.widget.SearchView
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.edit
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dnevtukhova.searchfilmsapp.R
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants.PAGE_NUMBER
import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmsItem
import com.example.dnevtukhova.searchfilmsapp.di.Injectable
import com.example.dnevtukhova.searchfilmsapp.presentation.view.adapter.FilmsAdapter
import com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel.DetailFragmentViewModel
import com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel.FilmsListViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_film_list.*
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
    lateinit var detailViewModel: DetailFragmentViewModel
    lateinit var searchView: SearchView
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

        initRecycler(view)

        filmsViewModel = ViewModelProvider(
            requireActivity(),
            filmsViewModelFactory
        ).get(FilmsListViewModel::class.java)

        filmsViewModel.films?.observe(
            this.viewLifecycleOwner,
            { films ->
                filmsViewModel.initSharedPref()
                adapterFilms.setItems(films)
            })

        filmsViewModel.error.observe(
            this.viewLifecycleOwner,
            { error ->
                requireView().showSnackbar("Ошибка $error", Snackbar.LENGTH_LONG, "Обновить") {
                    filmsViewModel.refreshAllFilms()
                }
            })

        filmsViewModel.searchFilms?.observe(
            this.viewLifecycleOwner,
            { films ->
                adapterFilmsForSearch.setItems(films)
            })

        filmsViewModel.progressBar.observe(
            this.viewLifecycleOwner,
            { if (progressbar != null) {
                    if (it) {
                        progressbar.isVisible = true
                    } else {
                        progressbar.isGone = true
                    }
                }
            }
        )

        swipeRefreshLayout.setOnRefreshListener {
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

    private fun initRecycler(view: View) {
        recycler = view.findViewById(R.id.recyclerViewFragment)
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recycler.layoutManager = layoutManager
        adapterFilms =
            FilmsAdapter(
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
                if (isScrolling && (currentItems + scrollOutItems == totalItems)) {
                    isScrolling = false
                    fetchData()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
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
        adapterFilmsForSearch = FilmsAdapter(
            requireContext(),
            LayoutInflater.from(context),
            object : FilmsAdapter.OnFilmsClickListener {
                override fun onFilmsClick(filmsItem: FilmsItem, position: Int) {
                    detailViewModel.selectFilm(filmsItem)
                    listener?.onFilmsSelected(filmsItem)
                }
                override fun onFavouriteClick(filmsItem: FilmsItem, position: Int) {
                }
                @RequiresApi(Build.VERSION_CODES.M)
                override fun onWatchLaterClick(filmsItem: FilmsItem, position: Int) {
                }
            })
        recyclerViewFragmentSearch.adapter = adapterFilmsForSearch
        recyclerViewFragmentSearch.addItemDecoration(itemDecoration)
    }

    private fun fetchData() {
        val page = filmsViewModel.mSettings.getInt(PAGE_NUMBER, 0)
        val page2 = page + 1
        filmsViewModel.mSettings.edit { putInt(PAGE_NUMBER, page2) }.apply {
        }
        filmsViewModel.refreshAllFilms()
        recycler.post {
            adapterFilms.notifyItemRangeChanged(
                layoutManager.itemCount,
                20)
        }
    }

    interface FilmsListListener {
        fun onFilmsSelected(filmsItem: FilmsItem)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.films_menu, menu)
        val searchItem: MenuItem = menu.findItem(R.id.action_search)
        val searchManager =
            requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = searchItem.actionView as SearchView
        searchView.setOnClickListener {
            searchView.isIconified = false
        }
        searchView.clearFocus()
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                filmsViewModel.getFilmsFromSearch(query!!)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText!!.length > 2) {
                    recycler.isVisible = false
                    recyclerViewFragmentSearch.isVisible = true
                }
                filmsViewModel.getFilmsFromSearch(newText)
                adapterFilmsForSearch.notifyDataSetChanged()
                if (newText.isEmpty()) {
                    recycler.isVisible = true
                    recyclerViewFragmentSearch.isVisible = false
                }
                return false
            }
        })
        searchView.setOnCloseListener {
            recyclerViewFragmentSearch.isVisible = false
            recycler.isVisible = true
            true
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        SettingsFragment(),
                        SettingsFragment.TAG
                    )
                    .addToBackStack(SettingsFragment.TAG)
                    .commit()

                return false
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
