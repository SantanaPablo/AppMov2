package com.istea.appclima5.vista.clima

import android.content.Intent
import androidx.compose.animation.animateContentSize
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.ejecutar(ClimaIntencion.CargarClima(nombre, lat, lon))
    }

    LaunchedEffect(Unit) {
        viewModel.eventosUi.collect { evento ->
            when (evento) {
                is ClimaEventoUi.CambiarCiudad -> onCambiarCiudad()

                is ClimaEventoUi.CompartirTexto -> {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, evento.texto)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Compartir clima"))
                }

                is ClimaEventoUi.MostrarMensaje -> {
                    snackbarHostState.showSnackbar(evento.mensaje)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Clima") },
                actions = {
                    IconButton(onClick = { viewModel.ejecutar(ClimaIntencion.CambiarCiudad) }) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Cambiar ciudad")
                    }
                }
            )
        }
    ) { padding ->

        when (val estadoActual = estado) {

            ClimaEstado.Cargando -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ClimaEstado.Exitoso -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    DetalleClimaHoy(
                        clima = estadoActual.clima,
                        nombre = estadoActual.nombreCiudad
                    )

                    Button(
                        onClick = { viewModel.ejecutar(ClimaIntencion.Compartir) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Compartir")
                    }

                    Text(
                        "Pronóstico próximos días",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    PronosticoGrafico(estadoActual.pronostico)
                }
            }

            is ClimaEstado.Error -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        estadoActual.mensaje,
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
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = nombre,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "${clima.main.temp.toInt()}°C",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = clima.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() }
                    ?: "Desconocido",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(20.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DetalleItem("Sensación", "${clima.main.feels_like.toInt()}°C")
                DetalleItem("Humedad", "${clima.main.humidity}%")
                DetalleItem("Viento", "${clima.wind.speed} m/s")
            }

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DetalleItem("Min", "${clima.main.temp_min.toInt()}°C")
                DetalleItem("Max", "${clima.main.temp_max.toInt()}°C")
                DetalleItem("Presión", "${clima.main.pressure} hPa")
            }
        }
    }
}


@Composable
fun DetalleItem(label: String, valor: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
    val formatterEntrada = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val formatterSalida = DateTimeFormatter.ofPattern("dd/MM")

    val pronosticoPorDia = pronostico
        .groupBy { f ->
            Instant.ofEpochSecond(f.dt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }
        .map { (fecha, forecasts) ->
            val tempMax = forecasts.maxOf { it.main.temp_max }
            val tempMin = forecasts.minOf { it.main.temp_min }
            val descripcion = forecasts.first().weather.firstOrNull()?.description ?: ""
            Triple(fecha, tempMax, tempMin) to descripcion
        }
        .take(5)

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(pronosticoPorDia) { (temps, descripcion) ->
            val (fecha, tempMax, tempMin) = temps
            PronosticoDiaCard(
                fechaFormateada = fecha.format(formatterSalida),
                tempMax = tempMax,
                tempMin = tempMin,
                descripcion = descripcion
            )
        }
    }
}


@Composable
fun PronosticoDiaCard(
    fechaFormateada: String,
    tempMax: Double,
    tempMin: Double,
    descripcion: String
) {
    Card(
        modifier = Modifier.width(120.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = fechaFormateada,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = descripcion.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )

            Spacer(Modifier.height(8.dp))

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
