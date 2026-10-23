package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.ContactoEntity
import com.example.myapplication.data.EstadoOportunidad
import com.example.myapplication.data.Prioridad
import com.example.myapplication.data.TipoOportunidad
import com.example.myapplication.data.UsuarioEntity
import com.example.myapplication.ui.components.CampoTexto
import com.example.myapplication.ui.components.PantallaSecundaria
import com.example.myapplication.ui.components.SelectorDesplegable
import com.example.myapplication.viewmodel.OportunidadFormUi
import com.example.myapplication.viewmodel.OportunidadFormViewModel

/** id == 0 crea una oportunidad nueva; id > 0 modifica la existente. */
@Composable
fun OportunidadFormScreen(
    id: Long,
    vm: OportunidadFormViewModel,
    qrContent: String?,
    onQrContentConsumed: () -> Unit,
    onGuardado: (Long) -> Unit,
    onVolver: () -> Unit,
    onAbrirEscaner: () -> Unit
) {
    val ui by vm.ui.collectAsState()

    // Al volver del escáner, el ViewModel interpreta el QR; la vista solo le entrega el contenido.
    LaunchedEffect(qrContent) {
        if (!qrContent.isNullOrBlank()) {
            vm.importarQr(qrContent)
            onQrContentConsumed()
        }
    }

    PantallaSecundaria(
        titulo = if (id > 0) "Modificar oportunidad" else "Nueva oportunidad",
        onAtras = onVolver
    ) { padding ->
        if (ui.cargando) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            FormularioOportunidad(
                padding = padding,
                ui = ui,
                vm = vm,
                onAbrirEscaner = onAbrirEscaner,
                onGuardado = onGuardado
            )
        }
    }
}

@Composable
private fun FormularioOportunidad(
    padding: PaddingValues,
    ui: OportunidadFormUi,
    vm: OportunidadFormViewModel,
    onAbrirEscaner: () -> Unit,
    onGuardado: (Long) -> Unit
) {
    val d = ui.formulario.datos
    val errores = ui.formulario.errores
    val editar = vm::editar
    val eventoSeleccionado = ui.eventoSeleccionado

    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ui.mensajeQr?.let { mensaje ->
            Text(mensaje, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
        }
        CampoTexto("Nombre de la oportunidad", d.nombre, { v -> editar { it.copy(nombre = v) } }, error = errores["nombre"])
        SelectorDesplegable("Tipo", TipoOportunidad.entries, d.tipo, { it.etiqueta }, { t -> editar { it.copy(tipo = t) } })
        CampoTexto("Descripción", d.descripcion, { v -> editar { it.copy(descripcion = v) } }, lineas = 3)
        CampoTexto("Fuente de información (ficticia)", d.fuente, { v -> editar { it.copy(fuente = v) } })
        CampoTexto(
            "Fecha de detección (AAAA-MM-DD)", d.fechaDeteccion,
            { v -> editar { it.copy(fechaDeteccion = v) } }, error = errores["fechaDeteccion"]
        )
        SelectorDesplegable(
            etiqueta = "Evento asociado",
            opciones = ui.eventos,
            seleccionado = eventoSeleccionado,
            etiquetaDe = { it.nombre },
            onSeleccion = { evento -> editar { it.copy(eventoId = evento.id, contactoId = null) } },
            error = errores["evento"],
            textoVacio = "Seleccionar evento"
        )
        Text(
            "Organización: ${ui.organizaciones.find { it.id == eventoSeleccionado?.organizacionId }?.nombre ?: "se toma del evento"}",
            style = MaterialTheme.typography.bodySmall
        )
        SelectorDesplegable<ContactoEntity?>(
            etiqueta = "Contacto corporativo",
            opciones = listOf<ContactoEntity?>(null) + ui.contactosDisponibles,
            seleccionado = ui.contactos.find { it.id == d.contactoId },
            etiquetaDe = { it?.nombre ?: "Sin contacto" },
            onSeleccion = { c -> editar { it.copy(contactoId = c?.id) } }
        )
        SelectorDesplegable<UsuarioEntity?>(
            etiqueta = "Responsable interno",
            opciones = listOf<UsuarioEntity?>(null) + ui.usuarios,
            seleccionado = ui.usuarios.find { it.id == d.responsableId },
            etiquetaDe = { it?.nombre ?: "Sin asignar" },
            onSeleccion = { u -> editar { it.copy(responsableId = u?.id) } }
        )
        SelectorDesplegable("Prioridad", Prioridad.entries, d.prioridad, { it.etiqueta }, { p -> editar { it.copy(prioridad = p) } })
        SelectorDesplegable("Estado", EstadoOportunidad.entries, d.estado, { it.etiqueta }, { e -> editar { it.copy(estado = e) } })
        CampoTexto(
            "Potencial comercial (1 a 5)", d.potencial,
            { v -> editar { it.copy(potencial = v) } }, error = errores["potencial"], teclado = KeyboardType.Number
        )
        CampoTexto(
            "Afinidad con el público objetivo (1 a 5)", d.afinidad,
            { v -> editar { it.copy(afinidad = v) } }, error = errores["afinidad"], teclado = KeyboardType.Number
        )
        CampoTexto("Observaciones", d.observaciones, { v -> editar { it.copy(observaciones = v) } }, lineas = 3)

        OutlinedButton(onClick = onAbrirEscaner, modifier = Modifier.fillMaxWidth()) {
            Text("Escanear código QR")
        }

        Button(onClick = { vm.guardar(onGuardado) }, modifier = Modifier.fillMaxWidth()) {
            Text("Guardar oportunidad")
        }
    }
}
