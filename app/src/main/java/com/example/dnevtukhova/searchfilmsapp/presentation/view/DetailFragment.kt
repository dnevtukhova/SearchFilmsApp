package com.example.dnevtukhova.searchfilmsapp.presentation.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment.DIRECTORY_PICTURES
import android.os.Environment.getExternalStoragePublicDirectory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.dnevtukhova.searchfilmsapp.App
import com.example.dnevtukhova.searchfilmsapp.R
import com.example.dnevtukhova.searchfilmsapp.data.FilmsItem
import com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel.FilmsListViewModel
import com.example.dnevtukhova.searchfilmsapp.presentation.viewmodel.FilmsViewModelFactory
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_detail.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

const val PERMISSION_REQUEST_WRITE_STORAGE = 0

class DetailFragment : Fragment() {

    private lateinit var detailViewViewModel: FilmsListViewModel
    private lateinit var filmsDetailItem: FilmsItem

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressbarLoadImage.visibility = View.INVISIBLE
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
                        requireContext(),
                        android.R.color.transparent
                    )
                )
                initializeObjects(filmsDetail)
                filmsDetailItem = filmsDetail
                button_load_file.setOnClickListener { loadPoster() }
            })
    }

    private fun loadPoster() {
        // Проверяем разрешения на запись в хранилище
        if (requireActivity().checkSelfPermissionCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // разрешение уже получено, запускаем процесс загрузки картинки и сохранения в галерее
            progressbarLoadImage.visibility = View.VISIBLE
            Completable.fromRunnable {
                saveGallery()
            }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        requireView().showSnackbar(
                            R.string.fileSaveInGallery,
                            Snackbar.LENGTH_INDEFINITE
                        )
                        if (progressbarLoadImage != null) {
                            progressbarLoadImage.visibility = View.GONE
                        }
                    }

                    override fun onError(e: Throwable) {
                        if (progressbarLoadImage != null) {
                            progressbarLoadImage.visibility = View.GONE
                        }
                        requireView().showSnackbar("Ошибка $e", Snackbar.LENGTH_SHORT)
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
                Completable.fromRunnable {
                    saveGallery()
                }
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : DisposableCompletableObserver() {
                        override fun onComplete() {
                            requireView().showSnackbar(
                                R.string.fileSaveInGallery,
                                Snackbar.LENGTH_INDEFINITE
                            )
                            if (progressbarLoadImage != null) {
                                progressbarLoadImage.visibility = View.GONE
                            }
                        }

                        override fun onError(e: Throwable) {
                            if (progressbarLoadImage != null) {
                                progressbarLoadImage.visibility = View.GONE
                            }
                            requireView().showSnackbar("Ошибка $e", Snackbar.LENGTH_SHORT)
                        }
                    })

            } else {
                requireView().showSnackbar(R.string.permissionNoGranted, Snackbar.LENGTH_SHORT)

            }
        }
    }

    private fun saveGallery() {
        Glide.with(requireContext())
            .asBitmap()
            .load(FilmsListFragment.PICTURE + filmsDetailItem.image)
            .into(object : CustomTarget<Bitmap>(500, 500) {
                override fun onLoadCleared(placeholder: Drawable?) {
                    Toast.makeText(context, "onLoadCleared", Toast.LENGTH_SHORT).show()
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    saveImage(resource)
                }
            })
    }

    private fun saveImage(image: Bitmap) {
        val imageFileName = "JPEG_ " + filmsDetailItem.title + ".jpg"
        val storageDir = getExternalStoragePublicDirectory("$DIRECTORY_PICTURES/SearchFilmsApp")
        var success = true
        if (!storageDir.exists()) {
            success = storageDir.mkdirs()
        }
        if (success) {
            val imageFile = File(storageDir, imageFileName)
            val savedImagePath = imageFile.absolutePath
            try {
                val fOut: OutputStream = FileOutputStream(imageFile)
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // Add the image to the system gallery
            galleryAddPic(savedImagePath)
        }
    }

    private fun galleryAddPic(imagePath: String) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri: Uri = Uri.fromFile(File(imagePath))
        mediaScanIntent.data = contentUri
        requireActivity().sendBroadcast(mediaScanIntent)
    }

    @SuppressLint("ResourceType")
    fun initializeObjects(item: FilmsItem) {
        Glide.with(image_detail_view.context)
            .load(FilmsListFragment.PICTURE + item.image)
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
