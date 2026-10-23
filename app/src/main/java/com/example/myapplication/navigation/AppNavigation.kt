package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.ui.screen.AlertasScreen
import com.example.myapplication.ui.screen.ContactoFormScreen
import com.example.myapplication.ui.screen.ContactosScreen
import com.example.myapplication.ui.screen.EventoFormScreen
import com.example.myapplication.ui.screen.EventosScreen
import com.example.myapplication.ui.screen.InicioScreen
import com.example.myapplication.ui.screen.LoginScreen
import com.example.myapplication.ui.screen.OportunidadDetalleScreen
import com.example.myapplication.ui.screen.OportunidadFormScreen
import com.example.myapplication.ui.screen.OportunidadesScreen
import com.example.myapplication.ui.screen.OrganizacionFormScreen
import com.example.myapplication.ui.screen.OrganizacionesScreen
import com.example.myapplication.ui.screen.PerfilScreen
import com.example.myapplication.ui.screen.QrScannerScreen
import com.example.myapplication.ui.screen.RegistroScreen
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.CatalogoViewModel
import com.example.myapplication.viewmodel.ContactoFormViewModel
import com.example.myapplication.viewmodel.EventoFormViewModel
import com.example.myapplication.viewmodel.Fabrica
import com.example.myapplication.viewmodel.HomeViewModel
import com.example.myapplication.viewmodel.OportunidadDetalleViewModel
import com.example.myapplication.viewmodel.OportunidadFormViewModel
import com.example.myapplication.viewmodel.OportunidadViewModel
import com.example.myapplication.viewmodel.OrganizacionFormViewModel
import com.example.myapplication.viewmodel.QrViewModel
import com.example.myapplication.viewmodel.SesionViewModel

/**
 * Solo define el grafo de navegación. Cada pantalla obtiene su ViewModel:
 * los globales (sesión, listas, catálogos) viven con la actividad; los de formulario
 * o detalle viven con su entrada de navegación y se liberan al salir de la pantalla.
 */
