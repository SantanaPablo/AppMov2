package com.istea.appclima5.vista.ciudades

sealed class CiudadIntencion {
    data class BuscarCiudad(val texto: String) : CiudadIntencion()
    data class SeleccionarCiudad(
        val nombre: String,
        val lat: Double,
        val lon: Double,
        val country: String
    ) : CiudadIntencion()
    data class BuscarPorGeolocalizacion(val lat: Double, val lon: Double) : CiudadIntencion()
    data class ActualizarTexto(val texto: String) : CiudadIntencion()
}