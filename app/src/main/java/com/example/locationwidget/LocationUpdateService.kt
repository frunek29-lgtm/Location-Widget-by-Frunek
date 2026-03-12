package com.example.locationwidget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.IOException
import java.util.Locale

class LocationUpdateService : Service(), LocationListener {

    private lateinit var locationManager: LocationManager

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Uruchamianie usługi lokalizacji..."))
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        requestUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        requestUpdates()
        return START_STICKY
    }

    private fun requestUpdates() {
        try {
            if (PermissionUtils.hasLocationPermission(this)) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        10_000L,
                        0f,
                        this
                    )
                }
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        10_000L,
                        0f,
                        this
                    )
                }

                val lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                lastKnown?.let { onLocationChanged(it) }
            } else {
                WidgetUpdater.saveLocation(this, "Brak pozwolenia", "—")
                WidgetUpdater.updateAllWidgets(this)
            }
        } catch (security: SecurityException) {
            WidgetUpdater.saveLocation(this, "Brak pozwolenia", "—")
            WidgetUpdater.updateAllWidgets(this)
        }
    }

    override fun onLocationChanged(location: Location) {
        val geocoder = Geocoder(this, Locale.getDefault())
        var city = "Nieznana miejscowość"
        var postal = "Brak kodu"

        try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            val address = addresses?.firstOrNull()
            city = address?.locality
                ?: address?.subAdminArea
                ?: address?.adminArea
                ?: city
            postal = address?.postalCode ?: postal
        } catch (_: IOException) {
        } catch (_: IllegalArgumentException) {
        }

        WidgetUpdater.saveLocation(this, city, postal)
        WidgetUpdater.updateAllWidgets(this)
        val text = "$city, $postal"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        try {
            locationManager.removeUpdates(this)
        } catch (_: SecurityException) {
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Location Widget")
            .setContentText(content)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Widget Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "location_widget_channel"
        private const val NOTIFICATION_ID = 1001

        @Volatile
        var isRunning: Boolean = false

        fun start(context: Context) {
            val intent = Intent(context, LocationUpdateService::class.java)
            ContextCompatCompat.startForegroundServiceCompat(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, LocationUpdateService::class.java)
            context.stopService(intent)
            isRunning = false
        }
    }
}
