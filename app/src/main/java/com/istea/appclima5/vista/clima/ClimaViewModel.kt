package com.istea.appclima5.vista.clima

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.istea.appclima5.repository.Repositorio
import com.istea.appclima5.repository.domain.model.Clima
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ClimaViewModel(
    private val repositorio: Repositorio
) : ViewModel() {

    private val _estado = MutableStateFlow<ClimaEstado>(ClimaEstado.Cargando)
    val estado: StateFlow<ClimaEstado> = _estado.asStateFlow()

    private val _eventosUi = MutableSharedFlow<ClimaEventoUi>()
    val eventosUi: SharedFlow<ClimaEventoUi> = _eventosUi.asSharedFlow()

    private var datosCompartir: String = ""

    fun ejecutar(intencion: ClimaIntencion) {
        when (intencion) {
            is ClimaIntencion.CargarClima -> cargarClima(intencion.nombre, intencion.lat, intencion.lon)
            is ClimaIntencion.CambiarCiudad -> emitirCambioCiudad()
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

                _estado.value = ClimaEstado.Exitoso(
                    clima = clima,
                    pronostico = pronostico,
                    nombreCiudad = nombre
                )

            } catch (e: Exception) {
                _estado.value = ClimaEstado.Error("Error al cargar clima")
                _eventosUi.emit(ClimaEventoUi.MostrarMensaje("Error: ${e.message}"))
            }
        }
    }

    private fun emitirCambioCiudad() {
        viewModelScope.launch {
            _eventosUi.emit(ClimaEventoUi.CambiarCiudad)
        }
    }

    private fun compartir() {
        viewModelScope.launch {
            _eventosUi.emit(ClimaEventoUi.CompartirTexto(datosCompartir))
        }
    }

    private fun generarTextoCompartir(clima: Clima, ciudad: String): String {
        return """
            Clima en $ciudad
            
            Temperatura: ${clima.main.temp}°C
            Sensación: ${clima.main.feels_like}°C
            Estado: ${clima.weather.firstOrNull()?.description ?: "N/A"}
            Humedad: ${clima.main.humidity}%
            Viento: ${clima.wind.speed} m/s
            
            Compartido desde AppDelClima
        """.trimIndent()
    }
}

sealed class ClimaEventoUi {
    data object CambiarCiudad : ClimaEventoUi()
    data class CompartirTexto(val texto: String) : ClimaEventoUi()
    data class MostrarMensaje(val mensaje: String) : ClimaEventoUi()
}
