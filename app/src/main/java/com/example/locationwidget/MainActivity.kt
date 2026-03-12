package com.example.locationwidget

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
            updateStatus()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        val permissionButton: Button = findViewById(R.id.permissionButton)
        val startButton: Button = findViewById(R.id.startButton)
        val stopButton: Button = findViewById(R.id.stopButton)

        permissionButton.setOnClickListener { requestPermissionsIfNeeded() }
        startButton.setOnClickListener {
            requestPermissionsIfNeeded()
            if (hasLocationPermissions()) {
                LocationUpdateService.start(this)
                updateWidgetNow(this)
                updateStatus()
            }
        }
        stopButton.setOnClickListener {
            LocationUpdateService.stop(this)
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_CITY, "Zatrzymano")
                .putString(KEY_POSTAL, "—")
                .apply()
            updateWidgetNow(this)
            updateStatus()
        }

        updateStatus()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun requestPermissionsIfNeeded() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun hasLocationPermissions(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private fun updateStatus() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val city = prefs.getString(KEY_CITY, "Brak danych") ?: "Brak danych"
        val postal = prefs.getString(KEY_POSTAL, "—") ?: "—"
        val running = LocationUpdateService.isRunning
        val permissions = if (hasLocationPermissions()) "OK" else "BRAK"

        statusText.text = "Pozwolenia: $permissions\nUsługa: ${if (running) "WŁĄCZONA" else "WYŁĄCZONA"}\nOstatnia lokalizacja: $city, $postal"
    }
}
