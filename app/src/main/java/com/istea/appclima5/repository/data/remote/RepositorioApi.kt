package com.istea.appclima5.repository.data.remote
import com.istea.appclima5.utils.Constantes
import com.istea.appclima5.repository.domain.model.Ciudad
import com.istea.appclima5.repository.domain.model.Clima
import com.istea.appclima5.repository.domain.model.ForecastDTO
import com.istea.appclima5.repository.domain.model.ListForecast
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class RepositorioApi {
    private val apiKey = "1cf7e82149f9a9a855dccecbdcff3778"

    private val cliente = HttpClient() {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun buscarCiudad(ciudad: String): List<Ciudad> {
        try {
            val respuesta = cliente.get("https://api.openweathermap.org/geo/1.0/direct") {
                parameter("q", ciudad)
                parameter("limit", 100)
                parameter("appid", apiKey)
            }

            if (respuesta.status == HttpStatusCode.OK) {
                return respuesta.body<List<Ciudad>>()
            } else {
                throw Exception("Error al buscar ciudad: ${respuesta.status}")
            }
        } catch (e: Exception) {
            throw Exception("Error al buscar ciudad: ${e.message}")
        }
    }

    suspend fun traerClima(lat: Double, lon: Double): Clima {
        try {
            val respuesta = cliente.get("https://api.openweathermap.org/data/2.5/weather") {
                parameter("lat", lat)
                parameter("lon", lon)
                parameter("units", "metric")
                parameter("lang", "es")
                parameter("appid", apiKey)
            }

            if (respuesta.status == HttpStatusCode.OK) {
                return respuesta.body<Clima>()
            } else {
                throw Exception("Error al traer clima: ${respuesta.status}")
            }
        } catch (e: Exception) {
            throw Exception("Error al traer clima: ${e.message}")
        }
    }

    suspend fun traerPronostico(nombre: String): List<ListForecast> {
        try {
            val respuesta = cliente.get("https://api.openweathermap.org/data/2.5/forecast") {
                parameter("q", nombre)
                parameter("units", "metric")
                parameter("lang", "es")
                parameter("appid", apiKey)
            }

            if (respuesta.status == HttpStatusCode.OK) {
                val forecast = respuesta.body<ForecastDTO>()
                return forecast.list
            } else {
                throw Exception("Error al traer pronóstico: ${respuesta.status}")
            }
        } catch (e: Exception) {
            throw Exception("Error al traer pronóstico: ${e.message}")
        }
    }

    suspend fun buscarCiudadPorCoordenada(lat: Double, lon: Double): List<Ciudad> {
        try {
            val respuesta = cliente.get("https://api.openweathermap.org/geo/1.0/reverse") {
                parameter("lat", lat)
                parameter("lon", lon)
                parameter("limit", 100)
                parameter("appid", apiKey)
        }
            if (respuesta.status == HttpStatusCode.OK) {
                return respuesta.body<List<Ciudad>>()
            } else {
                throw Exception("Error al buscar ciudad: ${respuesta.status}")
            }
        } catch (e: Exception) {
            throw Exception("Error al buscar ciudad: ${e.message}")
        }

    }

    suspend fun obtenerCiudadesSugeridas(): List<Ciudad> {
        val listaSugerencias = mutableListOf<Ciudad>()

        for (nombre in Constantes.CIUDADES_POPULARES) {
            try {
                val resultados = buscarCiudad(nombre)
                if (resultados.isNotEmpty()) {
                    listaSugerencias.add(resultados.first())
                }
            } catch (e: Exception) {
            }
        }
        return listaSugerencias
    }

}