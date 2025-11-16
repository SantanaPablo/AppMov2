package com.istea.appclima5.vista.ciudad

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CiudadVista(
    viewModel: CiudadViewModel,
    onCiudadSeleccionada: (String, Double, Double, String) -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var textoBusqueda by remember { mutableStateOf("") }
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.obtenerUbicacionActual(context) { lat, lon ->
                viewModel.ejecutar(CiudadIntencion.BuscarCiudad("${lat},${lon}"))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar Ciudad") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Buscador
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = {
                    textoBusqueda = it
                    viewModel.ejecutar(CiudadIntencion.ActualizarTexto(it))
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar ciudad") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            viewModel.ejecutar(CiudadIntencion.BuscarPorGeolocalizacion)
                        }
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Geolocalización")
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.ejecutar(CiudadIntencion.BuscarCiudad(textoBusqueda)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = textoBusqueda.isNotBlank()
            ) {
                Text("Buscar")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de ciudades
            when (val estadoActual = estado) {
                is CiudadEstado.Vacio -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Busca una ciudad para comenzar")
                    }
                }
                is CiudadEstado.Cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is CiudadEstado.Exitoso -> {
                    if (estadoActual.ciudades.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No se encontraron ciudades")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(estadoActual.ciudades) { ciudad ->
                                CiudadItem(
                                    ciudad = ciudad,
                                    onClick = {
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
                                )
                            }
                        }
                    }
                }
                is CiudadEstado.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = estadoActual.mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CiudadItem(ciudad: Ciudad, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = ciudad.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${ciudad.state ?: ""} ${ciudad.country}".trim(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Lat: ${ciudad.lat}, Lon: ${ciudad.lon}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}