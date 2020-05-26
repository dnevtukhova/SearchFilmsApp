package com.example.dnevtukhova.searchfilmsapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlinx.android.synthetic.main.fragment_detail.*

class DetailFragment : Fragment() {

    private lateinit var mImageView: ImageView
    private lateinit var mTextView: TextView
    private lateinit var mCheckBox: CheckBox
    private lateinit var mEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val item: FilmsItem = arguments?.getSerializable(EXTRA_ID_BUTTON) as FilmsItem
        val collapsingToolbarLayout =
            view.findViewById(R.id.collapsing_toolbar) as CollapsingToolbarLayout
        collapsingToolbarLayout.title = item.title
        collapsingToolbarLayout.setExpandedTitleColor(
            getColor(
                context!!,
                android.R.color.transparent
            )
        )
        mImageView = view.findViewById(R.id.image_detail_view)
        mTextView = view.findViewById(R.id.description)
        mCheckBox = view.findViewById(R.id.checkbox_like)
        mEditText = view.findViewById(R.id.edit_text)
        initializeObjects(item)
    }

    @SuppressLint("ResourceType")
    fun initializeObjects(item: FilmsItem) {
        Glide.with(mImageView.context)
            .load(FilmsListFragment.PICTURE + item.image)
            .into(mImageView)
        description.text = item.description
    }

    companion object {
        const val EXTRA_ID_BUTTON = "com.example.dnevtukhova.searchfilmsapp.extra_id_button"
        const val TAG = "DetailFragment"

        fun newInstance(item: FilmsItem): DetailFragment {
            val fragment = DetailFragment()
            val bundle = Bundle()
            bundle.putSerializable(EXTRA_ID_BUTTON, item)
            fragment.arguments = bundle
            return fragment
        }
    }
}
