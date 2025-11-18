package com.istea.appclima5.vista.clima

import com.istea.appclima5.repository.Repositorio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClimaViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repositorio: Repositorio
    private lateinit var viewModel: ClimaViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repositorio = Repositorio(usarMock = true)
        viewModel = ClimaViewModel(repositorio)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun cargarClima_actualizaEstadoExitoso() = runTest {
        viewModel.ejecutar(ClimaIntencion.CargarClima("Buenos Aires", -34.6, -58.3))

        val estado = viewModel.estado.value
        assertTrue(estado is ClimaEstado.Exitoso)

        val exitoso = estado as ClimaEstado.Exitoso
        assertEquals("Buenos Aires", exitoso.nombreCiudad)
        assertNotNull(exitoso.clima)
        assertTrue(exitoso.pronostico.isNotEmpty())
    }

    @Test
    fun cambiarCiudad_emiteEventoCambiarCiudad() = runTest {
        val eventos = mutableListOf<ClimaEventoUi>()
        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.eventosUi.collect { eventos.add(it) }
        }

        viewModel.ejecutar(ClimaIntencion.CambiarCiudad)

        assertTrue(eventos.any { it is ClimaEventoUi.CambiarCiudad })
        job.cancel()
    }

    @Test
    fun compartir_emiteEventoCompartirTexto() = runTest {
        viewModel.ejecutar(ClimaIntencion.CargarClima("Madrid", 40.4, -3.7))

        val eventos = mutableListOf<ClimaEventoUi>()
        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.eventosUi.collect { eventos.add(it) }
        }

        viewModel.ejecutar(ClimaIntencion.Compartir)

        assertTrue(eventos.any { it is ClimaEventoUi.CompartirTexto })

        val evento = eventos.find { it is ClimaEventoUi.CompartirTexto } as ClimaEventoUi.CompartirTexto
        assertTrue(evento.texto.contains("Madrid"))
        assertTrue(evento.texto.contains("AppDelClima"))

        job.cancel()
    }
}