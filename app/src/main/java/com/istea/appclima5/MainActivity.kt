package com.istea.appclima5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.istea.appclima5.repository.Repositorio
import com.istea.appclima5.repository.data.local.ConfiguracionLocal
import com.istea.appclima5.router.Ruta
import com.istea.appclima5.ui.theme.AppClima5Theme
import com.istea.appclima5.vista.ciudad.CiudadVista
import com.istea.appclima5.vista.ciudad.CiudadViewModel
import com.istea.appclima5.vista.clima.ClimaVista
import com.istea.appclima5.vista.clima.ClimaViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val repositorio: Repositorio,
    private val configuracionLocal: ConfiguracionLocal
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CiudadViewModel::class.java)) {
            return CiudadViewModel(repositorio, configuracionLocal) as T
        }
        if (modelClass.isAssignableFrom(ClimaViewModel::class.java)) {
            return ClimaViewModel(repositorio) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppClima5Theme {
                val context = LocalContext.current.applicationContext
                val configuracionLocal = remember { ConfiguracionLocal(context) }

                val repositorio = remember { Repositorio(usarMock = true) }

                val factory = remember { ViewModelFactory(repositorio, configuracionLocal) }
                val ciudadGuardada = remember { configuracionLocal.obtenerCiudadGuardada() }
                val startDestination: Ruta = if (ciudadGuardada != null) {
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
        }
    }
}

@Composable
fun AppNavegacion(
    factory: ViewModelFactory,
    startDestination: Ruta,
    configuracionLocal: ConfiguracionLocal
) {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable<Ruta.Ciudades> {
                val ciudadViewModel: CiudadViewModel = viewModel(factory = factory)

                CiudadVista(
                    viewModel = ciudadViewModel,
                    onCiudadSeleccionada = { nombre, lat, lon, country ->
                        val proximaRuta = Ruta.Clima(
                            nombre,
                            lat.toFloat(),
                            lon.toFloat(),
                            country
                        )
                        navController.navigate(proximaRuta) {
                            popUpTo(Ruta.Ciudades::class) { inclusive = true }
                        }
                    }
                )
            }

            composable<Ruta.Clima> { navBackStackEntry ->
                val args = navBackStackEntry.toRoute<Ruta.Clima>()

                val climaViewModel: ClimaViewModel = viewModel(factory = factory)

                ClimaVista(
                    viewModel = climaViewModel,
                    nombre = args.nombre,
                    lat = args.lat.toDouble(),
                    lon = args.lon.toDouble(),
                    onCambiarCiudad = {
                        configuracionLocal.borrarCiudad()
                        navController.navigate(Ruta.Ciudades) {
                            popUpTo(Ruta.Clima::class) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}