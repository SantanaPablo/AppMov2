package com.istea.appclima5.repository

import com.istea.appclima5.repository.data.local.RepositorioMock
import com.istea.appclima5.repository.data.remote.RepositorioApi
import com.istea.appclima5.repository.domain.model.Ciudad
import com.istea.appclima5.repository.domain.model.Clima
import com.istea.appclima5.repository.domain.model.ListForecast

class Repositorio(private val usarMock: Boolean = true) {

    private val repositorioMock = RepositorioMock()
    private val repositorioApi = RepositorioApi()

    suspend fun buscarCiudad(ciudad: String): List<Ciudad> {
        return if (usarMock) {
            repositorioMock.buscarCiudad(ciudad)
        } else {
            repositorioApi.buscarCiudad(ciudad)
        }
    }

    suspend fun traerClima(lat: Double, lon: Double): Clima {
        return if (usarMock) {
            repositorioMock.traerClima(lat, lon)
        } else {
            repositorioApi.traerClima(lat, lon)
        }
    }

    suspend fun traerPronostico(nombre: String): List<ListForecast> {
        return if (usarMock) {
            repositorioMock.traerPronostico(nombre)
        } else {
            repositorioApi.traerPronostico(nombre)
        }
    }
}