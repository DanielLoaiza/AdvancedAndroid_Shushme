package com.example.android.shushme

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Result
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.PlaceBuffer

const val GEOFENCE_TIMEOUT = (24 * 60 * 60 * 1000).toLong()
const val GEOFENCE_RADIUS = 50F

class Geofencing(val context: Context, val googleApiClient: GoogleApiClient): ResultCallback<Result> {

    private var geoFencePendingIntent: PendingIntent? = null
    private val geofenceList: MutableList<Geofence> = mutableListOf()

    fun updateGeofencesList(places: PlaceBuffer?) {
        geofenceList.clear()
        if (places != null && places.count > 0) {
            places.forEach {
                val geofence = Geofence.Builder()
                        .setRequestId(it.id)
                        .setExpirationDuration(GEOFENCE_TIMEOUT)
                        .setCircularRegion(it.latLng.latitude, it.latLng.longitude, GEOFENCE_RADIUS)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build()

                geofenceList.add(geofence)
            }
        }
    }

    fun registerAllGeofences() {
        if(googleApiClient.isConnected && geofenceList.size > 0) {
            try {
                LocationServices.GeofencingApi.addGeofences(
                        googleApiClient,
                        getGeofenceRequest(),
                        getGeofencePendingIntent())
                        .setResultCallback(this)
            } catch (exception: SecurityException) {
                Log.e("Secutity", exception.message)
            }
        }
    }

    fun unregisterAllGeofences() {
        if(googleApiClient.isConnected && geofenceList.size > 0) {
            try {
                LocationServices.GeofencingApi.removeGeofences(
                        googleApiClient,
                        getGeofencePendingIntent())
                        .setResultCallback(this)
            } catch (exception: SecurityException) {
                Log.e("Secutity", exception.message)
            }
        }
    }

    private fun getGeofenceRequest(): GeofencingRequest {
        return GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofenceList)
                .build()
    }

    private fun getGeofencePendingIntent(): PendingIntent? {
        geoFencePendingIntent?.let {
            return it
        } ?: run {
            val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
            geoFencePendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            return geoFencePendingIntent
        }
    }

    override fun onResult(result: Result) {
        Log.e("result", result.status.toString())
    }
}
