package com.istea.appclima5.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun weatherIcon(desc: String): ImageVector {
    val d = desc.lowercase()

    return when {
        "clear" in d -> Icons.Default.WbSunny
        "cloud" in d -> Icons.Default.Cloud
        "rain" in d -> Icons.Default.Umbrella
        "storm" in d || "thunder" in d -> Icons.Default.FlashOn
        "snow" in d -> Icons.Default.AcUnit
        else -> Icons.Default.WbCloudy
    }
}
