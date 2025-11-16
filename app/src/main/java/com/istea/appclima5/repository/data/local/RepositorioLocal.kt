package com.istea.appclima5.repository.data.local

import android.content.Context
import android.content.SharedPreferences

class ConfiguracionLocal(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "app_clima_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_CIUDAD_NOMBRE = "ciudad_nombre"
        private const val KEY_CIUDAD_LAT = "ciudad_lat"
        private const val KEY_CIUDAD_LON = "ciudad_lon"
        private const val KEY_CIUDAD_COUNTRY = "ciudad_country"
    }

    fun guardarCiudad(nombre: String, lat: Double, lon: Double, country: String) {
        prefs.edit().apply {
            putString(KEY_CIUDAD_NOMBRE, nombre)
            putFloat(KEY_CIUDAD_LAT, lat.toFloat())
            putFloat(KEY_CIUDAD_LON, lon.toFloat())
            putString(KEY_CIUDAD_COUNTRY, country)
            apply()
        }
    }

    fun obtenerCiudadGuardada(): CiudadGuardada? {
        val nombre = prefs.getString(KEY_CIUDAD_NOMBRE, null)
        val lat = prefs.getFloat(KEY_CIUDAD_LAT, 0f)
        val lon = prefs.getFloat(KEY_CIUDAD_LON, 0f)
        val country = prefs.getString(KEY_CIUDAD_COUNTRY, null)

        return if (nombre != null && country != null && lat != 0f && lon != 0f) {
            CiudadGuardada(
                nombre = nombre,
                lat = lat.toDouble(),
                lon = lon.toDouble(),
                country = country
            )
        } else {
            null
        }
    }

    fun borrarCiudad() {
        prefs.edit().clear().apply()
    }
}

data class CiudadGuardada(
    val nombre: String,
    val lat: Double,
    val lon: Double,
    val country: String
)