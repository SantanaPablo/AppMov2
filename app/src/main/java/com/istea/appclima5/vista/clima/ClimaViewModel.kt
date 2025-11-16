package com.istea.appclima5.vista.clima

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.istea.appclima5.repository.data.remote.RepositorioApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.istea.appclima5.repository.Repositorio

class ClimaViewModel(
    private val repositorio: Repositorio
) : ViewModel() {

    private val _estado = MutableStateFlow<ClimaEstado>(ClimaEstado.Cargando)
    val estado: StateFlow<ClimaEstado> = _estado.asStateFlow()

    private var datosCompartir: String = ""

    fun ejecutar(intencion: ClimaIntencion) {
        when (intencion) {
            is ClimaIntencion.CargarClima -> cargarClima(
                intencion.nombre,
                intencion.lat,
                intencion.lon
            )
            is ClimaIntencion.CambiarCiudad -> cambiarCiudad()
            is ClimaIntencion.Compartir -> compartir()
        }
    }

    private fun cargarClima(nombre: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            _estado.value = ClimaEstado.Cargando
            try {
                val clima = repositorio.traerClima(lat, lon)
                val pronostico = repositorio.traerPronostico(nombre)

                datosCompartir = generarTextoCompartir(clima, nombre)

                _estado.value = ClimaEstado.Exitoso(clima, pronostico, nombre)
            } catch (e: Exception) {
                _estado.value = ClimaEstado.Error("Error al cargar clima: ${e.message}")
            }
        }
    }

    private fun cambiarCiudad() {
    }

    private fun compartir() {
    }

    fun obtenerTextoCompartir(): String {
        return datosCompartir
    }

    private fun generarTextoCompartir(clima: com.istea.appclima5.repository.domain.model.Clima, ciudad: String): String {
        return """
            Clima en $ciudad
            
            Temperatura: ${clima.main.temp}°C
            Sensación térmica: ${clima.main.feels_like}°C
            Estado: ${clima.weather.firstOrNull()?.description ?: "Desconocido"}
            Humedad: ${clima.main.humidity}%
            Viento: ${clima.wind.speed} m/s
            
            Compartido desde AppDelClima
        """.trimIndent()
    }
}