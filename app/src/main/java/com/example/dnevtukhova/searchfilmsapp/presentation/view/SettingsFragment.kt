package com.example.dnevtukhova.searchfilmsapp.presentation.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.example.dnevtukhova.searchfilmsapp.App
import com.example.dnevtukhova.searchfilmsapp.R
import com.example.dnevtukhova.searchfilmsapp.data.api.NetworkConstants
import kotlinx.android.synthetic.main.settings_fragment.*

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mSettings = App.instance.applicationContext.getSharedPreferences(
            SETTINGS,
            Context.MODE_PRIVATE
        )
        val theme = mSettings.getBoolean(NetworkConstants.THEME, true)

        darkButton.setOnClickListener {
            if (theme) {
                mSettings.edit { putBoolean(NetworkConstants.THEME, false) }.apply {
                    requireActivity().finish()
                    App.isSettingsFragmentToOpen = true
                   startActivity(Intent(context, MainActivity::class.java))
                }
            } else {
                Toast.makeText(context, requireContext().getString(R.string.darkTheme), Toast.LENGTH_SHORT).show()
            }
        }

        lightButton.setOnClickListener {
            if (!theme) {
                mSettings.edit { putBoolean(NetworkConstants.THEME, true) }.apply {
                    requireActivity().finish()
                    App.isSettingsFragmentToOpen = true
                    startActivity(Intent(context, MainActivity::class.java))
                }
            } else {
                Toast.makeText(context, requireContext().getString(R.string.lightTheme), Toast.LENGTH_SHORT).show()
            }
        }

        logoMovie.setOnClickListener {
            val browseIntent = Intent(Intent.ACTION_VIEW, Uri.parse(URI))
            startActivity(browseIntent)
        }
    }

    companion object {
        const val TAG = "SettingsFragment"
        const val URI  = "https://www.themoviedb.org"
        const val SETTINGS = "Settings"
    }
}