package com.example.dnevtukhova.searchfilmsapp.presentation.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.dnevtukhova.searchfilmsapp.App
import com.example.dnevtukhova.searchfilmsapp.R
import com.example.dnevtukhova.searchfilmsapp.data.FilmsItem
import com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel.FilmsListViewModel
import com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel.FilmsViewModelFactory
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlinx.android.synthetic.main.fragment_detail.*

class DetailFragment : Fragment() {

    private lateinit var mImageView: ImageView
    private lateinit var mTextView: TextView
    private lateinit var mCheckBox: CheckBox
    private lateinit var mEditText: EditText
    private lateinit var detailViewViewModel: FilmsListViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = arguments
        val filmsItem: FilmsItem? = bundle?.getParcelable("key")

        val myViewModelFactory = FilmsViewModelFactory(App.instance.filmsInteractor)
        detailViewViewModel = ViewModelProvider(
            requireActivity(),
            myViewModelFactory
        ).get(FilmsListViewModel::class.java)
        Log.d(TAG, "filmsItem $filmsItem")
        if (filmsItem != null) {
            detailViewViewModel.selectFilm(filmsItem)
        }
        detailViewViewModel.filmsDetail.observe(
            this.viewLifecycleOwner,
            Observer<FilmsItem> { filmsDetail ->
                val collapsingToolbarLayout =
                    view.findViewById(R.id.collapsing_toolbar) as CollapsingToolbarLayout
                collapsingToolbarLayout.title = filmsDetail.title
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
                initializeObjects(filmsDetail)
            })
    }

    @SuppressLint("ResourceType")
    fun initializeObjects(item: FilmsItem) {
        Glide.with(mImageView.context)
            .load(FilmsListFragment.PICTURE + item.image)
            .into(mImageView)
        description.text = item.description
    }

    companion object {
        const val TAG = "DetailFragment"
        fun newInstance(): DetailFragment {
            return DetailFragment()
        }

        fun newInstance(filmsItem: FilmsItem): DetailFragment {
            val detailFragment = DetailFragment()
            val bundle = Bundle()
            bundle.putParcelable("key", filmsItem)
            detailFragment.arguments = bundle
            return detailFragment
        }
    }
}
