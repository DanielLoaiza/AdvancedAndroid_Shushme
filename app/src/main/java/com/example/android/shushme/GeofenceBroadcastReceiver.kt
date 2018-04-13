package com.example.android.shushme

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private val TAG = GeofenceBroadcastReceiver::class.java.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        val geoFenceTransition = geofencingEvent.geofenceTransition

        when (geoFenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Log.e(TAG, "unknown transition ")
                setRingerMode(context, AudioManager.RINGER_MODE_SILENT)
                buildNotification(context, geoFenceTransition)
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Log.e(TAG, "unknown transition ")
                setRingerMode(context, AudioManager.RINGER_MODE_NORMAL)
                buildNotification(context, geoFenceTransition)
            }
            else -> Log.e(TAG, "unknown transition ")
        }
    }

    private fun setRingerMode(context: Context?, mode: Int) {
        val notificationManager: NotificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT < 24 || android.os.Build.VERSION.SDK_INT >= 24
                && !notificationManager.isNotificationPolicyAccessGranted) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.ringerMode = mode
        }

    }

    private fun buildNotification(context: Context?, transitionType: Int) {
        createChannel(context)
        context?.let {
            val notificationBuilder = NotificationCompat.Builder(it, "DefaultChannel")
            if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
                notificationBuilder.setSmallIcon(R.drawable.ic_volume_off_white_24dp)
                        .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_volume_off_white_24dp))
                        .setContentText("silent mode on")
            } else {
                notificationBuilder.setSmallIcon(R.drawable.ic_volume_up_white_24dp)
                        .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_volume_up_white_24dp))
                        .setContentText("Back to normal")
            }

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(0, notificationBuilder.build())
        }


    }

    private fun createChannel(context: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val name = "DefaultChannel"
            val description = "DefaultDescripttion"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(name, name, importance)
            channel.description = description
            // Register the channel with the system
            val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}