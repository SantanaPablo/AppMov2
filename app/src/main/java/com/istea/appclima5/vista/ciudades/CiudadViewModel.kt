package com.istea.appclima5.vista.ciudad

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.istea.appclima5.repository.Repositorio
import com.istea.appclima5.repository.data.local.ConfiguracionLocal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CiudadViewModel(
    private val repositorio: Repositorio,
    private val configuracionLocal: ConfiguracionLocal
) : ViewModel() {

    private val _estado = MutableStateFlow<CiudadEstado>(CiudadEstado.Vacio)
    val estado: StateFlow<CiudadEstado> = _estado.asStateFlow()

    fun ejecutar(intencion: CiudadIntencion) {
        when (intencion) {
            is CiudadIntencion.BuscarCiudad -> buscarCiudad(intencion.texto)
            is CiudadIntencion.SeleccionarCiudad -> seleccionarCiudad(
                intencion.nombre,
                intencion.lat,
                intencion.lon,
                intencion.country
            )
            is CiudadIntencion.BuscarPorGeolocalizacion -> buscarPorGeolocalizacion(intencion.lat, intencion.lon)
            is CiudadIntencion.ActualizarTexto -> actualizarTexto(intencion.texto)
        }
    }

    private fun buscarCiudad(texto: String) {
        if (texto.isBlank()) {
            _estado.value = CiudadEstado.Vacio
            return
        }

        viewModelScope.launch {
            _estado.value = CiudadEstado.Cargando
            try {
                val ciudades = repositorio.buscarCiudad(texto)
                _estado.value = CiudadEstado.Exitoso(ciudades, texto)
            } catch (e: Exception) {
                _estado.value = CiudadEstado.Error("Error al buscar ciudades: ${e.message}")
            }
        }
    }

    private fun seleccionarCiudad(nombre: String, lat: Double, lon: Double, country: String) {
        try {
            configuracionLocal.guardarCiudad(nombre, lat, lon, country)
        } catch (e: Exception) {
            _estado.value = CiudadEstado.Error("Error al guardar ciudad: ${e.message}")
        }
    }

    private fun buscarPorGeolocalizacion(lat: Double, lon: Double) {
        _estado.value = CiudadEstado.Cargando
    }

    private fun actualizarTexto(texto: String) {
        val estadoActual = _estado.value
        if (estadoActual is CiudadEstado.Exitoso) {
            _estado.value = estadoActual.copy(textoBusqueda = texto)
        }
    }

    fun obtenerUbicacionActual(context: Context, onUbicacion: (Double, Double) -> Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    onUbicacion(location.latitude, location.longitude)
                } else {
                    solicitarNuevaUbicacion(fusedLocationClient, onUbicacion)
                }
            }
        }
    }

    private fun solicitarNuevaUbicacion(fusedLocationClient: FusedLocationProviderClient, onUbicacion: (Double, Double) -> Unit) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 10000L
        ).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    onUbicacion(location.latitude, location.longitude)
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
        }
    }
}