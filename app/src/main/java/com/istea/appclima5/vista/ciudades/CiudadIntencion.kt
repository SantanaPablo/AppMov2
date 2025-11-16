package com.istea.appclima5.vista.ciudad

sealed class CiudadIntencion {
    data class BuscarCiudad(val texto: String) : CiudadIntencion()
    data class SeleccionarCiudad(
        val nombre: String,
        val lat: Double,
        val lon: Double,
        val country: String
    ) : CiudadIntencion()
    data object BuscarPorGeolocalizacion : CiudadIntencion()
    data class ActualizarTexto(val texto: String) : CiudadIntencion()
}