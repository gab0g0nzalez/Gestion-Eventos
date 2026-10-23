package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.EventoEntity
import com.example.myapplication.data.OrganizacionEntity
import com.example.myapplication.data.Repositorio
import com.example.myapplication.domain.DatosEvento
import com.example.myapplication.domain.Validar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Formulario de un evento nuevo. Vive mientras la pantalla esté en el stack de navegación. */
class EventoFormViewModel(private val repo: Repositorio) : ViewModel() {

    val organizaciones: StateFlow<List<OrganizacionEntity>> =
        repo.observarOrganizaciones().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _formulario = MutableStateFlow(Formulario(DatosEvento()))
    val formulario: StateFlow<Formulario<DatosEvento>> = _formulario.asStateFlow()

    fun editar(cambio: (DatosEvento) -> DatosEvento) = _formulario.actualizarDatos(cambio)

    /** Valida y guarda. onOk se invoca solo cuando el guardado ya terminó. */
    fun guardar(onOk: () -> Unit) {
        val datos = _formulario.value.datos
        val errores = Validar.evento(datos)
        if (errores.isNotEmpty()) {
            _formulario.conErrores(errores)
            return
        }
        viewModelScope.launch {
            repo.guardarEvento(
                EventoEntity(
                    nombre = datos.nombre.trim(),
                    tipo = datos.tipo,
                    descripcion = datos.descripcion.trim(),
                    fechaInicio = datos.fechaInicio,
                    fechaTermino = datos.fechaTermino,
                    horario = datos.horario.trim(),
                    ubicacion = datos.ubicacion.trim(),
                    comuna = datos.comuna.trim(),
                    region = datos.region.trim(),
                    organizacionId = datos.organizacionId,
                    publicoEsperado = datos.publicoEsperado.trim(),
                    publicoObjetivo = datos.publicoObjetivo.trim(),
                    cantidadAsistentes = datos.asistentes.toInt(),
                    condicionesParticipacion = datos.condiciones.trim(),
                    requisitos = datos.requisitos.trim(),
                    costoReferencial = datos.costo.toLong(),
                    espaciosDisponibles = datos.espacios.toInt(),
                    observaciones = datos.observaciones.trim()
                )
            )
            onOk()
        }
    }
}
