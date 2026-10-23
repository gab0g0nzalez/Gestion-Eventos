package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Fila de la lista de oportunidades, con datos del evento, la organización y el responsable. */
data class OportunidadResumen(
    val id: Long,
    val nombre: String,
    val tipo: TipoOportunidad,
    val estado: EstadoOportunidad,
    val prioridad: Prioridad,
    val fechaDeteccion: String,
    val responsableId: Long?,
    val responsableNombre: String?,
    val eventoNombre: String?,
    val eventoFecha: String?,
    val eventoComuna: String?,
    val eventoRegion: String?,
    val organizacionNombre: String?,
    val fechaUltimaActualizacion: String
)

private const val SQL_RESUMEN = """
SELECT o.id AS id, o.nombre AS nombre, o.tipo AS tipo, o.estado AS estado,
       o.prioridad AS prioridad, o.fechaDeteccion AS fechaDeteccion,
       o.responsableId AS responsableId, u.nombre AS responsableNombre,
       e.nombre AS eventoNombre, e.fechaInicio AS eventoFecha,
       e.comuna AS eventoComuna, e.region AS eventoRegion,
       org.nombre AS organizacionNombre,
       o.fechaUltimaActualizacion AS fechaUltimaActualizacion
FROM oportunidades o
LEFT JOIN usuarios u ON u.id = o.responsableId
LEFT JOIN eventos e ON e.id = o.eventoId
LEFT JOIN organizaciones org ON org.id = o.organizacionId
ORDER BY o.fechaUltimaActualizacion DESC
"""

@Dao
interface AppDao {

    // ---------- Usuarios ----------
    @Insert
    suspend fun insertarUsuario(usuario: UsuarioEntity): Long

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarUsuarioPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun observarUsuario(id: Long): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios ORDER BY nombre")
    fun observarUsuarios(): Flow<List<UsuarioEntity>>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contarUsuarios(): Int

    // ---------- Eventos ----------
    @Insert
    suspend fun insertarEvento(evento: EventoEntity): Long

    @Query("SELECT * FROM eventos WHERE id = :id")
    suspend fun obtenerEvento(id: Long): EventoEntity?

    @Query("SELECT * FROM eventos ORDER BY fechaInicio")
    fun observarEventos(): Flow<List<EventoEntity>>

    // ---------- Organizaciones ----------
    @Insert
    suspend fun insertarOrganizacion(organizacion: OrganizacionEntity): Long

    @Query("SELECT * FROM organizaciones ORDER BY nombre")
    fun observarOrganizaciones(): Flow<List<OrganizacionEntity>>

    // ---------- Contactos ----------
    @Insert
    suspend fun insertarContacto(contacto: ContactoEntity): Long

    @Query("SELECT * FROM contactos ORDER BY nombre")
    fun observarContactos(): Flow<List<ContactoEntity>>

    @Query("UPDATE contactos SET fechaUltimoContacto = :fecha WHERE id = :id")
    suspend fun actualizarUltimoContacto(id: Long, fecha: String)

    // ---------- Oportunidades ----------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarOportunidad(oportunidad: OportunidadEntity): Long

    @Query("SELECT * FROM oportunidades WHERE id = :id")
    suspend fun obtenerOportunidad(id: Long): OportunidadEntity?

    @Query("SELECT * FROM oportunidades WHERE id = :id")
    fun observarOportunidad(id: Long): Flow<OportunidadEntity?>

    @Query("SELECT * FROM oportunidades")
    fun observarTodasOportunidades(): Flow<List<OportunidadEntity>>

    @Query(SQL_RESUMEN)
    fun observarResumen(): Flow<List<OportunidadResumen>>

    // ---------- Seguimientos ----------
    @Insert
    suspend fun insertarSeguimiento(seguimiento: SeguimientoEntity): Long

    @Query("SELECT * FROM seguimientos WHERE oportunidadId = :oportunidadId ORDER BY fecha DESC, hora DESC")
    fun observarSeguimientos(oportunidadId: Long): Flow<List<SeguimientoEntity>>

    @Query("SELECT * FROM seguimientos")
    fun observarTodosSeguimientos(): Flow<List<SeguimientoEntity>>

    // ---------- Propuestas ----------
    @Insert
    suspend fun insertarPropuesta(propuesta: PropuestaEntity): Long

    @Query("SELECT * FROM propuestas WHERE oportunidadId = :oportunidadId ORDER BY fechaEnvio DESC")
    fun observarPropuestas(oportunidadId: Long): Flow<List<PropuestaEntity>>

    @Query("SELECT * FROM propuestas")
    fun observarTodasPropuestas(): Flow<List<PropuestaEntity>>

    @Query("UPDATE propuestas SET respondida = 1, respuesta = :respuesta WHERE id = :id")
    suspend fun responderPropuesta(id: Long, respuesta: String)

    // ---------- Historial ----------
    @Insert
    suspend fun insertarHistorial(historial: HistorialEntity): Long

    @Query("SELECT * FROM historial WHERE oportunidadId = :oportunidadId ORDER BY id DESC")
    fun observarHistorial(oportunidadId: Long): Flow<List<HistorialEntity>>

    // ---------- Resultados ----------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarResultado(resultado: ResultadoEntity): Long

    @Query("SELECT * FROM resultados WHERE oportunidadId = :oportunidadId")
    fun observarResultado(oportunidadId: Long): Flow<ResultadoEntity?>
}
