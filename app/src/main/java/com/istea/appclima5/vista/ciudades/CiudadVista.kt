package com.istea.appclima5.vista.ciudades

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.istea.appclima5.repository.domain.model.Ciudad
import com.istea.appclima5.vista.ciudad.CiudadViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CiudadVista(
    viewModel: CiudadViewModel,
    onCiudadSeleccionada: (String, Double, Double, String) -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var texto by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(texto) {
        delay(350)
        viewModel.ejecutar(CiudadIntencion.BuscarCiudad(texto))
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.obtenerUbicacionActual(context) { lat, lon ->
                viewModel.ejecutar(CiudadIntencion.BuscarPorGeolocalizacion(lat, lon))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Seleccionar Ciudad") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            OutlinedTextField(
                value = texto,
                onValueChange = {
                    texto = it
                    viewModel.ejecutar(CiudadIntencion.ActualizarTexto(it))
                },
                label = { Text("Buscar ciudad") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            locationPermissionLauncher.launch(
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        }
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            when (val e = estado) {
                CiudadEstado.Vacio -> EstadoTexto("Busca una ciudad para comenzar")
                CiudadEstado.Cargando -> EstadoCargando()
                is CiudadEstado.Error -> EstadoTexto(e.mensaje, esError = true)

                is CiudadEstado.Exitoso -> {
                    if (e.ciudades.isEmpty()) {
                        EstadoTexto("No se encontraron ciudades")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(e.ciudades) { ciudad ->
                                CiudadItem(ciudad) {
                                    viewModel.ejecutar(
                                        CiudadIntencion.SeleccionarCiudad(
                                            ciudad.name,
                                            ciudad.lat,
                                            ciudad.lon,
                                            ciudad.country
                                        )
                                    )
                                    onCiudadSeleccionada(
                                        ciudad.name,
                                        ciudad.lat,
                                        ciudad.lon,
                                        ciudad.country
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadoTexto(texto: String, esError: Boolean = false) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            color = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EstadoCargando() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun CiudadItem(ciudad: Ciudad, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(ciudad.name, style = MaterialTheme.typography.titleMedium)

            Text(
                "${ciudad.state ?: ""} ${ciudad.country}".trim(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                "Lat: ${ciudad.lat}, Lon: ${ciudad.lon}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
