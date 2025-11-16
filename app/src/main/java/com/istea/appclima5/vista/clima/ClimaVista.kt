package com.istea.appclima5.vista.clima

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.istea.appclima5.repository.domain.model.Clima
import com.istea.appclima5.repository.domain.model.ListForecast
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClimaVista(
    viewModel: ClimaViewModel,
    nombre: String,
    lat: Double,
    lon: Double,
    onCambiarCiudad: () -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.ejecutar(ClimaIntencion.CargarClima(nombre, lat, lon))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clima") },
                actions = {
                    IconButton(onClick = onCambiarCiudad) { //este boton pueden agregarlo donde quieran y cambiarle el icono si quieren
                        Icon(Icons.Default.LocationOn, contentDescription = "Cambiar ciudad")
                    }
                }
            )
        }
    ) { padding ->
        when (val estadoActual = estado) {
            is ClimaEstado.Cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ClimaEstado.Exitoso -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Detalle del clima actual
                    DetalleClimaHoy(clima = estadoActual.clima, nombre = estadoActual.nombreCiudad)

                    // Boton compartir
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, viewModel.obtenerTextoCompartir())
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartir clima"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compartir")
                    }

                    Divider()

                    // pronostico
                    Text(
                        text = "Pronóstico próximos días",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    PronosticoGrafico(pronostico = estadoActual.pronostico)
                }
            }
            is ClimaEstado.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
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

@Composable
fun DetalleClimaHoy(clima: Clima, nombre: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${clima.main.temp.toInt()}°C",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = clima.weather.firstOrNull()?.description?.replaceFirstChar {
                    it.uppercase()
                } ?: "Desconocido",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DetalleItem("Sensación", "${clima.main.feels_like.toInt()}°C")
                DetalleItem("Humedad", "${clima.main.humidity}%")
                DetalleItem("Viento", "${clima.wind.speed} m/s")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DetalleItem("Mín", "${clima.main.temp_min.toInt()}°C")
                DetalleItem("Máx", "${clima.main.temp_max.toInt()}°C")
                DetalleItem("Presión", "${clima.main.pressure} hPa")
            }
        }
    }
}

@Composable
fun DetalleItem(label: String, valor: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PronosticoGrafico(pronostico: List<ListForecast>) {
    val pronosticoPorDia = pronostico.groupBy { forecast ->
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(forecast.dt * 1000))
    }.map { (fecha, forecasts) ->
        val tempMax = forecasts.maxOf { it.main.temp_max }
        val tempMin = forecasts.minOf { it.main.temp_min }
        val descripcion = forecasts.first().weather.firstOrNull()?.description ?: ""
        Triple(fecha, tempMax, tempMin) to descripcion
    }.take(5)

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(pronosticoPorDia) { (temps, descripcion) ->
            val (fecha, tempMax, tempMin) = temps
            PronosticoDiaCard(fecha, tempMax, tempMin, descripcion)
        }
    }
}

@Composable
fun PronosticoDiaCard(fecha: String, tempMax: Double, tempMin: Double, descripcion: String) {
    Card(
        modifier = Modifier.width(120.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatoSalida = SimpleDateFormat("dd/MM", Locale.getDefault())
            val fechaFormateada = try {
                formatoSalida.format(formatoEntrada.parse(fecha) ?: Date())
            } catch (e: Exception) {
                fecha
            }

            Text(
                text = fechaFormateada,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = descripcion.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${tempMax.toInt()}°",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Text(
                text = "${tempMin.toInt()}°",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}