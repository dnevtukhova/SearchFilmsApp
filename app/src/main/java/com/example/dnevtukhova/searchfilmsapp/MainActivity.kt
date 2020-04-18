package com.example.dnevtukhova.searchfilmsapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.dnevtukhova.searchfilmsapp.DetailActivity.Companion.EXTRA_CHECK_BOX_SHOWN
import com.example.dnevtukhova.searchfilmsapp.DetailActivity.Companion.EXTRA_TEXT
import com.example.dnevtukhova.searchfilmsapp.DetailActivity.Companion.newIntent

class MainActivity : AppCompatActivity() {

    private var mCurrentIndex: Int=0
    private lateinit var mNameShining: TextView
    private lateinit var mNameCloudAtlas: TextView

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val buttonShining: Button = findViewById<Button>(R.id.button_shining)
        val buttonCloudAtlas: Button = findViewById<Button>(R.id.button_cloud_atlas)
        mNameShining = findViewById<TextView>(R.id.name_shining)
        mNameCloudAtlas = findViewById<TextView>(R.id.name_cloud_atlas)

        if (savedInstanceState!=null) {
            mCurrentIndex=savedInstanceState.getInt(KEY_INDEX)
        }

        changeColorText(mCurrentIndex)

        buttonShining.setOnClickListener {
            clickExplicit(SHINING)
            mNameShining.setTextColor(Color.parseColor("#f34336"))
            mNameCloudAtlas.setTextColor(Color.parseColor("#050100"))
            mCurrentIndex=1
        }

        buttonCloudAtlas.setOnClickListener {
            clickExplicit(CLOUD_ATLAS)
            mNameCloudAtlas.setTextColor(Color.parseColor("#f34336"))
            mNameShining.setTextColor(Color.parseColor("#050100"))
            mCurrentIndex=2
        }

        val mButtonInvite: Button = findViewById<Button>(R.id.button_invite_friend)
        mButtonInvite.setOnClickListener {
           val i: Intent = Intent(Intent.ACTION_SEND)
            i.setType("text/plain")
            i.putExtra(Intent.EXTRA_TEXT, getString(R.string.invite))
            startActivity(i)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_INDEX,mCurrentIndex)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
         if(requestCode== CODE) {
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
    }

    fun clickExplicit (value: String) {
        val intent: Intent = newIntent(this, value)
        startActivityForResult(intent, CODE)
    }

    fun changeColorText (value: Int) {
        if (mCurrentIndex==1) {
            mNameShining.setTextColor(Color.parseColor("#f34336"))
        }
        if (mCurrentIndex==2) {
            mNameCloudAtlas.setTextColor(Color.parseColor("#f34336"))
        }
    }

    companion object{
        const val SHINING = "shining"
        const val CLOUD_ATLAS = "cloudAtlas"
        const val KEY_INDEX = "INDEX"
        const val CODE = 0
        val TAG = MainActivity::class.java.simpleName
    }


}
