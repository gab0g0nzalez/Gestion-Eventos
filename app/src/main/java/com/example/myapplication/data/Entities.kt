package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Usuario ficticio del sistema. La clave se guarda como hash SHA-256, nunca en texto plano. */
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val correo: String,
    val claveHash: String,
    val perfil: Perfil = Perfil.COMERCIAL,
    val area: String,
    val zona: String,
    val activo: Boolean = true,
    val fechaCreacion: String,
    val fechaActualizacion: String
)

/** Fechas en formato AAAA-MM-DD y horas HH:MM para que se ordenen como texto. */
@Entity(tableName = "eventos")
data class EventoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val tipo: TipoOportunidad,
    val descripcion: String,
    val fechaInicio: String,
    val fechaTermino: String,
    val horario: String,
    val ubicacion: String,
    val comuna: String,
    val region: String,
    val organizacionId: Long?,
    val publicoEsperado: String,
    val publicoObjetivo: String,
    val cantidadAsistentes: Int,
    val condicionesParticipacion: String,
    val requisitos: String,
    val costoReferencial: Long,
    val espaciosDisponibles: Int,
    val observaciones: String
)

@Entity(tableName = "organizaciones")
data class OrganizacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val tipo: TipoOrganizacion,
    val rubro: String,
    val comuna: String,
    val region: String,
    val sitioWeb: String,
    val publicoAsociado: String,
    val observaciones: String
)

@Entity(tableName = "contactos")
data class ContactoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val organizacionId: Long,
    val cargo: String,
    val area: AreaContacto,
    val correo: String,
    val telefono: String,
    val medioPreferido: MedioContacto,
    val fechaUltimoContacto: String?,
    val observaciones: String
)

@Entity(tableName = "oportunidades")
data class OportunidadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val tipo: TipoOportunidad,
    val descripcion: String,
    val fuenteInformacion: String,
    val fechaDeteccion: String,
    val responsableId: Long?,
    val prioridad: Prioridad,
    val potencialComercial: Int,
    val afinidadPublico: Int,
    val estado: EstadoOportunidad,
    val eventoId: Long?,
    val organizacionId: Long?,
    val contactoId: Long?,
    val fechaUltimaActualizacion: String,
    val observaciones: String,
    val creadoPorId: Long?
)

@Entity(tableName = "seguimientos")
data class SeguimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val oportunidadId: Long,
    val tipoAccion: TipoAccion,
    val fecha: String,
    val hora: String,
    val resultado: String,
    val proximaAccion: String,
    val fechaProximoSeguimiento: String?,
    val observaciones: String,
    val usuarioId: Long?
)

@Entity(tableName = "propuestas")
data class PropuestaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val oportunidadId: Long,
    val fechaEnvio: String,
    val descripcion: String,
    val condicionesParticipacion: String,
    val costoReferencial: Long,
    val respondida: Boolean = false,
    val respuesta: String = ""
)

/** Registro de auditoría: cambios de estado, seguimientos, propuestas y resultados. */
@Entity(tableName = "historial")
data class HistorialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val oportunidadId: Long,
    val fecha: String,
    val tipo: String,
    val descripcion: String,
    val usuarioId: Long?
)

@Entity(tableName = "resultados")
data class ResultadoEntity(
    @PrimaryKey val oportunidadId: Long,
    val fechaRealizacion: String,
    val estadoFinal: String,
    val cantidadAsistentes: Int,
    val contactosGenerados: Int,
    val personasInteresadas: Int,
    val inscripciones: Int,
    val observaciones: String,
    val evaluacion: String,
    val aprendizajes: String
)
