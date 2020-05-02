package com.example.dnevtukhova.searchfilmsapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {

    private lateinit var mImageView: ImageView
    private lateinit var mTextView: TextView
    private lateinit var mCheckBox: CheckBox
    private lateinit var mEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme)
        }
        setContentView(R.layout.activity_detail)
        val bundle: Bundle? = intent.getExtras()
        val item: FilmsItem = bundle?.getSerializable(EXTRA_ID_BUTTON) as FilmsItem
        mImageView = findViewById(R.id.image_detail_view)
        mTextView = findViewById(R.id.description)
        mCheckBox = findViewById(R.id.checkbox_like)
        mEditText = findViewById(R.id.edit_text)
        initializeObjects(item)
    }

    @SuppressLint("ResourceType")
    fun initializeObjects(item: FilmsItem) {
        mImageView.setImageResource(item.image)
        description.setText(item.description)
    }

    companion object {
        const val EXTRA_ID_BUTTON = "com.example.dnevtukhova.searchfilmsapp.extra_id_button"

        fun newIntent(packageContext: Context, item: FilmsItem): Intent {
            val intent = Intent(packageContext, DetailActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable(EXTRA_ID_BUTTON, item)
            intent.putExtras(bundle)
            return intent
        }
    }
}
