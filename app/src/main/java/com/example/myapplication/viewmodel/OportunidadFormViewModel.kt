package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ContactoEntity
import com.example.myapplication.data.EventoEntity
import com.example.myapplication.data.OportunidadEntity
import com.example.myapplication.data.OrganizacionEntity
import com.example.myapplication.data.Repositorio
import com.example.myapplication.data.UsuarioEntity
import com.example.myapplication.domain.DatosOportunidad
import com.example.myapplication.domain.Fechas
import com.example.myapplication.domain.LectorQr
import com.example.myapplication.domain.Validar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado completo de la pantalla de alta o modificación de oportunidad. */
data class OportunidadFormUi(
    val cargando: Boolean = false,
    val formulario: Formulario<DatosOportunidad> = Formulario(DatosOportunidad()),
    val mensajeQr: String? = null,
    val eventos: List<EventoEntity> = emptyList(),
    val organizaciones: List<OrganizacionEntity> = emptyList(),
    val contactos: List<ContactoEntity> = emptyList(),
    val usuarios: List<UsuarioEntity> = emptyList()
) {
    val eventoSeleccionado: EventoEntity?
        get() = eventos.find { it.id == formulario.datos.eventoId }

    /** Si hay evento elegido, solo se ofrecen contactos de su organización. */
    val contactosDisponibles: List<ContactoEntity>
        get() = eventoSeleccionado?.let { evento ->
            contactos.filter { it.organizacionId == evento.organizacionId }
        } ?: contactos
}

/**
 * Formulario de oportunidad. id == 0 crea una nueva; id > 0 modifica la existente.
 * Se crea por entrada de navegación: el formulario no se arrastra entre visitas.
 */
class OportunidadFormViewModel(
    private val repo: Repositorio,
    private val id: Long
) : ViewModel() {

    private data class Catalogos(
        val eventos: List<EventoEntity>,
        val organizaciones: List<OrganizacionEntity>,
        val contactos: List<ContactoEntity>,
        val usuarios: List<UsuarioEntity>
    )

    private val catalogos = combine(
        repo.observarEventos(),
        repo.observarOrganizaciones(),
        repo.observarContactos(),
        repo.observarUsuarios()
    ) { eventos, organizaciones, contactos, usuarios ->
        Catalogos(eventos, organizaciones, contactos, usuarios)
    }

    // null = aún cargando la oportunidad a modificar
    private val _formulario = MutableStateFlow<Formulario<DatosOportunidad>?>(
        if (id > 0) null else Formulario(DatosOportunidad())
    )
    private val _mensajeQr = MutableStateFlow<String?>(null)

    val ui: StateFlow<OportunidadFormUi> = combine(_formulario, _mensajeQr, catalogos) { form, qr, cat ->
        OportunidadFormUi(
            cargando = form == null,
            formulario = form ?: Formulario(DatosOportunidad()),
            mensajeQr = qr,
            eventos = cat.eventos,
            organizaciones = cat.organizaciones,
            contactos = cat.contactos,
            usuarios = cat.usuarios
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OportunidadFormUi(cargando = id > 0))

    init {
        if (id > 0) {
            viewModelScope.launch {
                val existente = repo.obtenerOportunidad(id)
                _formulario.value = existente?.let { Formulario(it.aDatos()) } ?: Formulario(DatosOportunidad())
            }
        }
    }

    fun editar(cambio: (DatosOportunidad) -> DatosOportunidad) {
        _formulario.update { actual ->
            actual?.let { it.copy(datos = cambio(it.datos)) }
        }
    }

    /** Rellena los campos reconocidos del QR. Espera a que carguen los catálogos para poder enlazar eventos y contactos. */
    fun importarQr(contenido: String) {
        viewModelScope.launch {
            val actual = _formulario.filterNotNull().first()
            val cat = catalogos.first()
            val resultado = LectorQr.interpretar(
                contenido = contenido,
                actual = actual.datos,
                eventos = cat.eventos,
                contactos = cat.contactos,
                usuarios = cat.usuarios
            )
            _formulario.update { it?.copy(datos = resultado.datos) }
            _mensajeQr.value = resultado.mensaje
        }
    }

    /** Valida y guarda. onOk recibe el id de la oportunidad guardada. */
    fun guardar(onOk: (Long) -> Unit) {
        val datos = _formulario.value?.datos ?: return
        val errores = Validar.oportunidad(datos)
        if (errores.isNotEmpty()) {
            _formulario.update { it?.copy(errores = errores) }
            return
        }

        viewModelScope.launch {
            val usuarioId = repo.usuarioSesionId()
            val existente = if (id > 0) repo.obtenerOportunidad(id) else null
            val evento = datos.eventoId?.let { repo.obtenerEvento(it) }
            val entidad = OportunidadEntity(
                id = existente?.id ?: 0L,
                nombre = datos.nombre.trim(),
                tipo = datos.tipo,
                descripcion = datos.descripcion.trim(),
                fuenteInformacion = datos.fuente.trim(),
                fechaDeteccion = datos.fechaDeteccion,
                responsableId = datos.responsableId,
                prioridad = datos.prioridad,
                potencialComercial = datos.potencial.toInt(),
                afinidadPublico = datos.afinidad.toInt(),
                estado = datos.estado,
                eventoId = datos.eventoId,
                // La organización se toma del evento: un evento pertenece a una organización.
                organizacionId = evento?.organizacionId ?: existente?.organizacionId,
                contactoId = datos.contactoId,
                fechaUltimaActualizacion = Fechas.ahora(),
                observaciones = datos.observaciones.trim(),
                creadoPorId = existente?.creadoPorId ?: usuarioId
            )
            val idGuardada = if (existente == null) {
                repo.crearOportunidad(entidad, usuarioId)
            } else {
                repo.editarOportunidad(entidad, usuarioId)
                existente.id
            }
            onOk(idGuardada)
        }
    }

    private fun OportunidadEntity.aDatos(): DatosOportunidad = DatosOportunidad(
        nombre = nombre,
        tipo = tipo,
        descripcion = descripcion,
        fuente = fuenteInformacion,
        fechaDeteccion = fechaDeteccion,
        responsableId = responsableId,
        prioridad = prioridad,
        potencial = potencialComercial.toString(),
        afinidad = afinidadPublico.toString(),
        eventoId = eventoId,
        contactoId = contactoId,
        estado = estado,
        observaciones = observaciones
    )
}
