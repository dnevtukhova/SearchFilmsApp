package com.example.dnevtukhova.searchfilmsapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {

    private lateinit var mImageView: ImageView
    private lateinit var mTextView: TextView
    private lateinit var mCheckBox: CheckBox
    private lateinit var mEditText: EditText
    private lateinit var text: String
    private var isChecked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppCompatDelegate.getDefaultNightMode() === AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme)
        }
        setContentView(R.layout.activity_detail)
        var bundle: Bundle = intent.getExtras()
        val item : FilmsItem = bundle.getSerializable(EXTRA_ID_BUTTON) as FilmsItem

        mImageView = findViewById<ImageView>(R.id.image_detail_view)
        mTextView = findViewById<TextView>(R.id.description)

        initializateObjects(item)
        mCheckBox = findViewById<CheckBox>(R.id.checkbox_like)
        mEditText = findViewById<EditText>(R.id.edit_text)


        mEditText.addTextChangedListener(object : TextWatcher  {
            override fun afterTextChanged(p0: Editable?) {
                getAnswerText(intent)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                text=p0.toString()
            }
        })
        mCheckBox.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {

                if (p0?.isChecked!!) {
                    isChecked= true
                    getAnswerCheckBox(intent)
                }

            }
        })
        getAnswerCheckBox(intent)
    }

    @SuppressLint("ResourceType")
    fun initializateObjects (item: FilmsItem) {
        mImageView.setImageResource(item.image)
        description.setText(item.description)
    }



    fun getAnswerCheckBox (intent: Intent) {
        intent.putExtra(EXTRA_CHECK_BOX_SHOWN, isChecked)
        setResult(Activity.RESULT_OK, intent)
    }

    fun getAnswerText (intent: Intent) {
        intent.putExtra(EXTRA_TEXT, text)
       setResult(Activity.RESULT_OK, intent)
    }


    companion object{
        const val EXTRA_ID_BUTTON = "com.example.dnevtukhova.searchfilmsapp.extra_id_button"
        const val EXTRA_CHECK_BOX_SHOWN = "com.example.dnevtukhova.searchfilmsapp.extra_check_box_shown"
        const val EXTRA_TEXT = "com.example.dnevtukhova.searchfilmsapp.extra_text"


        fun newIntent (packageContext: Context, item: FilmsItem): Intent {
             val intent = Intent (packageContext, DetailActivity::class.java)
           val bundle : Bundle = Bundle();
            bundle.putSerializable(EXTRA_ID_BUTTON, item);
            intent.putExtras(bundle)
            return intent
            }
    }
}
