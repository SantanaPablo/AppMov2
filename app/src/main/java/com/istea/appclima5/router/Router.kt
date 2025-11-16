package com.istea.appclima5.router

import kotlinx.serialization.Serializable
@Serializable
sealed class Ruta {
    @Serializable
    data object Ciudades : Ruta()

    @Serializable
    data class Clima(
        val nombre: String,
        val lat: Float,
        val lon: Float,
        val country: String
    ) : Ruta()
}