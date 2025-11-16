package com.istea.appclima5.repository.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Ciudad(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String? = null
)