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
import com.istea.appclima5.vista.ciudades.CiudadEstado
import com.istea.appclima5.vista.ciudades.CiudadIntencion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.istea.appclima5.repository.data.local.IConfiguracionLocal

class CiudadViewModel(
    private val repositorio: Repositorio,
    private val configuracionLocal: IConfiguracionLocal
) : ViewModel() {

    private val _estado = MutableStateFlow<CiudadEstado>(CiudadEstado.Vacio)
    val estado: StateFlow<CiudadEstado> = _estado.asStateFlow()

    init {
        cargarSugerenciasIniciales()
    }

    fun ejecutar(intencion: CiudadIntencion) {
        when (intencion) {
            is CiudadIntencion.BuscarCiudad -> buscarCiudad(intencion.texto)
            is CiudadIntencion.SeleccionarCiudad -> seleccionarCiudad(intencion)
            is CiudadIntencion.BuscarPorGeolocalizacion -> buscarPorGeolocalizacion(intencion.lat, intencion.lon)
            is CiudadIntencion.ActualizarTexto -> actualizarTexto(intencion.texto)
        }
    }

    private fun buscarCiudad(texto: String) {
        if (texto.isBlank()) {
            cargarSugerenciasIniciales()
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

    private fun seleccionarCiudad(intencion: CiudadIntencion.SeleccionarCiudad) {
        try {
            configuracionLocal.guardarCiudad(
                intencion.nombre,
                intencion.lat,
                intencion.lon,
                intencion.country
            )
        } catch (e: Exception) {
            _estado.value = CiudadEstado.Error("Error al guardar ciudad seleccionada")
        }
    }

    private fun buscarPorGeolocalizacion(lat: Double, lon: Double) {
        viewModelScope.launch {
            _estado.value = CiudadEstado.Cargando
            try {
                val ciudades = repositorio.buscarCiudadPorCoordenada(lat, lon)
                _estado.value = CiudadEstado.Exitoso(ciudades)
            } catch (e: Exception) {
                _estado.value = CiudadEstado.Error("Error al buscar por coordenadas: ${e.message}")
            }
        }
    }

    private fun actualizarTexto(texto: String) {
        val actual = _estado.value
        if (actual is CiudadEstado.Exitoso) {
            _estado.value = actual.copy(textoBusqueda = texto)
        }
    }

    fun obtenerUbicacionActual(context: Context, onUbicacion: (Double, Double) -> Unit) {
        val permiso = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permiso != PackageManager.PERMISSION_GRANTED) return

        val client = LocationServices.getFusedLocationProviderClient(context)

        client.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onUbicacion(location.latitude, location.longitude)
            } else {
                solicitarNuevaUbicacion(client, onUbicacion)
            }
        }
    }

    private fun solicitarNuevaUbicacion(
        fusedLocationClient: FusedLocationProviderClient,
        onUbicacion: (Double, Double) -> Unit
    ) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            8_000L
        ).build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation
                if (loc != null) {
                    onUbicacion(loc.latitude, loc.longitude)
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )
        } catch (_: SecurityException) { }
    }

    private fun cargarSugerenciasIniciales() {
        viewModelScope.launch {
            _estado.value = CiudadEstado.Cargando
            try {
                val sugerencias = repositorio.obtenerCiudadesSugeridas()
                _estado.value = CiudadEstado.Exitoso(sugerencias, "")
            } catch (e: Exception) {
                _estado.value = CiudadEstado.Error("Error al cargar sugerencias")
            }
        }
    }
}
