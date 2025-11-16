package com.istea.appclima5.repository.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ForecastDTO(
    val list: List<ListForecast>
)

@Serializable
data class ListForecast(
    val dt: Long,
    val main: MainForecast,
    val weather: List<WeatherForecast>,
    val clouds: CloudsForecast,
    val wind: WindForecast,
    val dt_txt: String
)

@Serializable
data class MainForecast(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int
)

@Serializable
data class WeatherForecast(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

@Serializable
data class CloudsForecast(
    val all: Int
)

@Serializable
data class WindForecast(
    val speed: Double,
    val deg: Int
)