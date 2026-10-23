package com.example.myapplication.domain

import com.example.myapplication.data.AreaContacto
import com.example.myapplication.data.EstadoOportunidad
import com.example.myapplication.data.MedioContacto
import com.example.myapplication.data.Prioridad
import com.example.myapplication.data.TipoAccion
import com.example.myapplication.data.TipoOportunidad
import com.example.myapplication.data.TipoOrganizacion

/*
 * Datos de formularios tal como los escribe el usuario (texto).
 * Las reglas de validación están en Validar; los VM convierten estos datos en entidades.
 */

data class DatosOportunidad(
    val nombre: String = "",
    val tipo: TipoOportunidad = TipoOportunidad.OTRA,
    val descripcion: String = "",
    val fuente: String = "",
    val fechaDeteccion: String = Fechas.hoy(),
    val responsableId: Long? = null,
    val prioridad: Prioridad = Prioridad.MEDIA,
    val potencial: String = "3",
    val afinidad: String = "3",
    val eventoId: Long? = null,
    val contactoId: Long? = null,
    val estado: EstadoOportunidad = EstadoOportunidad.EVENTO_IDENTIFICADO,
    val observaciones: String = ""
)

data class DatosEvento(
    val nombre: String = "",
    val tipo: TipoOportunidad = TipoOportunidad.EVENTO_CORPORATIVO,
    val descripcion: String = "",
    val fechaInicio: String = Fechas.hoy(),
    val fechaTermino: String = Fechas.hoy(),
    val horario: String = "",
    val ubicacion: String = "",
    val comuna: String = "",
    val region: String = "",
    val organizacionId: Long? = null,
    val publicoEsperado: String = "",
    val publicoObjetivo: String = "",
    val asistentes: String = "",
    val condiciones: String = "",
    val requisitos: String = "",
    val costo: String = "0",
    val espacios: String = "",
    val observaciones: String = ""
)

data class DatosOrganizacion(
    val nombre: String = "",
    val tipo: TipoOrganizacion = TipoOrganizacion.EMPRESA,
    val rubro: String = "",
    val comuna: String = "",
    val region: String = "",
    val sitioWeb: String = "",
    val publicoAsociado: String = "",
    val observaciones: String = ""
)

data class DatosContacto(
    val nombre: String = "",
    val organizacionId: Long? = null,
    val cargo: String = "",
    val area: AreaContacto = AreaContacto.EVENTOS,
    val correo: String = "",
    val telefono: String = "",
    val medio: MedioContacto = MedioContacto.CORREO,
    val observaciones: String = ""
)

data class DatosSeguimiento(
    val oportunidadId: Long,
    val tipo: TipoAccion = TipoAccion.SEGUIMIENTO,
    val fecha: String = Fechas.hoy(),
    val hora: String = Fechas.horaActual(),
    val resultado: String = "",
    val proximaAccion: String = "",
    val fechaProximoSeguimiento: String = "",
    val observaciones: String = ""
)

data class DatosPropuesta(
    val oportunidadId: Long,
    val fechaEnvio: String = Fechas.hoy(),
    val descripcion: String = "",
    val condiciones: String = "",
    val costo: String = "0"
)

data class DatosResultado(
    val oportunidadId: Long,
    val fechaRealizacion: String = Fechas.hoy(),
    val estadoFinal: String = "",
    val asistentes: String = "0",
    val contactos: String = "0",
    val interesadas: String = "0",
    val inscripciones: String = "0",
    val observaciones: String = "",
    val evaluacion: String = "",
    val aprendizajes: String = ""
)
