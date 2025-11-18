package com.istea.appclima5.vista.ciudad

import com.istea.appclima5.repository.Repositorio
import com.istea.appclima5.repository.data.local.CiudadGuardada
import com.istea.appclima5.repository.data.local.IConfiguracionLocal
import com.istea.appclima5.vista.ciudades.CiudadEstado
import com.istea.appclima5.vista.ciudades.CiudadIntencion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CiudadViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repositorio: Repositorio
    private lateinit var configFake: FakeConfiguracionLocal
    private lateinit var viewModel: CiudadViewModel

    class FakeConfiguracionLocal : IConfiguracionLocal {
        var ciudadGuardada: CiudadGuardada? = null
        var simularErrorAlGuardar: Boolean = false

        override fun guardarCiudad(nombre: String, lat: Double, lon: Double, country: String) {
            if (simularErrorAlGuardar) {
                throw Exception("Error de escritura en disco simulado")
            }
            ciudadGuardada = CiudadGuardada(nombre, lat, lon, country)
        }

        override fun obtenerCiudadGuardada(): CiudadGuardada? = ciudadGuardada
        override fun borrarCiudad() { ciudadGuardada = null }
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repositorio = Repositorio(usarMock = true)
        configFake = FakeConfiguracionLocal()
        viewModel = CiudadViewModel(repositorio, configFake)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_cargaSugerenciasIniciales() {
        val estado = viewModel.estado.value
        assertTrue(estado is CiudadEstado.Exitoso)
        val exitoso = estado as CiudadEstado.Exitoso
        assertTrue(exitoso.ciudades.isNotEmpty())
        assertEquals("Buenos Aires", exitoso.ciudades.first().name)
    }

    @Test
    fun buscarCiudad_busquedaValidaActualizaEstadoExitoso() = runTest {
        val busqueda = "Cordoba"
        viewModel.ejecutar(CiudadIntencion.BuscarCiudad(busqueda))
        val estado = viewModel.estado.value
        assertTrue(estado is CiudadEstado.Exitoso)
        val exitoso = estado as CiudadEstado.Exitoso
        assertEquals("Cordoba", exitoso.ciudades.first().name)
        assertEquals(busqueda, exitoso.textoBusqueda)
    }

    @Test
    fun buscarCiudad_textoVacioCargaSugerencias() = runTest {
        viewModel.ejecutar(CiudadIntencion.BuscarCiudad("Cordoba"))
        viewModel.ejecutar(CiudadIntencion.BuscarCiudad(""))
        val estado = viewModel.estado.value as CiudadEstado.Exitoso
        val primeraCiudad = estado.ciudades.first().name
        assertEquals("Buenos Aires", primeraCiudad)
    }

    @Test
    fun buscarCiudad_nombreErrorDevuelveEstadoError() = runTest {
        viewModel.ejecutar(CiudadIntencion.BuscarCiudad("error"))
        val estado = viewModel.estado.value
        assertTrue(estado is CiudadEstado.Error)
        val errorState = estado as CiudadEstado.Error
        assertTrue(errorState.mensaje.contains("Error simulado"))
    }

    @Test
    fun buscarPorGeolocalizacion_devuelveCiudadMock() = runTest {
        viewModel.ejecutar(CiudadIntencion.BuscarPorGeolocalizacion(-34.0, -58.0))
        val estado = viewModel.estado.value
        assertTrue(estado is CiudadEstado.Exitoso)
        val exitoso = estado as CiudadEstado.Exitoso
        assertEquals("Ubicación Mock (BsAs)", exitoso.ciudades.first().name)
    }

    @Test
    fun actualizarTexto_modificaTextoBusqueda() {
        val estadoInicial = viewModel.estado.value as CiudadEstado.Exitoso
        val listaOriginal = estadoInicial.ciudades
        viewModel.ejecutar(CiudadIntencion.ActualizarTexto("Escribiendo..."))
        val estadoNuevo = viewModel.estado.value as CiudadEstado.Exitoso
        assertEquals("Escribiendo...", estadoNuevo.textoBusqueda)
        assertEquals(listaOriginal, estadoNuevo.ciudades)
    }

    @Test
    fun seleccionarCiudad_guardaEnConfiguracion() {
        viewModel.ejecutar(CiudadIntencion.SeleccionarCiudad("Madrid", 40.0, -3.0, "España"))
        assertNotNull(configFake.ciudadGuardada)
        assertEquals("Madrid", configFake.ciudadGuardada?.nombre)
    }

    @Test
    fun seleccionarCiudad_errorAlGuardarDevuelveEstadoError() {
        configFake.simularErrorAlGuardar = true
        viewModel.ejecutar(CiudadIntencion.SeleccionarCiudad("Madrid", 40.0, -3.0, "España"))
        val estado = viewModel.estado.value
        assertTrue(estado is CiudadEstado.Error)
        assertEquals("Error al guardar ciudad seleccionada", (estado as CiudadEstado.Error).mensaje)
    }
}