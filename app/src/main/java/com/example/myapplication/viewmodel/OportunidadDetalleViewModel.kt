package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.EstadoOportunidad
import com.example.myapplication.data.HistorialEntity
import com.example.myapplication.data.OportunidadEntity
import com.example.myapplication.data.Prioridad
import com.example.myapplication.data.PropuestaEntity
import com.example.myapplication.data.Repositorio
import com.example.myapplication.data.ResultadoEntity
import com.example.myapplication.data.SeguimientoEntity
import com.example.myapplication.domain.DatosPropuesta
import com.example.myapplication.domain.DatosResultado
import com.example.myapplication.domain.DatosSeguimiento
import com.example.myapplication.domain.Fechas
import com.example.myapplication.domain.Validar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Diálogo abierto desde el detalle. */
sealed interface DialogoDetalle {
    data object Seguimiento : DialogoDetalle
    data object Propuesta : DialogoDetalle
    data object Resultado : DialogoDetalle
    data class RespuestaPropuesta(val propuesta: PropuestaEntity) : DialogoDetalle
}

/**
 * Detalle de una oportunidad: datos, gestión, historial y los formularios de sus diálogos.
 * Se crea por entrada de navegación con el id de la oportunidad.
 */
class OportunidadDetalleViewModel(
    private val repo: Repositorio,
    private val id: Long
) : ViewModel() {

    private fun <T> Flow<T>.paraVista(inicial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), inicial)

    val oportunidad: StateFlow<OportunidadEntity?> = repo.observarOportunidad(id).paraVista(null)
    val seguimientos: StateFlow<List<SeguimientoEntity>> = repo.observarSeguimientos(id).paraVista(emptyList())
    val propuestas: StateFlow<List<PropuestaEntity>> = repo.observarPropuestas(id).paraVista(emptyList())
    val historial: StateFlow<List<HistorialEntity>> = repo.observarHistorial(id).paraVista(emptyList())
    val resultado: StateFlow<ResultadoEntity?> = repo.observarResultado(id).paraVista(null)

    // ---------- Diálogos y sus formularios ----------

    private val _dialogo = MutableStateFlow<DialogoDetalle?>(null)
    val dialogo: StateFlow<DialogoDetalle?> = _dialogo.asStateFlow()

    private val _formSeguimiento = MutableStateFlow(Formulario(DatosSeguimiento(oportunidadId = id)))
    val formSeguimiento: StateFlow<Formulario<DatosSeguimiento>> = _formSeguimiento.asStateFlow()

    private val _formPropuesta = MutableStateFlow(Formulario(DatosPropuesta(oportunidadId = id)))
    val formPropuesta: StateFlow<Formulario<DatosPropuesta>> = _formPropuesta.asStateFlow()

    private val _formResultado = MutableStateFlow(Formulario(DatosResultado(oportunidadId = id)))
    val formResultado: StateFlow<Formulario<DatosResultado>> = _formResultado.asStateFlow()

    private val _formRespuesta = MutableStateFlow(Formulario(""))
    val formRespuesta: StateFlow<Formulario<String>> = _formRespuesta.asStateFlow()

    fun abrirSeguimiento() {
        _formSeguimiento.value = Formulario(DatosSeguimiento(oportunidadId = id))
        _dialogo.value = DialogoDetalle.Seguimiento
    }

    fun abrirPropuesta() {
        _formPropuesta.value = Formulario(DatosPropuesta(oportunidadId = id))
        _dialogo.value = DialogoDetalle.Propuesta
    }

    fun abrirResultado() {
        _formResultado.value = Formulario(DatosResultado(oportunidadId = id))
        _dialogo.value = DialogoDetalle.Resultado
    }

    fun abrirRespuesta(propuesta: PropuestaEntity) {
        _formRespuesta.value = Formulario("")
        _dialogo.value = DialogoDetalle.RespuestaPropuesta(propuesta)
    }

    fun cerrarDialogo() {
        _dialogo.value = null
    }

    fun editarSeguimiento(cambio: (DatosSeguimiento) -> DatosSeguimiento) {
        _formSeguimiento.actualizarDatos(cambio)
    }

    fun editarPropuesta(cambio: (DatosPropuesta) -> DatosPropuesta) {
        _formPropuesta.actualizarDatos(cambio)
    }

    fun editarResultado(cambio: (DatosResultado) -> DatosResultado) {
        _formResultado.actualizarDatos(cambio)
    }

    fun editarRespuesta(texto: String) {
        _formRespuesta.actualizarDatos { texto }
    }

    // ---------- Acciones sobre la oportunidad ----------

    fun cambiarEstado(estado: EstadoOportunidad) {
        viewModelScope.launch { repo.cambiarEstado(id, estado, repo.usuarioSesionId()) }
    }

    fun cambiarPrioridad(prioridad: Prioridad) {
        viewModelScope.launch { repo.cambiarPrioridad(id, prioridad, repo.usuarioSesionId()) }
    }

    fun asignarResponsable(responsableId: Long?) {
        viewModelScope.launch { repo.asignarResponsable(id, responsableId, repo.usuarioSesionId()) }
    }

    // ---------- Guardado de diálogos ----------

    fun guardarSeguimiento() {
        val datos = _formSeguimiento.value.datos
        val errores = Validar.seguimiento(datos)
        if (errores.isNotEmpty()) {
            _formSeguimiento.conErrores(errores)
            return
        }
        viewModelScope.launch {
            repo.registrarSeguimiento(
                SeguimientoEntity(
                    oportunidadId = datos.oportunidadId,
                    tipoAccion = datos.tipo,
                    fecha = datos.fecha,
                    hora = datos.hora,
                    resultado = datos.resultado.trim(),
                    proximaAccion = datos.proximaAccion.trim(),
                    fechaProximoSeguimiento = datos.fechaProximoSeguimiento.trim().takeIf { it.isNotEmpty() },
                    observaciones = datos.observaciones.trim(),
                    usuarioId = null
                ),
                repo.usuarioSesionId()
            )
        }
        cerrarDialogo()
    }

    fun guardarPropuesta() {
        val datos = _formPropuesta.value.datos
        val errores = Validar.propuesta(datos)
        if (errores.isNotEmpty()) {
            _formPropuesta.conErrores(errores)
            return
        }
        viewModelScope.launch {
            repo.registrarPropuesta(
                PropuestaEntity(
                    oportunidadId = datos.oportunidadId,
                    fechaEnvio = datos.fechaEnvio,
                    descripcion = datos.descripcion.trim(),
                    condicionesParticipacion = datos.condiciones.trim(),
                    costoReferencial = datos.costo.toLong(),
                    respondida = false
                ),
                repo.usuarioSesionId()
            )
        }
        cerrarDialogo()
    }

    fun guardarResultado() {
        val datos = _formResultado.value.datos
        val errores = Validar.resultado(datos)
        if (errores.isNotEmpty()) {
            _formResultado.conErrores(errores)
            return
        }
        viewModelScope.launch {
            repo.guardarResultado(
                ResultadoEntity(
                    oportunidadId = datos.oportunidadId,
                    fechaRealizacion = datos.fechaRealizacion,
                    estadoFinal = datos.estadoFinal.trim(),
                    cantidadAsistentes = datos.asistentes.toInt(),
                    contactosGenerados = datos.contactos.toInt(),
                    personasInteresadas = datos.interesadas.toInt(),
                    inscripciones = datos.inscripciones.toInt(),
                    observaciones = datos.observaciones.trim(),
                    evaluacion = datos.evaluacion.trim(),
                    aprendizajes = datos.aprendizajes.trim()
                ),
                repo.usuarioSesionId()
            )
        }
        cerrarDialogo()
    }

    fun guardarRespuesta() {
        val dialogoActual = _dialogo.value as? DialogoDetalle.RespuestaPropuesta ?: return
        val texto = _formRespuesta.value.datos
        if (texto.isBlank()) {
            _formRespuesta.conErrores(mapOf("respuesta" to "Campo obligatorio"))
            return
        }
        viewModelScope.launch {
            repo.registrarRespuestaPropuesta(
                dialogoActual.propuesta,
                texto.trim(),
                repo.usuarioSesionId()
            )
        }
        cerrarDialogo()
    }
}
