# AppMov2
Aplicaciones Móviles Segundo Parcial
Julieta Iturriaga, Pablo Britez Santana, Alejandro D’Amen, Mercedes Carabajal, Gastón Contreras

# AppClima5

## 1. Capa de Datos y Dominio (Repository)

En package Repository se encuentran los modelos de Dominio, Configuración local y  el origen de los datos (sea Local o por API) 

**Dominio:** Se definen los modelos de Dominio (`Ciudad`, `Clima`, `Pronostico`)
**Data (El "Switcher"):** La clase `Repositorio` funciona como un interruptor inteligente:
    * Recibe parámetro `usarMock: Boolean`.
    * **Si es `true`:** Utiliza `RepositorioMock`, que devuelve datos fijos (ideal para pruebas sin la api).
    * **Si es `false`:** Utiliza `RepositorioApi`, que se conecta a **OpenWeatherMap** usando **Ktor Client** para traer datos reales.

## 2. Router

**`Router.kt`:** Define las rutas como objetos o clases selladas (`Sealed Classes`).
    * `Ruta.Ciudades`: Un objeto simple para la pantalla de inicio.
    * `Ruta.Clima`: Una `data class` que obliga a pasar los argumentos necesarios (`nombre`, `lat`, `lon`, `country`).
Es imposible navegar a la pantalla de clima sin enviarle coordenadas válidas.

## 3. Arquitectura de la Vista

Cada pantalla sigue un flujo de datos unidireccional dividido en 4 componentes:

**Vista (Composable):** Solo es la parte del front y envía intenciones.
**Intención (`...Intencion`):** Representa las acciones del usuario. El usuario no "llama funciones", sino que "emite intenciones".
    El usuario emite una intención por ejemplo BuscarCiudad
**ViewModel:** Es el cerebro. Recibe la **Intención**, procesa la lógica (llamando al Repositorio) y actualiza el **Estado** a través de un flujo de estados.
**Estado (`...Estado`):** Representa la posición exacta de la UI en un momento dado. Es una clase sellada con:
    * `Vacio` (Inicial).
    * `Cargando` (Spinner).
    * `Exitoso` (Lista de datos).
    * `Error` (Mensaje de fallo).


## 4. AppNavegacion

Este componente (`AppNavegacion.kt`) sirve para la navegación en la interfaz gráfica.

Contiene el `NavHost` de Compose.
Interpreta las rutas definidas en el `Router`.
**Inyección de Dependencias:** Se utiliza el `ViewModelFactory` para crear e inyectar los ViewModels (`CiudadViewModel` y `ClimaViewModel`) en sus respectivas pantallas.
Maneja la lógica de transición entre `CiudadVista` y `ClimaVista`.

## 5. MainPage y Testing

### Refactorización: MainPage
Se mueve la lógica de inicio desde `MainActivity` hacia un Composable llamado `MainPage`.
**Responsabilidad:** Inicializar las dependencias globales (`Repositorio`, `ConfiguracionLocal`) usando `remember` para que sobrevivan a recomposiciones.
**Decisión de Inicio:** Verifica si hay una ciudad guardada en preferencias para decidir si arranca en la lista de ciudades o directamente en el clima.

### Estrategia de Testing: "Fake" vs "Mock"
Para los Unit Tests, el problema era que `ConfiguracionLocal` utiliza `SharedPreferences`, lo cual requiere el `Context` de Android no disponible en tests unitarios.

Creamos la interfaz IConfiguracionLocal (en Repository), CiudadViewModel depende de esta interfaz y no de la clase concreta (También ConfiguracionLocal implementa IConfiguracionLocal)
**En el Test:** Creamos una clase FakeConfiguracionLocal (IConfiguracionLocal)
Esta clase simplemente guarda los datos en una variable en memoria y testea los métodos en los ViewModels