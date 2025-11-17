package com.istea.appclima5.vista

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.istea.appclima5.ViewModelFactory
import com.istea.appclima5.repository.data.local.ConfiguracionLocal
import com.istea.appclima5.router.Ruta
import com.istea.appclima5.vista.ciudad.CiudadViewModel
import com.istea.appclima5.vista.ciudad.CiudadVista
import com.istea.appclima5.vista.clima.ClimaViewModel
import com.istea.appclima5.vista.clima.ClimaVista

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