package com.example.dnevtukhova.searchfilmsapp.presentation.view

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.dnevtukhova.searchfilmsapp.R
import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmsItem

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, p1: Intent?) {
        val bundle = p1?.getBundleExtra(FilmsListFragment.BUNDLE)
        val filmsItem: FilmsItem? = bundle?.getParcelable(FilmsListFragment.FILMS_ITEM_EXTRA)
        Log.d("NotificationReceiver", "filmsItem $filmsItem")
        val notificationIntent = Intent("${filmsItem!!.id}", null, context, MainActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        notificationIntent.putExtra(MainActivity.FILM_FROM_NOTIFICATION, filmsItem)

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context!!, FilmsListFragment.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications_active_white_24dp)
            .setContentTitle(filmsItem!!.title)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setStyle(NotificationCompat.BigTextStyle().bigText(filmsItem.description))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationId = System.currentTimeMillis().toInt()
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, builder.build())
    }
}