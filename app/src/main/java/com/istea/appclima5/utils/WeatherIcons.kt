package com.istea.appclima5.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp

@Composable
fun WeatherIconVector(desc: String, modifier: Modifier = Modifier) {
    val d = desc.lowercase()

    val (icon, color) = when {

        "clear" in d || "despejado" in d || "claro" in d || "sun" in d ->
            Icons.Default.WbSunny to Color(0xFFFFC107)

        "nublado" in d || "nubes" in d || "cloud" in d || "overcast" in d ->
            Icons.Default.Cloud to Color(0xFF90A4AE)

        "lluvia" in d || "rain" in d || "drizzle" in d ->
            Icons.Default.Umbrella to Color(0xFF2196F3)

        "tormenta" in d || "trueno" in d || "thunderstorm" in d ->
            Icons.Default.FlashOn to Color(0xFFFF5722)

        "nieve" in d || "snow" in d || "sleet" in d ->
            Icons.Default.AcUnit to Color(0xFFB3E5FC)


        else -> Icons.Default.WbCloudy to Color.Gray
    }

    Image(
        painter = rememberVectorPainter(icon),
        contentDescription = null,
        modifier = modifier.size(64.dp),
        colorFilter = ColorFilter.tint(color)
    )
}