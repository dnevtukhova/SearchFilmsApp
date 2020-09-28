package com.example.dnevtukhova.searchfilmsapp.presentation.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.dnevtukhova.searchfilmsapp.R
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants.PICTURE
import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmsItem
import com.example.dnevtukhova.searchfilmsapp.di.Injectable
import com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel.DetailFragmentViewModel
import com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel.FilmsViewModelFactory
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_detail.*
import java.io.File
import javax.inject.Inject

const val PERMISSION_REQUEST_WRITE_STORAGE = 0

class DetailFragment : Fragment(), Injectable {
    @Inject
    lateinit var filmsViewModelFactory: FilmsViewModelFactory
    lateinit var detailViewViewModel: DetailFragmentViewModel
    private lateinit var filmsDetailItem: FilmsItem

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressbarLoadImage.visibility = View.INVISIBLE
        val bundle = arguments
        val filmsItem: FilmsItem? = bundle?.getParcelable("key")

        detailViewViewModel = ViewModelProvider(
            requireActivity(),
            filmsViewModelFactory
        ).get(DetailFragmentViewModel::class.java)
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
                        requireContext(),
                        android.R.color.transparent
                    )
                )
                initializeObjects(filmsDetail)
                filmsDetailItem = filmsDetail
                button_load_file.setOnClickListener { loadPoster() }
                if (!filmsDetail.favorite) {
                    imageFavoriteDetail.setImageResource(R.drawable.ic_favorite_red_48dp)
                }
                ratingText.text = "Рейтинг ${filmsDetail.average}"
            })
        imageShared.setOnClickListener {
            imageShared.animation = AnimationUtils.loadAnimation(
                context,
                R.anim.my_animation
            )
            val i = Intent(Intent.ACTION_SEND)
            i.type = "text/plain"
            val text =
                getString(R.string.invite) + " ${filmsDetailItem.title} - ${filmsDetailItem.description}"
            i.putExtra(Intent.EXTRA_TEXT, text)
            startActivity(i)
        }
    }

    private fun loadPoster() {
        // Проверяем разрешения на запись в хранилище
        if (requireActivity().checkSelfPermissionCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // разрешение уже получено, запускаем процесс загрузки картинки и сохранения в галерее
            progressbarLoadImage.visibility = View.VISIBLE
            detailViewViewModel.loadImage(filmsDetailItem, requireContext())
            detailViewViewModel.loadImageLiveD.observe(this.viewLifecycleOwner,
                Observer<DetailFragmentViewModel.State> { result ->
                    when (result) {
                        is DetailFragmentViewModel.State.Success -> {
                            if (progressbarLoadImage != null) {
                                progressbarLoadImage.visibility = View.GONE
                            }
                            galleryAddPic(result.imagePath)
                            requireView().showSnackbar(
                                R.string.fileSaveInGallery,
                                Snackbar.LENGTH_INDEFINITE
                            )
                        }
                        is DetailFragmentViewModel.State.Error -> {
                            if (progressbarLoadImage != null) {
                                progressbarLoadImage.visibility = View.GONE
                            }
                            requireView().showSnackbar(result.error!!, Snackbar.LENGTH_SHORT)
                        }
                    }
                })
        } else {
            // Нужно получить разрещшение
            requireView().showSnackbar(
                R.string.permissionSaveFile,
                Snackbar.LENGTH_SHORT
            )
            requestWriteExternalStoragePermission()
        }
    }

    private fun requestWriteExternalStoragePermission() {
        // Permission has not been granted and must be requested.
        if (requireActivity().shouldShowRequestPermissionRationaleCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requireView().showSnackbar(
                R.string.saveImageInGallery,
                Snackbar.LENGTH_INDEFINITE, R.string.ok
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_WRITE_STORAGE
                )
            }
        } else {
            requireView().showSnackbar(
                R.string.getPermissionSaveFile,
                Snackbar.LENGTH_LONG
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_WRITE_STORAGE) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requireView().showSnackbar(R.string.permissionIsDone, Snackbar.LENGTH_SHORT)
                loadPoster()
            } else {
                requireView().showSnackbar(R.string.permissionNoGranted, Snackbar.LENGTH_SHORT)
            }
        }
    }

    private fun galleryAddPic(imagePath: String?) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri: Uri = Uri.fromFile(File(imagePath))
        mediaScanIntent.data = contentUri
        requireActivity().sendBroadcast(mediaScanIntent)
    }

    @SuppressLint("ResourceType")
    fun initializeObjects(item: FilmsItem) {
        Glide.with(image_detail_view.context)
            .load(PICTURE + item.image)
            .into(image_detail_view)
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
