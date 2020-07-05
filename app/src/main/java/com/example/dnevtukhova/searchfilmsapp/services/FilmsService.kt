package com.example.dnevtukhova.searchfilmsapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.example.dnevtukhova.searchfilmsapp.R
import com.example.dnevtukhova.searchfilmsapp.data.FilmsItem
import com.example.dnevtukhova.searchfilmsapp.presentation.view.FilmsListFragment
import com.example.dnevtukhova.searchfilmsapp.presentation.view.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FilmsService : FirebaseMessagingService() {
    companion object {
        const val TAG ="FilmsService"
        const val NOTIFICATION_CHANNEL_ID = "notification from firebase"
        const val NOTIFICATION_NAME = "Notifications"
        const val NOTIFICATION_DESCRIPTION = ""
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, remoteMessage.data.getValue("id"))
        Log.d(TAG, remoteMessage.data.getValue("icon"))

        remoteMessage.notification?.let {
            Log.d(TAG, "title: " + it.title)
            Log.d(TAG, "body: " + it.body)
            Log.d(TAG, "icon: " + it.icon)

        }
        val filmsItem = FilmsItem((remoteMessage.data.getValue("id")).toInt(), remoteMessage.notification!!.title!!, remoteMessage.notification!!.body!!, remoteMessage.data.getValue("icon").toString(), true,true )
        sendNotification(filmsItem)
    }

        private fun sendNotification(filmsItem: FilmsItem) {

            val notificationChannelId = NOTIFICATION_CHANNEL_ID

            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (nm.getNotificationChannel(notificationChannelId) == null) {
                    NotificationChannel(notificationChannelId, NOTIFICATION_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                        description = NOTIFICATION_DESCRIPTION
                        enableLights(true)
                        enableVibration(true)
                        setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
                        nm.createNotificationChannel(this)
                    }
                }
            }

            val notificationIntent = Intent("$filmsItem.id", null, applicationContext, MainActivity::class.java)
            notificationIntent.putExtra(MainActivity.FILM_FROM_NOTIFICATION, filmsItem)
            val bitmap: Bitmap = Glide
                .with(this)
                .asBitmap()
                .load(FilmsListFragment.PICTURE + filmsItem.image)
                .submit()
                .get()


            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                1,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_active_white_24dp)
                .setContentTitle(filmsItem.title)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                 .setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap)
                        .setSummaryText(filmsItem.description))
             //  .setStyle(NotificationCompat.BigTextStyle().bigText(filmsItem.description))
                .setAutoCancel(true)
             //   .setLargeIcon(bitmap)

                .setContentIntent(pendingIntent)

            val notificationId = System.currentTimeMillis().toInt()
            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(notificationId, builder.build())
        }


}