package com.istea.appclima5.vista.clima

import com.istea.appclima5.repository.domain.model.Clima
import com.istea.appclima5.repository.domain.model.ListForecast

sealed class ClimaEstado {
    data object Cargando : ClimaEstado()
    data class Exitoso(
        val clima: Clima,
        val pronostico: List<ListForecast>,
        val nombreCiudad: String
    ) : ClimaEstado()
    data class Error(val mensaje: String) : ClimaEstado()
}