package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.domain.Alerta
import com.example.myapplication.domain.Alertas
import com.example.myapplication.domain.Fechas
import com.example.myapplication.domain.Seguridad
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Punto único de acceso a datos. Los ViewModels no hablan directamente con el DAO.
 * La sesión se guarda en SharedPreferences (solo el id del usuario, nunca la clave).
 */
class Repositorio private constructor(
    private val dao: AppDao,
    private val prefs: SharedPreferences
) {
    private val sesionId = MutableStateFlow(prefs.getLong(CLAVE_SESION, -1L))

    companion object {
        private const val PREFS = "sesion_crm"
        private const val CLAVE_SESION = "usuario_id"

        @Volatile
        private var instancia: Repositorio? = null

        fun obtener(context: Context): Repositorio {
            instancia?.let { return it }
            return synchronized(this) {
                instancia ?: crear(context).also { instancia = it }
            }
        }

        private fun crear(context: Context): Repositorio {
            val app = context.applicationContext
            return Repositorio(
                AppDatabase.obtener(app).appDao(),
                app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            )
        }
    }

    // ---------- Sesión ----------

    fun sesionIniciada(): Boolean = sesionId.value >= 0

    fun usuarioActual(): Flow<UsuarioEntity?> =
        sesionId.flatMapLatest { id -> if (id < 0) flowOf(null) else dao.observarUsuario(id) }

    suspend fun iniciarSesion(correo: String, clave: String): Boolean {
        val usuario = dao.buscarUsuarioPorCorreo(correo.trim()) ?: return false
        if (!usuario.activo || usuario.claveHash != Seguridad.hash(clave)) return false
        abrirSesion(usuario.id)
        return true
    }

    /** Devuelve false si el correo ya está registrado. */
    suspend fun registrarUsuario(
        nombre: String,
        correo: String,
        clave: String,
        area: String,
        zona: String
    ): Boolean {
        if (dao.buscarUsuarioPorCorreo(correo.trim()) != null) return false
        val ahora = Fechas.ahora()
        val id = dao.insertarUsuario(
            UsuarioEntity(
                nombre = nombre.trim(),
                correo = correo.trim(),
                claveHash = Seguridad.hash(clave),
                area = area.trim(),
                zona = zona.trim(),
                fechaCreacion = ahora,
                fechaActualizacion = ahora
            )
        )
        abrirSesion(id)
        return true
    }

    fun cerrarSesion() {
        prefs.edit().remove(CLAVE_SESION).apply()
        sesionId.value = -1L
    }

    private fun abrirSesion(id: Long) {
        prefs.edit().putLong(CLAVE_SESION, id).apply()
        sesionId.value = id
    }

    fun usuarioSesionId(): Long? = sesionId.value.takeIf { it >= 0 }

    // ---------- Datos de referencia ----------

    suspend fun sembrarSiVacio() {
        if (dao.contarUsuarios() == 0) SeedData.cargar(dao)
    }

    fun observarUsuarios(): Flow<List<UsuarioEntity>> = dao.observarUsuarios()
    fun observarEventos(): Flow<List<EventoEntity>> = dao.observarEventos()
    fun observarOrganizaciones(): Flow<List<OrganizacionEntity>> = dao.observarOrganizaciones()
    fun observarContactos(): Flow<List<ContactoEntity>> = dao.observarContactos()

    suspend fun obtenerEvento(id: Long): EventoEntity? = dao.obtenerEvento(id)

    suspend fun guardarEvento(evento: EventoEntity): Long = dao.insertarEvento(evento)

    suspend fun guardarOrganizacion(organizacion: OrganizacionEntity): Long =
        dao.insertarOrganizacion(organizacion)

    suspend fun guardarContacto(contacto: ContactoEntity): Long = dao.insertarContacto(contacto)

    // ---------- Oportunidades ----------

    fun observarResumenOportunidades(): Flow<List<OportunidadResumen>> = dao.observarResumen()

    fun observarOportunidadesEntidades(): Flow<List<OportunidadEntity>> = dao.observarTodasOportunidades()

    fun observarOportunidad(id: Long): Flow<OportunidadEntity?> = dao.observarOportunidad(id)

    suspend fun obtenerOportunidad(id: Long): OportunidadEntity? = dao.obtenerOportunidad(id)

    suspend fun crearOportunidad(oportunidad: OportunidadEntity, usuarioId: Long?): Long {
        val id = dao.guardarOportunidad(oportunidad)
        registrarHistorial(id, "CREACION", "Oportunidad creada", usuarioId)
        return id
    }

    suspend fun editarOportunidad(oportunidad: OportunidadEntity, usuarioId: Long?) {
        dao.guardarOportunidad(oportunidad)
        registrarHistorial(oportunidad.id, "EDICION", "Datos generales actualizados", usuarioId)
    }

    suspend fun cambiarEstado(id: Long, nuevo: EstadoOportunidad, usuarioId: Long?) {
        modificarOportunidad(id, usuarioId) { actual ->
            if (actual.estado == nuevo) null
            else "Estado: ${actual.estado.etiqueta} → ${nuevo.etiqueta}" to actual.copy(estado = nuevo)
        }
    }

    suspend fun cambiarPrioridad(id: Long, nueva: Prioridad, usuarioId: Long?) {
        modificarOportunidad(id, usuarioId) { actual ->
            if (actual.prioridad == nueva) null
            else "Prioridad: ${actual.prioridad.etiqueta} → ${nueva.etiqueta}" to actual.copy(prioridad = nueva)
        }
    }

    suspend fun asignarResponsable(id: Long, responsableId: Long?, usuarioId: Long?) {
        modificarOportunidad(id, usuarioId) { actual ->
            if (actual.responsableId == responsableId) {
                null
            } else {
                "Responsable reasignado" to actual.copy(responsableId = responsableId)
            }
        }
    }

    /**
     * Aplica un cambio sobre la oportunidad y, si hubo cambio real, lo deja en el historial.
     * El lambda devuelve null si no hay nada que cambiar.
     */
    private suspend fun modificarOportunidad(
        id: Long,
        usuarioId: Long?,
        cambio: (OportunidadEntity) -> Pair<String, OportunidadEntity>?
    ) {
        val actual = dao.obtenerOportunidad(id) ?: return
        val resultado = cambio(actual) ?: return
        val (descripcion, nueva) = resultado
        dao.guardarOportunidad(nueva.copy(fechaUltimaActualizacion = Fechas.ahora()))
        registrarHistorial(id, "ESTADO", descripcion, usuarioId)
    }

    // ---------- Seguimientos, propuestas y resultados ----------

    fun observarSeguimientos(oportunidadId: Long): Flow<List<SeguimientoEntity>> =
        dao.observarSeguimientos(oportunidadId)

    fun observarPropuestas(oportunidadId: Long): Flow<List<PropuestaEntity>> =
        dao.observarPropuestas(oportunidadId)

    fun observarHistorial(oportunidadId: Long): Flow<List<HistorialEntity>> =
        dao.observarHistorial(oportunidadId)

    fun observarResultado(oportunidadId: Long): Flow<ResultadoEntity?> =
        dao.observarResultado(oportunidadId)

    suspend fun registrarSeguimiento(seguimiento: SeguimientoEntity, usuarioId: Long?): Long {
        val id = dao.insertarSeguimiento(seguimiento.copy(usuarioId = usuarioId))
        dao.obtenerOportunidad(seguimiento.oportunidadId)?.let { oportunidad ->
            dao.guardarOportunidad(oportunidad.copy(fechaUltimaActualizacion = Fechas.ahora()))
            oportunidad.contactoId?.let { dao.actualizarUltimoContacto(it, seguimiento.fecha) }
        }
        registrarHistorial(
            seguimiento.oportunidadId,
            "SEGUIMIENTO",
            "${seguimiento.tipoAccion.etiqueta}: ${seguimiento.resultado}",
            usuarioId
        )
        return id
    }

    suspend fun registrarPropuesta(propuesta: PropuestaEntity, usuarioId: Long?): Long {
        val id = dao.insertarPropuesta(propuesta)
        modificarOportunidad(propuesta.oportunidadId, usuarioId) { actual ->
            "Propuesta enviada (${propuesta.fechaEnvio})" to
                actual.copy(estado = EstadoOportunidad.PROPUESTA_ENVIADA)
        }
        return id
    }

    suspend fun registrarRespuestaPropuesta(
        propuesta: PropuestaEntity,
        respuesta: String,
        usuarioId: Long?
    ) {
        dao.responderPropuesta(propuesta.id, respuesta)
        registrarHistorial(
            propuesta.oportunidadId,
            "PROPUESTA",
            "Respuesta a propuesta: $respuesta",
            usuarioId
        )
    }

    suspend fun guardarResultado(resultado: ResultadoEntity, usuarioId: Long?) {
        dao.guardarResultado(resultado)
        registrarHistorial(
            resultado.oportunidadId,
            "RESULTADO",
            "Resultado registrado: ${resultado.estadoFinal}",
            usuarioId
        )
    }

    private suspend fun registrarHistorial(
        oportunidadId: Long,
        tipo: String,
        descripcion: String,
        usuarioId: Long?
    ) {
        dao.insertarHistorial(
            HistorialEntity(
                oportunidadId = oportunidadId,
                fecha = Fechas.ahora(),
                tipo = tipo,
                descripcion = descripcion,
                usuarioId = usuarioId
            )
        )
    }

    // ---------- Alertas ----------

    fun observarAlertas(): Flow<List<Alerta>> = combine(
        dao.observarTodasOportunidades(),
        dao.observarEventos(),
        dao.observarTodosSeguimientos(),
        dao.observarTodasPropuestas()
    ) { oportunidades, eventos, seguimientos, propuestas ->
        Alertas.calcular(Fechas.hoy(), oportunidades, eventos, seguimientos, propuestas)
    }
}
