package com.example.dnevtukhova.searchfilmsapp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dnevtukhova.searchfilmsapp.DetailActivity.Companion.EXTRA_CHECK_BOX_SHOWN
import com.example.dnevtukhova.searchfilmsapp.DetailActivity.Companion.EXTRA_TEXT
import com.example.dnevtukhova.searchfilmsapp.DetailActivity.Companion.newIntent
import com.example.dnevtukhova.searchfilmsapp.FavoriteActivity.Companion.EXTRA_FAVORITE_DELETE
import com.example.dnevtukhova.searchfilmsapp.FavoriteActivity.Companion.newIntentF

class MainActivity : AppCompatActivity() {
    @SuppressLint("ResourceType")
    @RequiresApi(Build.VERSION_CODES.M)
    val items = mutableListOf<FilmsItem>(
        FilmsItem(
            1,
            R.string.name_film_shining,
            R.string.description_shining,
            R.drawable.shining,
            R.drawable.ic_favorite_border_black_24dp
        ),
        FilmsItem(
            2,
            R.string.inception,
            R.string.inception_description,
            R.drawable.inception,
            R.drawable.ic_favorite_border_black_24dp
        ),
        FilmsItem(
            3,
            R.string.maleficent,
            R.string.maleficent_description,
            R.drawable.maleficenta,
            R.drawable.ic_favorite_border_black_24dp
        ),
        FilmsItem(
            4,
            R.string.name_film_cloud_atlas,
            R.string.description_cloud_atlas,
            R.drawable.cloud_atlas,
            R.drawable.ic_favorite_border_black_24dp
        ),
        FilmsItem(
            5,
            R.string.ford,
            R.string.ford_description,
            R.drawable.ford_ferrari,
            R.drawable.ic_favorite_border_black_24dp
        ),
        FilmsItem(
            6,
            R.string.joker,
            R.string.joker_description,
            R.drawable.joker,
            R.drawable.ic_favorite_border_black_24dp
        ),
        FilmsItem(
            7,
            R.string.green_book,
            R.string.green_book_description,
            R.drawable.green_book,
            R.drawable.ic_favorite_border_black_24dp
        )
    )
    var itemsFavoyrit = ArrayList<FilmsItem>()
    private var mCurrentIndex: Int = 0
    private lateinit var mNameShining: TextView
    private lateinit var mNameCloudAtlas: TextView
    var container: RelativeLayout? = null

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppCompatDelegate.getDefaultNightMode() === AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme)
        }
        setContentView(R.layout.activity_main)
        initRecycler()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.films_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.film_favorite -> {
                val intent: Intent = newIntentF(this, itemsFavoyrit)
                startActivityForResult(intent, CODE_FAVORITE)
            }
            R.id.invite_friend -> {
                val i: Intent = Intent(Intent.ACTION_SEND)
                i.setType("text/plain")
                i.putExtra(Intent.EXTRA_TEXT, getString(R.string.invite))
                startActivity(i)
            }
            R.id.app_theme -> {
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }

                finish();
                startActivity(Intent(applicationContext, MainActivity::class.java));
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_INDEX, mCurrentIndex)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODE) {
            var answer: String? = null
            var answerCheck: Boolean? = null
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    answer = it.getStringExtra(EXTRA_TEXT)
                    answerCheck = it.getBooleanExtra(EXTRA_CHECK_BOX_SHOWN, false)
                }
            }
            Log.i(TAG, "the answer is:$answer")
            Log.i(TAG, "checkbox is:$answerCheck")
        }
        if (requestCode == CODE_FAVORITE) {
            data?.let {
                val item: FilmsItem = it.getSerializableExtra(EXTRA_FAVORITE_DELETE) as FilmsItem
                itemsFavoyrit.remove(item)
                item.favorite = R.drawable.ic_favorite_border_black_24dp
                for (i in items.indices) {
                    if (items[i].id == item.id) {
                        items.set(i, item)
                    }
                }
                Log.i(TAG, "favorite delete is:$item")
            }
            initRecycler()
        }
    }

    override fun onBackPressed() {
        val dialog: Dialog = Dialog(this)
        dialog.setContentView(R.layout.custom_dialog)
        val yesBtn = dialog.findViewById<Button>(R.id.button_yes)
        val noBtn = dialog.findViewById<Button>(R.id.button_no)
        yesBtn.setOnClickListener { super.onBackPressed() }
        noBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    fun clickExplicit(value: FilmsItem) {
        val intent: Intent = newIntent(this, value)
        startActivityForResult(intent, CODE)
    }

    fun changeColorText(value: Int) {
        if (mCurrentIndex == 1) {
            mNameShining.setTextColor(Color.parseColor("#f34336"))
        }
        if (mCurrentIndex == 2) {
            mNameCloudAtlas.setTextColor(Color.parseColor("#f34336"))
        }
    }

    private fun initRecycler() {
        val recycler = findViewById<RecyclerView>(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler.layoutManager = layoutManager
        recycler.adapter = FilmsAdapter(this,
            LayoutInflater.from(this),
            items,
            object : FilmsAdapter.OnFilmsClickListener {
                override fun onFilmsClick(filmsItem: FilmsItem, position: Int) {
                    clickExplicit(filmsItem)

                }

            },
            object : FilmsAdapter.OnFavouriteClickListener {
                override fun onFavouriteClick(filmsItem: FilmsItem, position: Int) {

                    if (filmsItem.favorite == R.drawable.ic_favorite_red_24dp) {

                        items[position].favorite = R.drawable.ic_favorite_border_black_24dp
                        recycler.adapter?.notifyItemChanged(position)
                        itemsFavoyrit.remove(items[position])
                    } else {

                        itemsFavoyrit.add(items[position])
                        items[position].favorite = R.drawable.ic_favorite_red_24dp
                        recycler.adapter?.notifyItemChanged(position)
                    }
                }
            })


        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (layoutManager.findLastVisibleItemPosition() == items.size) {
                    //дописать...
                }
            }
        })

        val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        itemDecoration.setDrawable(getDrawable(R.drawable.white_line))
        recycler.addItemDecoration(itemDecoration)

    }

    companion object {
        const val KEY_INDEX = "INDEX"
        const val CODE = 0
        const val CODE_FAVORITE = 1
        val TAG = MainActivity::class.java.simpleName
    }

    //region adapter and holder
    class FilmsItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTv: TextView = itemView.findViewById(R.id.titleTv)
        val subtitleTv: TextView = itemView.findViewById(R.id.subtitleTv)
        val imageFilm: ImageView = itemView.findViewById(R.id.image)
        val imageFavourite: ImageView = itemView.findViewById(R.id.imageFavourite)
        var container: ConstraintLayout = itemView.findViewById(R.id.container)

        fun bind(item: FilmsItem) {
            titleTv.setText(item.title)
            subtitleTv.setText(item.description)
            imageFilm.setImageResource(item.image)
            imageFavourite.setImageResource(item.favorite)

        }
    }

    class FilmsAdapter(
        val context: Context,
        val inflater: LayoutInflater,
        val items: List<FilmsItem>,
        val listener: OnFilmsClickListener,
        val listenerFavourite: OnFavouriteClickListener
    ) :
        RecyclerView.Adapter<FilmsItemViewHolder>() {
        // val mContext?: Context

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmsItemViewHolder {
            return FilmsItemViewHolder(inflater.inflate(R.layout.item_news, parent, false))
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: FilmsItemViewHolder, position: Int) {
// lets create the animation for the whole card
// first lets create a reference to it
// you ca use the previous same animation like the following
// but i want to use a different one so lets create it ..
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
            imageFavourite.setOnClickListener { listenerFavourite.onFavouriteClick(item, position) }
        }

        interface OnFilmsClickListener {
            fun onFilmsClick(filmsItem: FilmsItem, position: Int)
        }

        interface OnFavouriteClickListener {
            fun onFavouriteClick(filmsItem: FilmsItem, position: Int)
        }

    }
    //endregion

}
