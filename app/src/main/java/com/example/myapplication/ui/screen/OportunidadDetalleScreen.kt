package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.EstadoOportunidad
import com.example.myapplication.data.Prioridad
import com.example.myapplication.data.TipoAccion
import com.example.myapplication.data.UsuarioEntity
import com.example.myapplication.ui.components.CampoTexto
import com.example.myapplication.ui.components.PantallaSecundaria
import com.example.myapplication.ui.components.SelectorDesplegable
import com.example.myapplication.ui.components.TarjetaInfo
import com.example.myapplication.ui.components.pesos
import com.example.myapplication.viewmodel.CatalogoViewModel
import com.example.myapplication.viewmodel.DialogoDetalle
import com.example.myapplication.viewmodel.OportunidadDetalleViewModel

@Composable
fun OportunidadDetalleScreen(
    vm: OportunidadDetalleViewModel,
    catVm: CatalogoViewModel,
    onAtras: () -> Unit,
    onEditar: (Long) -> Unit
) {
    val oportunidad by vm.oportunidad.collectAsState()
    val seguimientos by vm.seguimientos.collectAsState()
    val propuestas by vm.propuestas.collectAsState()
    val historial by vm.historial.collectAsState()
    val resultado by vm.resultado.collectAsState()
    val dialogo by vm.dialogo.collectAsState()
    val eventos by catVm.eventos.collectAsState()
    val contactos by catVm.contactos.collectAsState()
    val usuarios by catVm.usuarios.collectAsState()

    PantallaSecundaria(titulo = "Detalle de oportunidad", onAtras = onAtras) { padding ->
        val actual = oportunidad
        if (actual == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@PantallaSecundaria
        }

        val evento = eventos.find { it.id == actual.eventoId }
        val contacto = contactos.find { it.id == actual.contactoId }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaInfo(actual.nombre) {
                Text("Tipo: ${actual.tipo.etiqueta}")
                Text("Descripción: ${actual.descripcion.ifBlank { "-" }}")
                Text("Fuente: ${actual.fuenteInformacion.ifBlank { "-" }}")
                Text("Detectada: ${actual.fechaDeteccion} · Última actualización: ${actual.fechaUltimaActualizacion}")
                Text("Evento: ${evento?.nombre ?: "-"} (${evento?.fechaInicio ?: "-"}, ${evento?.comuna ?: "-"})")
                Text("Contacto: ${contacto?.let { "${it.nombre} · ${it.cargo}" } ?: "Sin contacto"}")
                Text("Potencial: ${actual.potencialComercial}/5 · Afinidad con el público: ${actual.afinidadPublico}/5")
                Text("Observaciones: ${actual.observaciones.ifBlank { "-" }}")
                OutlinedButton(onClick = { onEditar(actual.id) }) { Text("Modificar datos") }
            }

            TarjetaInfo("Gestión") {
                SelectorDesplegable(
                    etiqueta = "Estado",
                    opciones = EstadoOportunidad.entries,
                    seleccionado = actual.estado,
                    etiquetaDe = { it.etiqueta },
                    onSeleccion = { vm.cambiarEstado(it) }
                )
                SelectorDesplegable(
                    etiqueta = "Prioridad",
                    opciones = Prioridad.entries,
                    seleccionado = actual.prioridad,
                    etiquetaDe = { it.etiqueta },
                    onSeleccion = { vm.cambiarPrioridad(it) }
                )
                SelectorDesplegable<UsuarioEntity?>(
                    etiqueta = "Responsable",
                    opciones = listOf<UsuarioEntity?>(null) + usuarios,
                    seleccionado = usuarios.find { it.id == actual.responsableId },
                    etiquetaDe = { it?.nombre ?: "Sin asignar" },
                    onSeleccion = { vm.asignarResponsable(it?.id) }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { vm.abrirSeguimiento() }, modifier = Modifier.weight(1f)) {
                    Text("Seguimiento")
                }
                Button(onClick = { vm.abrirPropuesta() }, modifier = Modifier.weight(1f)) {
                    Text("Propuesta")
                }
            }
            OutlinedButton(onClick = { vm.abrirResultado() }, modifier = Modifier.fillMaxWidth()) {
                Text("Registrar resultado de activación")
            }

            TarjetaInfo("Seguimientos (${seguimientos.size})") {
                if (seguimientos.isEmpty()) Text("Sin seguimientos registrados.")
                seguimientos.forEach { s ->
                    Text("${s.fecha} ${s.hora} · ${s.tipoAccion.etiqueta}", style = MaterialTheme.typography.labelLarge)
                    Text("Resultado: ${s.resultado}")
                    s.fechaProximoSeguimiento?.let {
                        Text("Próximo: $it · ${s.proximaAccion.ifBlank { "-" }}", style = MaterialTheme.typography.bodySmall)
                    }
                    HorizontalDivider()
                }
            }

            TarjetaInfo("Propuestas (${propuestas.size})") {
                if (propuestas.isEmpty()) Text("Sin propuestas enviadas.")
                propuestas.forEach { p ->
                    Text("Enviada ${p.fechaEnvio}: ${p.descripcion}")
                    Text("Costo referencial: ${pesos(p.costoReferencial)}", style = MaterialTheme.typography.bodySmall)
                    if (p.respondida) {
                        Text("Respuesta: ${p.respuesta}")
                    } else {
                        OutlinedButton(onClick = { vm.abrirRespuesta(p) }) { Text("Registrar respuesta") }
                    }
                    HorizontalDivider()
                }
            }

            TarjetaInfo("Resultado de la activación") {
                val r = resultado
                if (r == null) {
                    Text("Sin resultado registrado.")
                } else {
                    Text("Fecha: ${r.fechaRealizacion} · Estado final: ${r.estadoFinal}")
                    Text("Asistentes: ${r.cantidadAsistentes} · Contactos generados: ${r.contactosGenerados}")
                    Text("Personas interesadas: ${r.personasInteresadas} · Inscripciones: ${r.inscripciones}")
                    Text("Evaluación: ${r.evaluacion.ifBlank { "-" }}")
                    Text("Aprendizajes: ${r.aprendizajes.ifBlank { "-" }}")
                }
            }

            TarjetaInfo("Historial (${historial.size})") {
                if (historial.isEmpty()) Text("Sin movimientos.")
                historial.forEach { h ->
                    Text("${h.fecha} · ${h.descripcion}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        when (dialogo) {
            DialogoDetalle.Seguimiento -> DialogoSeguimiento(vm)
            DialogoDetalle.Propuesta -> DialogoPropuesta(vm)
            DialogoDetalle.Resultado -> DialogoResultado(vm)
            is DialogoDetalle.RespuestaPropuesta -> DialogoRespuesta(vm)
            null -> {}
        }
    }
}

@Composable
private fun DialogoSeguimiento(vm: OportunidadDetalleViewModel) {
    val formulario by vm.formSeguimiento.collectAsState()
    val d = formulario.datos
    val errores = formulario.errores
    AlertDialog(
        onDismissRequest = { vm.cerrarDialogo() },
        title = { Text("Registrar seguimiento") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SelectorDesplegable("Tipo de acción", TipoAccion.entries, d.tipo, { it.etiqueta }, { t -> vm.editarSeguimiento { it.copy(tipo = t) } })
                CampoTexto("Fecha (AAAA-MM-DD)", d.fecha, { v -> vm.editarSeguimiento { it.copy(fecha = v) } }, error = errores["fecha"])
                CampoTexto("Hora (HH:MM)", d.hora, { v -> vm.editarSeguimiento { it.copy(hora = v) } }, error = errores["hora"])
                CampoTexto("Resultado", d.resultado, { v -> vm.editarSeguimiento { it.copy(resultado = v) } }, error = errores["resultado"], lineas = 2)
                CampoTexto("Próxima acción", d.proximaAccion, { v -> vm.editarSeguimiento { it.copy(proximaAccion = v) } })
                CampoTexto(
                    "Fecha próximo seguimiento (AAAA-MM-DD)", d.fechaProximoSeguimiento,
                    { v -> vm.editarSeguimiento { it.copy(fechaProximoSeguimiento = v) } }, error = errores["fechaProximoSeguimiento"]
                )
                CampoTexto("Observaciones", d.observaciones, { v -> vm.editarSeguimiento { it.copy(observaciones = v) } }, lineas = 2)
            }
        },
        confirmButton = { TextButton(onClick = { vm.guardarSeguimiento() }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = { vm.cerrarDialogo() }) { Text("Cancelar") } }
    )
}

@Composable
private fun DialogoPropuesta(vm: OportunidadDetalleViewModel) {
    val formulario by vm.formPropuesta.collectAsState()
    val d = formulario.datos
    val errores = formulario.errores
    AlertDialog(
        onDismissRequest = { vm.cerrarDialogo() },
        title = { Text("Registrar propuesta enviada") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CampoTexto("Fecha de envío (AAAA-MM-DD)", d.fechaEnvio, { v -> vm.editarPropuesta { it.copy(fechaEnvio = v) } }, error = errores["fechaEnvio"])
                CampoTexto("Descripción de la propuesta", d.descripcion, { v -> vm.editarPropuesta { it.copy(descripcion = v) } }, error = errores["descripcion"], lineas = 2)
                CampoTexto("Condiciones de participación", d.condiciones, { v -> vm.editarPropuesta { it.copy(condiciones = v) } }, lineas = 2)
                CampoTexto("Costo referencial ficticio (pesos)", d.costo, { v -> vm.editarPropuesta { it.copy(costo = v) } }, error = errores["costo"], teclado = KeyboardType.Number)
            }
        },
        confirmButton = { TextButton(onClick = { vm.guardarPropuesta() }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = { vm.cerrarDialogo() }) { Text("Cancelar") } }
    )
}

