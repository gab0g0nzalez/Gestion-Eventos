package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.AreaContacto
import com.example.myapplication.data.MedioContacto
import com.example.myapplication.data.TipoOportunidad
import com.example.myapplication.data.TipoOrganizacion
import com.example.myapplication.ui.components.CampoTexto
import com.example.myapplication.ui.components.PantallaSecundaria
import com.example.myapplication.ui.components.SelectorDesplegable
import com.example.myapplication.viewmodel.CatalogoViewModel
import com.example.myapplication.viewmodel.ContactoFormViewModel
import com.example.myapplication.viewmodel.EventoFormViewModel
import com.example.myapplication.viewmodel.OrganizacionFormViewModel

// ---------- Eventos ----------

@Composable
fun EventosScreen(catVm: CatalogoViewModel, onAtras: () -> Unit, onNuevo: () -> Unit) {
    val eventos by catVm.eventos.collectAsState()
    val organizaciones by catVm.organizaciones.collectAsState()

    PantallaSecundaria(titulo = "Eventos", onAtras = onAtras) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                Button(onClick = onNuevo, modifier = Modifier.fillMaxWidth()) { Text("Registrar evento") }
            }
            if (eventos.isEmpty()) {
                item { Text("Sin eventos registrados.") }
            }
            items(eventos, key = { it.id }) { e ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(e.nombre, style = MaterialTheme.typography.titleMedium)
                        Text("${e.tipo.etiqueta} · ${e.fechaInicio} ${e.horario}", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${e.ubicacion}, ${e.comuna}, ${e.region}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Organización: ${organizaciones.find { it.id == e.organizacionId }?.nombre ?: "-"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Público esperado: ${e.publicoEsperado} · Asistentes estimados: ${e.cantidadAsistentes}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text("Condiciones: ${e.condicionesParticipacion}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun EventoFormScreen(vm: EventoFormViewModel, onGuardado: () -> Unit, onAtras: () -> Unit) {
    val ui by vm.formulario.collectAsState()
    val organizaciones by vm.organizaciones.collectAsState()
    val d = ui.datos
    val errores = ui.errores
    val editar = vm::editar

    PantallaSecundaria(titulo = "Nuevo evento", onAtras = onAtras) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CampoTexto("Nombre del evento", d.nombre, { v -> editar { it.copy(nombre = v) } }, error = errores["nombre"])
            SelectorDesplegable("Tipo", TipoOportunidad.entries, d.tipo, { it.etiqueta }, { t -> editar { it.copy(tipo = t) } })
            CampoTexto("Descripción", d.descripcion, { v -> editar { it.copy(descripcion = v) } }, lineas = 2)
            CampoTexto("Fecha de inicio (AAAA-MM-DD)", d.fechaInicio, { v -> editar { it.copy(fechaInicio = v) } }, error = errores["fechaInicio"])
            CampoTexto("Fecha de término (AAAA-MM-DD)", d.fechaTermino, { v -> editar { it.copy(fechaTermino = v) } }, error = errores["fechaTermino"])
            CampoTexto("Horario (HH:MM-HH:MM)", d.horario, { v -> editar { it.copy(horario = v) } }, error = errores["horario"])
            CampoTexto("Dirección o ubicación general (ficticia)", d.ubicacion, { v -> editar { it.copy(ubicacion = v) } }, error = errores["ubicacion"])
            CampoTexto("Comuna", d.comuna, { v -> editar { it.copy(comuna = v) } }, error = errores["comuna"])
            CampoTexto("Región", d.region, { v -> editar { it.copy(region = v) } }, error = errores["region"])
            SelectorDesplegable(
                etiqueta = "Organización responsable",
                opciones = organizaciones,
                seleccionado = organizaciones.find { it.id == d.organizacionId },
                etiquetaDe = { it.nombre },
                onSeleccion = { o -> editar { it.copy(organizacionId = o.id) } },
                error = errores["organizacion"],
                textoVacio = "Seleccionar organización"
            )
            CampoTexto("Público esperado", d.publicoEsperado, { v -> editar { it.copy(publicoEsperado = v) } })
            CampoTexto("Público objetivo", d.publicoObjetivo, { v -> editar { it.copy(publicoObjetivo = v) } })
            CampoTexto("Cantidad estimada de asistentes", d.asistentes, { v -> editar { it.copy(asistentes = v) } }, error = errores["asistentes"], teclado = KeyboardType.Number)
            CampoTexto("Condiciones de participación", d.condiciones, { v -> editar { it.copy(condiciones = v) } }, lineas = 2)
            CampoTexto("Requisitos", d.requisitos, { v -> editar { it.copy(requisitos = v) } }, lineas = 2)
            CampoTexto("Costo referencial ficticio (pesos)", d.costo, { v -> editar { it.copy(costo = v) } }, error = errores["costo"], teclado = KeyboardType.Number)
            CampoTexto("Espacios disponibles", d.espacios, { v -> editar { it.copy(espacios = v) } }, error = errores["espacios"], teclado = KeyboardType.Number)
            CampoTexto("Observaciones", d.observaciones, { v -> editar { it.copy(observaciones = v) } }, lineas = 2)
            Button(onClick = { vm.guardar(onGuardado) }, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar evento")
            }
        }
    }
}

// ---------- Organizaciones ----------

@Composable
fun OrganizacionesScreen(catVm: CatalogoViewModel, onAtras: () -> Unit, onNuevo: () -> Unit) {
    val organizaciones by catVm.organizaciones.collectAsState()

    PantallaSecundaria(titulo = "Organizaciones", onAtras = onAtras) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                Button(onClick = onNuevo, modifier = Modifier.fillMaxWidth()) { Text("Registrar organización") }
            }
            if (organizaciones.isEmpty()) {
                item { Text("Sin organizaciones registradas.") }
            }
            items(organizaciones, key = { it.id }) { o ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(o.nombre, style = MaterialTheme.typography.titleMedium)
                        Text("${o.tipo.etiqueta} · ${o.rubro}", style = MaterialTheme.typography.bodySmall)
                        Text("${o.comuna}, ${o.region}", style = MaterialTheme.typography.bodySmall)
                        Text("Público asociado: ${o.publicoAsociado}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun OrganizacionFormScreen(vm: OrganizacionFormViewModel, onGuardado: () -> Unit, onAtras: () -> Unit) {
    val ui by vm.formulario.collectAsState()
    val d = ui.datos
    val errores = ui.errores
    val editar = vm::editar

    PantallaSecundaria(titulo = "Nueva organización", onAtras = onAtras) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Use nombres ficticios o autorizados.", style = MaterialTheme.typography.bodySmall)
            CampoTexto("Nombre ficticio o autorizado", d.nombre, { v -> editar { it.copy(nombre = v) } }, error = errores["nombre"])
            SelectorDesplegable("Tipo de organización", TipoOrganizacion.entries, d.tipo, { it.etiqueta }, { t -> editar { it.copy(tipo = t) } })
            CampoTexto("Rubro", d.rubro, { v -> editar { it.copy(rubro = v) } }, error = errores["rubro"])
            CampoTexto("Comuna", d.comuna, { v -> editar { it.copy(comuna = v) } }, error = errores["comuna"])
            CampoTexto("Región", d.region, { v -> editar { it.copy(region = v) } }, error = errores["region"])
            CampoTexto("Sitio web ficticio o público (opcional)", d.sitioWeb, { v -> editar { it.copy(sitioWeb = v) } })
            CampoTexto("Público asociado", d.publicoAsociado, { v -> editar { it.copy(publicoAsociado = v) } })
            CampoTexto("Observaciones", d.observaciones, { v -> editar { it.copy(observaciones = v) } }, lineas = 2)
            Button(onClick = { vm.guardar(onGuardado) }, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar organización")
            }
        }
    }
}

