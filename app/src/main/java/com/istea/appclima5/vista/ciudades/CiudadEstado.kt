package com.istea.appclima5.vista.ciudad

import com.istea.appclima5.repository.domain.model.Ciudad

sealed class CiudadEstado {
    data object Vacio : CiudadEstado()
    data object Cargando : CiudadEstado()
    data class Exitoso(val ciudades: List<Ciudad>, val textoBusqueda: String = "") : CiudadEstado()
    data class Error(val mensaje: String) : CiudadEstado()
}