package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.EstadoOportunidad
import com.example.myapplication.data.OportunidadResumen
import com.example.myapplication.data.Prioridad
import com.example.myapplication.data.TipoOportunidad
import com.example.myapplication.data.UsuarioEntity
import com.example.myapplication.ui.components.CampoTexto
import com.example.myapplication.ui.components.Insignia
import com.example.myapplication.ui.components.PantallaPrincipal
import com.example.myapplication.ui.components.SelectorDesplegable
import com.example.myapplication.viewmodel.CatalogoViewModel
import com.example.myapplication.viewmodel.OportunidadViewModel

@Composable
fun OportunidadesScreen(
    opVm: OportunidadViewModel,
    catVm: CatalogoViewModel,
    rutaActual: String,
    onNavegar: (String) -> Unit,
    onAbrir: (Long) -> Unit,
    onNueva: () -> Unit
) {
    val lista by opVm.lista.collectAsState()
    val filtro by opVm.filtroActual.collectAsState()
    val usuarios by catVm.usuarios.collectAsState()

    PantallaPrincipal(
        titulo = "Oportunidades",
        rutaActual = rutaActual,
        onNavegar = onNavegar,
        accion = {
            ExtendedFloatingActionButton(
                onClick = onNueva,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nueva") }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CampoTexto(
                etiqueta = "Buscar por nombre u organización",
                valor = filtro.texto,
                onCambio = { t -> opVm.cambiarFiltro { it.copy(texto = t) } }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                CampoTexto(
                    etiqueta = "Detectada desde (AAAA-MM-DD)",
                    valor = filtro.desde ?: "",
                    onCambio = { t -> opVm.cambiarFiltro { it.copy(desde = t.takeIf { v -> v.isNotBlank() }) } },
                    modifier = Modifier.weight(1f)
                )
                CampoTexto(
                    etiqueta = "Hasta (AAAA-MM-DD)",
                    valor = filtro.hasta ?: "",
                    onCambio = { t -> opVm.cambiarFiltro { it.copy(hasta = t.takeIf { v -> v.isNotBlank() }) } },
                    modifier = Modifier.weight(1f)
                )
            }
            CampoTexto(
                etiqueta = "Comuna o región del evento",
                valor = filtro.comuna ?: "",
                onCambio = { t -> opVm.cambiarFiltro { it.copy(comuna = t.takeIf { v -> v.isNotBlank() }) } }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SelectorDesplegable(
                    etiqueta = "Estado",
                    opciones = listOf<EstadoOportunidad?>(null) + EstadoOportunidad.entries,
                    seleccionado = filtro.estado,
                    etiquetaDe = { it?.etiqueta ?: "Todos" },
                    onSeleccion = { e -> opVm.cambiarFiltro { it.copy(estado = e) } },
                    modifier = Modifier.weight(1f),
                    textoVacio = "Todos"
                )
                SelectorDesplegable(
                    etiqueta = "Prioridad",
                    opciones = listOf<Prioridad?>(null) + Prioridad.entries,
                    seleccionado = filtro.prioridad,
                    etiquetaDe = { it?.etiqueta ?: "Todas" },
                    onSeleccion = { p -> opVm.cambiarFiltro { it.copy(prioridad = p) } },
                    modifier = Modifier.weight(1f),
                    textoVacio = "Todas"
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SelectorDesplegable(
                    etiqueta = "Tipo",
                    opciones = listOf<TipoOportunidad?>(null) + TipoOportunidad.entries,
                    seleccionado = filtro.tipo,
                    etiquetaDe = { it?.etiqueta ?: "Todos" },
                    onSeleccion = { t -> opVm.cambiarFiltro { it.copy(tipo = t) } },
                    modifier = Modifier.weight(1f),
                    textoVacio = "Todos"
                )
                SelectorDesplegable<UsuarioEntity?>(
                    etiqueta = "Responsable",
                    opciones = listOf<UsuarioEntity?>(null) + usuarios,
                    seleccionado = usuarios.find { it.id == filtro.responsableId },
                    etiquetaDe = { it?.nombre ?: "Todos" },
                    onSeleccion = { u -> opVm.cambiarFiltro { it.copy(responsableId = u?.id) } },
                    modifier = Modifier.weight(1f),
                    textoVacio = "Todos"
                )
            }
            Text("${lista.size} resultado(s)", style = MaterialTheme.typography.bodySmall)

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                if (lista.isEmpty()) {
                    item { Text("No hay oportunidades con esos filtros.") }
                }
                items(lista, key = { it.id }) { oportunidad ->
                    TarjetaOportunidad(oportunidad) { onAbrir(oportunidad.id) }
                }
            }
        }
    }
}

@Composable
private fun TarjetaOportunidad(o: OportunidadResumen, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(o.nombre, style = MaterialTheme.typography.titleMedium)
            Text(
                "${o.tipo.etiqueta} · ${o.eventoNombre ?: "Sin evento"}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "${o.organizacionNombre ?: "Sin organización"} · ${o.eventoComuna ?: "-"}",
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Insignia(o.estado.etiqueta)
                Insignia(
                    o.prioridad.etiqueta,
                    color = if (o.prioridad == Prioridad.ALTA) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    }
                )
            }
            Text(
                "Responsable: ${o.responsableNombre ?: "Sin asignar"} · Detectada ${o.fechaDeteccion}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
