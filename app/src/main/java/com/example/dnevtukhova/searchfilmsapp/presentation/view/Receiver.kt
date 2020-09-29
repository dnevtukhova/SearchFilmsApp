package com.example.dnevtukhova.searchfilmsapp.presentation.view

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.dnevtukhova.searchfilmsapp.R
import com.example.dnevtukhova.searchfilmsapp.data.entity.FilmsItem

class Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, p1: Intent?) {
        val bundle = p1?.getBundleExtra(FilmsListFragment.BUNDLE)
        val filmsItem: FilmsItem? = bundle?.getParcelable(FilmsListFragment.FILMS_ITEM_EXTRA)
        val notificationIntent = Intent("$filmsItem.id", null, context, MainActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        notificationIntent.putExtra(MainActivity.FILM_FROM_NOTIFICATION, filmsItem)

        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val am = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= 23) {
            am.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(),
                pendingIntent
            )
        } else {
            am.setExact(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(),
                pendingIntent
            )
        }
        val builder = NotificationCompat.Builder(context, FilmsListFragment.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications_active_white_24dp)
            .setContentTitle(filmsItem!!.title)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setStyle(NotificationCompat.BigTextStyle().bigText(filmsItem.description))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationId = System.currentTimeMillis().toInt()
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, builder.build())
    }
}