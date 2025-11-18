package com.istea.appclima5

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.istea.appclima5.repository.Repositorio
import com.istea.appclima5.repository.data.local.ConfiguracionLocal
import com.istea.appclima5.router.Ruta
import com.istea.appclima5.vista.AppNavegacion

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val repositorio: Repositorio,
    private val configuracionLocal: ConfiguracionLocal
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(com.istea.appclima5.vista.ciudad.CiudadViewModel::class.java)) {
            return com.istea.appclima5.vista.ciudad.CiudadViewModel(repositorio, configuracionLocal) as T
        }
        if (modelClass.isAssignableFrom(com.istea.appclima5.vista.clima.ClimaViewModel::class.java)) {
            return com.istea.appclima5.vista.clima.ClimaViewModel(repositorio) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun MainPage() {
    val context = LocalContext.current.applicationContext
    val configuracionLocal = remember { ConfiguracionLocal(context) }
    val repositorio = remember { Repositorio(usarMock = false) }
    val factory = remember { ViewModelFactory(repositorio, configuracionLocal) }
    val ciudadGuardada = remember { configuracionLocal.obtenerCiudadGuardada() }

    val startDestination: Ruta =
        if (ciudadGuardada != null) {
            Ruta.Clima(
                nombre = ciudadGuardada.nombre,
                lat = ciudadGuardada.lat.toFloat(),
                lon = ciudadGuardada.lon.toFloat(),
                country = ciudadGuardada.country
            )
        } else {
            Ruta.Ciudades
        }

    AppNavegacion(
        factory = factory,
        startDestination = startDestination,
        configuracionLocal = configuracionLocal
    )
}
