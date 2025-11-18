package com.istea.appclima5.repository.data.local

interface IConfiguracionLocal {
    fun guardarCiudad(nombre: String, lat: Double, lon: Double, country: String)
    fun obtenerCiudadGuardada(): CiudadGuardada?
    fun borrarCiudad()
}