// ---------- Contactos ----------

@Composable
fun ContactosScreen(catVm: CatalogoViewModel, onAtras: () -> Unit, onNuevo: () -> Unit) {
    val contactos by catVm.contactos.collectAsState()
    val organizaciones by catVm.organizaciones.collectAsState()

    PantallaSecundaria(titulo = "Contactos", onAtras = onAtras) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                Button(onClick = onNuevo, modifier = Modifier.fillMaxWidth()) { Text("Registrar contacto") }
            }
            if (contactos.isEmpty()) {
                item { Text("Sin contactos registrados.") }
            }
            items(contactos, key = { it.id }) { c ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(c.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${c.cargo} · ${c.area.etiqueta}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Organización: ${organizaciones.find { it.id == c.organizacionId }?.nombre ?: "-"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Medio preferido: ${c.medioPreferido.etiqueta} · ${c.correo.ifBlank { c.telefono }}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Último contacto: ${c.fechaUltimoContacto ?: "sin registro"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContactoFormScreen(vm: ContactoFormViewModel, onGuardado: () -> Unit, onAtras: () -> Unit) {
    val ui by vm.formulario.collectAsState()
    val organizaciones by vm.organizaciones.collectAsState()
    val d = ui.datos
    val errores = ui.errores
    val editar = vm::editar

    PantallaSecundaria(titulo = "Nuevo contacto", onAtras = onAtras) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Nombres, correos y teléfonos deben ser ficticios.", style = MaterialTheme.typography.bodySmall)
            CampoTexto("Nombre ficticio", d.nombre, { v -> editar { it.copy(nombre = v) } }, error = errores["nombre"])
            SelectorDesplegable(
                etiqueta = "Organización",
                opciones = organizaciones,
                seleccionado = organizaciones.find { it.id == d.organizacionId },
                etiquetaDe = { it.nombre },
                onSeleccion = { o -> editar { it.copy(organizacionId = o.id) } },
                error = errores["organizacion"],
                textoVacio = "Seleccionar organización"
            )
            CampoTexto("Cargo o función", d.cargo, { v -> editar { it.copy(cargo = v) } }, error = errores["cargo"])
            SelectorDesplegable("Área de contacto", AreaContacto.entries, d.area, { it.etiqueta }, { a -> editar { it.copy(area = a) } })
            CampoTexto("Correo ficticio", d.correo, { v -> editar { it.copy(correo = v) } }, error = errores["correo"], teclado = KeyboardType.Email)
            CampoTexto("Teléfono ficticio", d.telefono, { v -> editar { it.copy(telefono = v) } }, error = errores["medio"], teclado = KeyboardType.Phone)
            SelectorDesplegable("Medio de contacto preferente", MedioContacto.entries, d.medio, { it.etiqueta }, { m -> editar { it.copy(medio = m) } })
            CampoTexto("Observaciones", d.observaciones, { v -> editar { it.copy(observaciones = v) } }, lineas = 2)
            Button(onClick = { vm.guardar(onGuardado) }, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar contacto")
            }
        }
    }
}