@Composable
private fun DialogoResultado(vm: OportunidadDetalleViewModel) {
    val formulario by vm.formResultado.collectAsState()
    val d = formulario.datos
    val errores = formulario.errores
    AlertDialog(
        onDismissRequest = { vm.cerrarDialogo() },
        title = { Text("Registrar resultado ficticio") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CampoTexto("Fecha de realización (AAAA-MM-DD)", d.fechaRealizacion, { v -> vm.editarResultado { it.copy(fechaRealizacion = v) } }, error = errores["fechaRealizacion"])
                CampoTexto("Estado final", d.estadoFinal, { v -> vm.editarResultado { it.copy(estadoFinal = v) } }, error = errores["estadoFinal"])
                CampoTexto("Asistentes estimados", d.asistentes, { v -> vm.editarResultado { it.copy(asistentes = v) } }, error = errores["asistentes"], teclado = KeyboardType.Number)
                CampoTexto("Contactos generados", d.contactos, { v -> vm.editarResultado { it.copy(contactos = v) } }, error = errores["contactos"], teclado = KeyboardType.Number)
                CampoTexto("Personas interesadas", d.interesadas, { v -> vm.editarResultado { it.copy(interesadas = v) } }, error = errores["interesadas"], teclado = KeyboardType.Number)
                CampoTexto("Inscripciones", d.inscripciones, { v -> vm.editarResultado { it.copy(inscripciones = v) } }, error = errores["inscripciones"], teclado = KeyboardType.Number)
                CampoTexto("Observaciones del equipo", d.observaciones, { v -> vm.editarResultado { it.copy(observaciones = v) } }, lineas = 2)
                CampoTexto("Evaluación general", d.evaluacion, { v -> vm.editarResultado { it.copy(evaluacion = v) } }, lineas = 2)
                CampoTexto("Aprendizajes para futuras activaciones", d.aprendizajes, { v -> vm.editarResultado { it.copy(aprendizajes = v) } }, lineas = 2)
            }
        },
        confirmButton = { TextButton(onClick = { vm.guardarResultado() }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = { vm.cerrarDialogo() }) { Text("Cancelar") } }
    )
}

@Composable
private fun DialogoRespuesta(vm: OportunidadDetalleViewModel) {
    val formulario by vm.formRespuesta.collectAsState()
    AlertDialog(
        onDismissRequest = { vm.cerrarDialogo() },
        title = { Text("Respuesta a la propuesta") },
        text = {
            CampoTexto(
                "Respuesta recibida (ficticia)", formulario.datos,
                { vm.editarRespuesta(it) },
                error = formulario.errores["respuesta"], lineas = 2
            )
        },
        confirmButton = { TextButton(onClick = { vm.guardarRespuesta() }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = { vm.cerrarDialogo() }) { Text("Cancelar") } }
    )
}