@Composable
fun AppNavigation(fabrica: Fabrica) {
    val sesion: SesionViewModel = viewModel(factory = fabrica.vm { SesionViewModel(it) })
    val authVm: AuthViewModel = viewModel(factory = fabrica.vm { AuthViewModel(it) })
    val homeVm: HomeViewModel = viewModel(factory = fabrica.vm { HomeViewModel(it) })
    val opVm: OportunidadViewModel = viewModel(factory = fabrica.vm { OportunidadViewModel(it) })
    val catVm: CatalogoViewModel = viewModel(factory = fabrica.vm { CatalogoViewModel(it) })

    val usuario by sesion.usuario.collectAsState()
    val nav = rememberNavController()

    fun ir(ruta: String) {
        nav.navigate(ruta) {
            popUpTo(Rutas.INICIO) { inclusive = false }
            launchSingleTop = true
        }
    }

    fun irAInicioTrasIngresar() {
        nav.navigate(Rutas.INICIO) {
            popUpTo(Rutas.LOGIN) { inclusive = true }
        }
    }

    NavHost(
        navController = nav,
        startDestination = if (sesion.sesionIniciada) Rutas.INICIO else Rutas.LOGIN
    ) {
        composable(Rutas.LOGIN) {
            val ui by authVm.ui.collectAsState()
            LoginScreen(
                ui = ui,
                onEditar = authVm::editar,
                onIngresar = { authVm.iniciarSesion { irAInicioTrasIngresar() } },
                onIrRegistro = {
                    authVm.limpiarErrores()
                    nav.navigate(Rutas.REGISTRO)
                }
            )
        }
        composable(Rutas.REGISTRO) {
            val ui by authVm.ui.collectAsState()
            RegistroScreen(
                ui = ui,
                onEditar = authVm::editar,
                onRegistrar = { authVm.registrar { irAInicioTrasIngresar() } },
                onAtras = {
                    authVm.limpiarErrores()
                    nav.popBackStack()
                }
            )
        }

        composable(Rutas.INICIO) {
            InicioScreen(
                homeVm = homeVm,
                usuario = usuario,
                rutaActual = Rutas.INICIO,
                onNavegar = ::ir,
                onNuevaOportunidad = { nav.navigate(Rutas.formularioOportunidad(0)) }
            )
        }
        composable(Rutas.OPORTUNIDADES) {
            OportunidadesScreen(
                opVm = opVm,
                catVm = catVm,
                rutaActual = Rutas.OPORTUNIDADES,
                onNavegar = ::ir,
                onAbrir = { nav.navigate(Rutas.oportunidad(it)) },
                onNueva = { nav.navigate(Rutas.formularioOportunidad(0)) }
            )
        }
        composable(
            Rutas.OPORTUNIDAD,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entrada ->
            val id = entrada.arguments?.getLong("id") ?: 0L
            val detalleVm: OportunidadDetalleViewModel = viewModel(
                viewModelStoreOwner = entrada,
                factory = fabrica.vm { OportunidadDetalleViewModel(it, id) }
            )
            OportunidadDetalleScreen(
                vm = detalleVm,
                catVm = catVm,
                onAtras = { nav.popBackStack() },
                onEditar = { nav.navigate(Rutas.formularioOportunidad(it)) }
            )
        }
        composable(
            Rutas.FORMULARIO_OPORTUNIDAD,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entrada ->
            val id = entrada.arguments?.getLong("id") ?: 0L
            val formVm: OportunidadFormViewModel = viewModel(
                viewModelStoreOwner = entrada,
                factory = fabrica.vm { OportunidadFormViewModel(it, id) }
            )
            // El escáner devuelve su resultado a través de esta entrada
            val qrContent by entrada.savedStateHandle
                .getStateFlow<String?>("qr_content", null)
                .collectAsState()
            OportunidadFormScreen(
                id = id,
                vm = formVm,
                qrContent = qrContent,
                onQrContentConsumed = { entrada.savedStateHandle["qr_content"] = null },
                onGuardado = { nuevoId ->
                    nav.navigate(Rutas.oportunidad(nuevoId)) {
                        popUpTo(Rutas.FORMULARIO_OPORTUNIDAD) { inclusive = true }
                    }
                },
                onVolver = { nav.popBackStack() },
                onAbrirEscaner = { nav.navigate(Rutas.QR) }
            )
        }

        composable(Rutas.EVENTOS) {
            EventosScreen(
                catVm = catVm,
                onAtras = { nav.popBackStack() },
                onNuevo = { nav.navigate(Rutas.EVENTO_NUEVO) }
            )
        }
        composable(Rutas.EVENTO_NUEVO) { entrada ->
            val vm: EventoFormViewModel = viewModel(
                viewModelStoreOwner = entrada,
                factory = fabrica.vm { EventoFormViewModel(it) }
            )
            EventoFormScreen(
                vm = vm,
                onGuardado = { nav.popBackStack() },
                onAtras = { nav.popBackStack() }
            )
        }
        composable(Rutas.ORGANIZACIONES) {
            OrganizacionesScreen(
                catVm = catVm,
                onAtras = { nav.popBackStack() },
                onNuevo = { nav.navigate(Rutas.ORGANIZACION_NUEVA) }
            )
        }
        composable(Rutas.ORGANIZACION_NUEVA) { entrada ->
            val vm: OrganizacionFormViewModel = viewModel(
                viewModelStoreOwner = entrada,
                factory = fabrica.vm { OrganizacionFormViewModel(it) }
            )
            OrganizacionFormScreen(
                vm = vm,
                onGuardado = { nav.popBackStack() },
                onAtras = { nav.popBackStack() }
            )
        }
        composable(Rutas.CONTACTOS) {
            ContactosScreen(
                catVm = catVm,
                onAtras = { nav.popBackStack() },
                onNuevo = { nav.navigate(Rutas.CONTACTO_NUEVO) }
            )
        }
        composable(Rutas.CONTACTO_NUEVO) { entrada ->
            val vm: ContactoFormViewModel = viewModel(
                viewModelStoreOwner = entrada,
                factory = fabrica.vm { ContactoFormViewModel(it) }
            )
            ContactoFormScreen(
                vm = vm,
                onGuardado = { nav.popBackStack() },
                onAtras = { nav.popBackStack() }
            )
        }

        composable(Rutas.ALERTAS) {
            AlertasScreen(
                homeVm = homeVm,
                rutaActual = Rutas.ALERTAS,
                onNavegar = ::ir,
                onAbrir = { nav.navigate(Rutas.oportunidad(it)) }
            )
        }
        composable(Rutas.PERFIL) {
            PerfilScreen(
                usuario = usuario,
                rutaActual = Rutas.PERFIL,
                onNavegar = ::ir,
                onCerrarSesion = {
                    sesion.cerrarSesion {
                        nav.navigate(Rutas.LOGIN) {
                            popUpTo(nav.graph.id) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(Rutas.QR) { entrada ->
            val qrVm: QrViewModel = viewModel(viewModelStoreOwner = entrada)
            QrScannerScreen(
                vm = qrVm,
                onQrRead = { contenido ->
                    nav.previousBackStackEntry?.savedStateHandle?.set("qr_content", contenido)
                    nav.popBackStack()
                }
            )
        }
    }
}
