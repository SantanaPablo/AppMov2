package com.istea.appclima5.vista.clima

sealed class ClimaIntencion {
    data class CargarClima(
        val nombre: String,
        val lat: Double,
        val lon: Double
    ) : ClimaIntencion()
    data object CambiarCiudad : ClimaIntencion()
    data object Compartir : ClimaIntencion()
}