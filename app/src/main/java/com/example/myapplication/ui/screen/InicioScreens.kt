package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.UsuarioEntity
import com.example.myapplication.domain.Alerta
import com.example.myapplication.navigation.Rutas
import com.example.myapplication.ui.components.Insignia
import com.example.myapplication.ui.components.PantallaPrincipal
import com.example.myapplication.ui.components.TarjetaInfo
import com.example.myapplication.viewmodel.HomeViewModel

@Composable
fun InicioScreen(
    homeVm: HomeViewModel,
    usuario: UsuarioEntity?,
    rutaActual: String,
    onNavegar: (String) -> Unit,
    onNuevaOportunidad: () -> Unit
) {
    val indicadores by homeVm.indicadores.collectAsState()
    val alertas by homeVm.alertas.collectAsState()

    PantallaPrincipal(titulo = "Inicio", rutaActual = rutaActual, onNavegar = onNavegar) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Hola, ${usuario?.nombre ?: "usuario"}", style = MaterialTheme.typography.headlineSmall)
            Text("Indicadores con datos ficticios.", style = MaterialTheme.typography.bodyMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                IndicadorCard("Total", indicadores.total, Modifier.weight(1f))
                IndicadorCard("Activas", indicadores.activas, Modifier.weight(1f))
                IndicadorCard("Confirmadas", indicadores.confirmadas, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                IndicadorCard("En propuesta", indicadores.propuestasEnviadas, Modifier.weight(1f))
                IndicadorCard("Descartadas", indicadores.descartadas, Modifier.weight(1f))
                IndicadorCard("Alertas", alertas.size, Modifier.weight(1f))
            }

            TarjetaInfo("Oportunidades por estado") {
                val filas = indicadores.porEstado.entries.sortedByDescending { it.value }
                if (filas.isEmpty()) Text("Sin oportunidades registradas.")
                filas.forEach { (estado, cantidad) -> Text("${estado.etiqueta}: $cantidad") }
            }

            Button(onClick = onNuevaOportunidad, modifier = Modifier.fillMaxWidth()) {
                Text("Registrar nueva oportunidad")
            }
            OutlinedButton(onClick = { onNavegar(Rutas.ALERTAS) }, modifier = Modifier.fillMaxWidth()) {
                Text("Ver alertas (${alertas.size})")
            }

            TarjetaInfo("Catálogos") {
                OutlinedButton(onClick = { onNavegar(Rutas.EVENTOS) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Eventos")
                }
                OutlinedButton(onClick = { onNavegar(Rutas.ORGANIZACIONES) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Organizaciones")
                }
                OutlinedButton(onClick = { onNavegar(Rutas.CONTACTOS) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Contactos")
                }
            }
        }
    }
}

@Composable
private fun IndicadorCard(titulo: String, valor: Int, modifier: Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelMedium)
            Text("$valor", style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
fun AlertasScreen(
    homeVm: HomeViewModel,
    rutaActual: String,
    onNavegar: (String) -> Unit,
    onAbrir: (Long) -> Unit
) {
    val alertas by homeVm.alertas.collectAsState()

    PantallaPrincipal(titulo = "Alertas", rutaActual = rutaActual, onNavegar = onNavegar) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Recordatorios de eventos próximos (14 días), seguimientos y propuestas.",
                style = MaterialTheme.typography.bodySmall
            )
            if (alertas.isEmpty()) {
                Text("Sin alertas. Todo está al día.", style = MaterialTheme.typography.bodyLarge)
            }
            alertas.forEach { alerta -> TarjetaAlerta(alerta, onAbrir) }
        }
    }
}

@Composable
private fun TarjetaAlerta(alerta: Alerta, onAbrir: (Long) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { alerta.oportunidadId?.let(onAbrir) }
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Insignia(alerta.tipo.etiqueta)
            Text(alerta.titulo, style = MaterialTheme.typography.titleSmall)
            Text(alerta.detalle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun PerfilScreen(
    usuario: UsuarioEntity?,
    rutaActual: String,
    onNavegar: (String) -> Unit,
    onCerrarSesion: () -> Unit
) {
    PantallaPrincipal(titulo = "Perfil", rutaActual = rutaActual, onNavegar = onNavegar) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (usuario == null) {
                Text("Cargando perfil…")
            } else {
                TarjetaInfo("Cuenta ficticia") {
                    Text("Identificador: ${usuario.id}")
                    Text("Nombre: ${usuario.nombre}")
                    Text("Correo: ${usuario.correo}")
                    Text("Perfil: ${usuario.perfil.etiqueta}")
                    Text("Área: ${usuario.area}")
                    Text("Zona: ${usuario.zona}")
                    Text("Estado: ${if (usuario.activo) "Activa" else "Inactiva"}")
                    Text("Creada: ${usuario.fechaCreacion}")
                }
            }
            Text(
                "Todos los datos de esta aplicación son ficticios o públicos. No ingrese datos personales reales.",
                style = MaterialTheme.typography.bodySmall
            )
            Button(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar sesión")
            }
        }
    }
}
