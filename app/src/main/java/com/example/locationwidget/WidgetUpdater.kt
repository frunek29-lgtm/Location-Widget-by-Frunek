package com.example.locationwidget

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val PREFS_NAME = "location_widget_prefs"
const val KEY_CITY = "key_city"
const val KEY_POSTAL = "key_postal"
const val KEY_LAST_UPDATE = "key_last_update"

object WidgetUpdater {
    fun saveLocation(context: Context, city: String, postal: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CITY, city)
            .putString(KEY_POSTAL, postal)
            .putString(KEY_LAST_UPDATE, timestamp)
            .apply()
    }

    fun updateAllWidgets(context: Context) {
        LocationWidgetProvider.updateAll(context)
    }
}

fun updateWidgetNow(context: Context) {
    WidgetUpdater.updateAllWidgets(context)
}
