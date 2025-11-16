package com.istea.appclima5.repository.data.local

import com.istea.appclima5.repository.domain.model.*

class RepositorioMock {
    private val cordoba = Ciudad(
        name = "Cordoba",
        lat = -31.4201,
        lon = -64.1888,
        country = "Argentina",
        state = "Cordoba"
    )

    private val bsAs = Ciudad(
        name = "Buenos Aires",
        lat = -34.6037,
        lon = -58.3816,
        country = "Argentina",
        state = "Buenos Aires"
    )

    private val laPlata = Ciudad(
        name = "La Plata",
        lat = -34.9215,
        lon = -57.9545,
        country = "Argentina",
        state = "Buenos Aires"
    )

    private val rosario = Ciudad(
        name = "Rosario",
        lat = -32.9442,
        lon = -60.6505,
        country = "Argentina",
        state = "Santa Fe"
    )

    private val mendoza = Ciudad(
        name = "Mendoza",
        lat = -32.8895,
        lon = -68.8458,
        country = "Argentina",
        state = "Mendoza"
    )

    private val ciudades = listOf(cordoba, bsAs, laPlata, rosario, mendoza)

    suspend fun buscarCiudad(ciudad: String): List<Ciudad> {
        if (ciudad == "error") {
            throw Exception("Error simulado en búsqueda")
        }
        return ciudades.filter { it.name.contains(ciudad, ignoreCase = true) }
    }

    suspend fun traerClima(lat: Double, lon: Double): Clima {
        return Clima(
            coord = Coord(lon = lon, lat = lat),
            weather = listOf(
                Weather(
                    id = 800,
                    main = "Clear",
                    description = "cielo despejado",
                    icon = "01d"
                )
            ),
            main = Main(
                temp = 25.5,
                feels_like = 24.8,
                temp_min = 22.0,
                temp_max = 28.0,
                pressure = 1013,
                humidity = 65
            ),
            wind = Wind(speed = 5.5, deg = 180),
            clouds = Clouds(all = 10),
            dt = System.currentTimeMillis() / 1000,
            name = ciudades.find {
                kotlin.math.abs(it.lat - lat) < 0.1 && kotlin.math.abs(it.lon - lon) < 0.1
            }?.name ?: "Ciudad"
        )
    }
    suspend fun buscarCiudadPorCoordenada(lat: Double, lon: Double): List<Ciudad> {
        if (lat == 0.0 && lon == 0.0) {
            throw Exception("Error simulado en búsqueda")
        }
        return ciudades.filter { it.lat == lat && it.lon == lon }
    }

    suspend fun traerPronostico(nombre: String): List<ListForecast> {
        val baseTime = System.currentTimeMillis() / 1000
        val pronosticos = mutableListOf<ListForecast>()

        repeat(40) { index ->
            val dayOffset = index / 8
            val tempBase = 20.0 + (dayOffset * 2)
            val tempVariation = if (index % 8 < 4) 3.0 else -2.0

            pronosticos.add(
                ListForecast(
                    dt = baseTime + (index * 10800L), // +3 horas cada vez
                    main = MainForecast(
                        temp = tempBase + tempVariation + (kotlin.math.sin(index.toDouble()) * 2),
                        feels_like = tempBase + tempVariation,
                        temp_min = tempBase - 2,
                        temp_max = tempBase + 4,
                        pressure = 1013 + (index % 5),
                        humidity = 60 + (index % 20)
                    ),
                    weather = listOf(
                        WeatherForecast(
                            id = if (index % 3 == 0) 800 else 801,
                            main = if (index % 3 == 0) "Clear" else "Clouds",
                            description = if (index % 3 == 0) "cielo despejado" else "algo nublado",
                            icon = if (index % 3 == 0) "01d" else "02d"
                        )
                    ),
                    clouds = CloudsForecast(all = (index % 4) * 25),
                    wind = WindForecast(
                        speed = 3.0 + (index % 5).toDouble(),
                        deg = 180 + (index % 8) * 45
                    ),
                    dt_txt = java.text.SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        java.util.Locale.getDefault()
                    ).format(java.util.Date((baseTime + (index * 10800L)) * 1000))
                )
            )
        }

        return pronosticos
    }
}

class RepositorioMockError {
    suspend fun buscarCiudad(ciudad: String): List<Ciudad> {
        throw Exception("Error de conexión simulado")
    }

    suspend fun traerClima(lat: Double, lon: Double): Clima {
        throw Exception("Error al obtener clima")
    }

    suspend fun traerPronostico(nombre: String): List<ListForecast> {
        throw Exception("Error al obtener pronóstico")
    }

    suspend fun buscarCiudadPorCoordenada(lat: Double, lon: Double): List<Ciudad> {
        throw Exception("Error de conexión simulado")
    }
}


