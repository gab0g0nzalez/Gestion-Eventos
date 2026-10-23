package com.example.myapplication.domain

import com.example.myapplication.data.EstadoOportunidad
import com.example.myapplication.data.EventoEntity
import com.example.myapplication.data.OportunidadEntity
import com.example.myapplication.data.OportunidadResumen
import com.example.myapplication.data.Prioridad
import com.example.myapplication.data.PropuestaEntity
import com.example.myapplication.data.SeguimientoEntity
import com.example.myapplication.data.TipoOportunidad
import java.security.MessageDigest

/** Lógica de negocio pura (sin Android), para poder probarla con pruebas unitarias JVM. */

object Seguridad {
    /** Hash SHA-256 en hexadecimal. Suficiente para un MVP con cuentas ficticias. */
    fun hash(texto: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(texto.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}

/** Validaciones de formularios. Devuelven un mapa campo -> mensaje; vacío significa válido. */
object Validar {
    private val regexCorreo = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    private val regexHorario = Regex("^([01]\\d|2[0-3]):[0-5]\\d\\s*-\\s*([01]\\d|2[0-3]):[0-5]\\d$")
    private const val OBLIGATORIO = "Campo obligatorio"
    private const val FORMATO_FECHA = "Use formato AAAA-MM-DD"

    fun correoValido(correo: String): Boolean = regexCorreo.matches(correo.trim())

    fun registro(
        nombre: String,
        correo: String,
        clave: String,
        area: String,
        zona: String,
        aceptaTerminos: Boolean
    ): Map<String, String> {
        val e = mutableMapOf<String, String>()
        if (nombre.isBlank()) e["nombre"] = OBLIGATORIO
        if (!correoValido(correo)) e["correo"] = "Correo inválido"
        if (clave.length < 6) e["clave"] = "Debe tener al menos 6 caracteres"
        if (area.isBlank()) e["area"] = OBLIGATORIO
        if (zona.isBlank()) e["zona"] = OBLIGATORIO
        if (!aceptaTerminos) e["terminos"] = "Debes aceptar los términos"
        return e
    }

    fun oportunidad(d: DatosOportunidad): Map<String, String> {
        val e = mutableMapOf<String, String>()
        if (d.nombre.isBlank()) e["nombre"] = OBLIGATORIO
        if (!Fechas.esValida(d.fechaDeteccion)) e["fechaDeteccion"] = FORMATO_FECHA
        if (!enRango(d.potencial, 1, 5)) e["potencial"] = "Ingrese un valor entre 1 y 5"
        if (!enRango(d.afinidad, 1, 5)) e["afinidad"] = "Ingrese un valor entre 1 y 5"
        if (d.eventoId == null) e["evento"] = "Seleccione un evento"
        return e
    }

    fun evento(d: DatosEvento): Map<String, String> {
        val e = mutableMapOf<String, String>()
        if (d.nombre.isBlank()) e["nombre"] = OBLIGATORIO
        if (!Fechas.esValida(d.fechaInicio)) e["fechaInicio"] = FORMATO_FECHA
        if (!Fechas.esValida(d.fechaTermino)) {
            e["fechaTermino"] = FORMATO_FECHA
        } else if (Fechas.esValida(d.fechaInicio) && d.fechaTermino < d.fechaInicio) {
            e["fechaTermino"] = "Debe ser igual o posterior a la fecha de inicio"
        }
        if (!regexHorario.matches(d.horario.trim())) e["horario"] = "Formato HH:MM-HH:MM"
        if (d.ubicacion.isBlank()) e["ubicacion"] = OBLIGATORIO
        if (d.comuna.isBlank()) e["comuna"] = OBLIGATORIO
        if (d.region.isBlank()) e["region"] = OBLIGATORIO
        if (d.organizacionId == null) e["organizacion"] = "Seleccione una organización"
        if (!enRango(d.asistentes, 0, 1_000_000)) e["asistentes"] = "Ingrese un número válido"
        if (d.costo.toLongOrNull()?.let { it >= 0 } != true) e["costo"] = "Ingrese un número válido"
        if (!enRango(d.espacios, 0, 10_000)) e["espacios"] = "Ingrese un número válido"
        return e
    }

    fun organizacion(d: DatosOrganizacion): Map<String, String> {
        val e = mutableMapOf<String, String>()
        if (d.nombre.isBlank()) e["nombre"] = OBLIGATORIO
        if (d.rubro.isBlank()) e["rubro"] = OBLIGATORIO
        if (d.comuna.isBlank()) e["comuna"] = OBLIGATORIO
        if (d.region.isBlank()) e["region"] = OBLIGATORIO
        return e
    }

    fun contacto(d: DatosContacto): Map<String, String> {
        val e = mutableMapOf<String, String>()
        if (d.nombre.isBlank()) e["nombre"] = OBLIGATORIO
        if (d.organizacionId == null) e["organizacion"] = "Seleccione una organización"
        if (d.cargo.isBlank()) e["cargo"] = OBLIGATORIO
        if (d.correo.isBlank() && d.telefono.isBlank()) {
            e["medio"] = "Ingrese un correo o teléfono ficticio"
        }
        if (d.correo.isNotBlank() && !correoValido(d.correo)) e["correo"] = "Correo inválido"
        return e
    }

    fun seguimiento(d: DatosSeguimiento): Map<String, String> {
        val e = mutableMapOf<String, String>()
        if (!Fechas.esValida(d.fecha)) e["fecha"] = FORMATO_FECHA
        if (!Fechas.esHoraValida(d.hora)) e["hora"] = "Formato HH:MM"
        if (d.resultado.isBlank()) e["resultado"] = OBLIGATORIO
        if (d.fechaProximoSeguimiento.isNotBlank()) {
            if (!Fechas.esValida(d.fechaProximoSeguimiento)) {
                e["fechaProximoSeguimiento"] = FORMATO_FECHA
            } else if (Fechas.esValida(d.fecha) && d.fechaProximoSeguimiento < d.fecha) {
                e["fechaProximoSeguimiento"] = "No puede ser anterior al seguimiento"
            }
        }
        return e
    }

    fun propuesta(d: DatosPropuesta): Map<String, String> {
        val e = mutableMapOf<String, String>()
        if (!Fechas.esValida(d.fechaEnvio)) e["fechaEnvio"] = FORMATO_FECHA
        if (d.descripcion.isBlank()) e["descripcion"] = OBLIGATORIO
        if (d.costo.toLongOrNull()?.let { it >= 0 } != true) e["costo"] = "Ingrese un número válido"
        return e
    }

    fun resultado(d: DatosResultado): Map<String, String> {
        val e = mutableMapOf<String, String>()
        if (!Fechas.esValida(d.fechaRealizacion)) e["fechaRealizacion"] = FORMATO_FECHA
        if (d.estadoFinal.isBlank()) e["estadoFinal"] = OBLIGATORIO
        listOf("asistentes", "contactos", "interesadas", "inscripciones").forEach { campo ->
            val valor = when (campo) {
                "asistentes" -> d.asistentes
                "contactos" -> d.contactos
                "interesadas" -> d.interesadas
                else -> d.inscripciones
            }
            if (!enRango(valor, 0, 1_000_000)) e[campo] = "Ingrese un número válido"
        }
        return e
    }

    private fun enRango(valor: String, min: Int, max: Int): Boolean =
        valor.toIntOrNull()?.let { it in min..max } ?: false
}

enum class TipoAlerta(val etiqueta: String) {
    EVENTO_PROXIMO("Evento próximo"),
    SEGUIMIENTO_ATRASADO("Seguimiento atrasado"),
    PROPUESTA_SIN_RESPUESTA("Propuesta sin respuesta"),
    OPORTUNIDAD_SIN_CONTACTO("Oportunidad sin contacto")
}

data class Alerta(
    val tipo: TipoAlerta,
    val titulo: String,
    val detalle: String,
    val oportunidadId: Long?
)

object Alertas {
    const val DIAS_ANTICIPACION = 14

    /**
     * Calcula las alertas a partir de los datos actuales.
     * [hoy] se recibe por parámetro para que la lógica sea determinista en las pruebas.
     */
    fun calcular(
        hoy: String,
        oportunidades: List<OportunidadEntity>,
        eventos: List<EventoEntity>,
        seguimientos: List<SeguimientoEntity>,
        propuestas: List<PropuestaEntity>
    ): List<Alerta> {
        val limite = Fechas.sumarDias(hoy, DIAS_ANTICIPACION)
        val resultado = mutableListOf<Alerta>()

        eventos
            .filter { it.fechaInicio >= hoy && it.fechaInicio <= limite }
            .forEach { evento ->
                resultado += Alerta(
                    TipoAlerta.EVENTO_PROXIMO,
                    evento.nombre,
                    "Inicia el ${evento.fechaInicio} en ${evento.comuna}",
                    null
                )
            }

        // Se evalúa el último seguimiento de cada oportunidad abierta.
        val ultimoPorOportunidad = seguimientos
            .groupBy { it.oportunidadId }
            .mapValues { (_, lista) -> lista.maxByOrNull { "${it.fecha} ${it.hora}" } }

        oportunidades.filter { !it.estado.cerrado }.forEach { oportunidad ->
            val proximo = ultimoPorOportunidad[oportunidad.id]?.fechaProximoSeguimiento
            if (!proximo.isNullOrBlank() && proximo < hoy) {
                resultado += Alerta(
                    TipoAlerta.SEGUIMIENTO_ATRASADO,
                    oportunidad.nombre,
                    "Debía gestionarse el $proximo",
                    oportunidad.id
                )
            }
            if (oportunidad.estado == EstadoOportunidad.EVENTO_IDENTIFICADO ||
                oportunidad.estado == EstadoOportunidad.CONTACTO_PENDIENTE
            ) {
                resultado += Alerta(
                    TipoAlerta.OPORTUNIDAD_SIN_CONTACTO,
                    oportunidad.nombre,
                    "Estado: ${oportunidad.estado.etiqueta}",
                    oportunidad.id
                )
            }
        }

        propuestas.filter { !it.respondida }.forEach { propuesta ->
            val nombre = oportunidades.find { it.id == propuesta.oportunidadId }?.nombre
                ?: "Propuesta #${propuesta.id}"
            resultado += Alerta(
                TipoAlerta.PROPUESTA_SIN_RESPUESTA,
                nombre,
                "Enviada el ${propuesta.fechaEnvio}",
                propuesta.oportunidadId
            )
        }
        return resultado
    }
}

data class ResumenIndicadores(
    val total: Int = 0,
    val activas: Int = 0,
    val confirmadas: Int = 0,
    val propuestasEnviadas: Int = 0,
    val descartadas: Int = 0,
    val porEstado: Map<EstadoOportunidad, Int> = emptyMap()
)

object Indicadores {
    fun calcular(oportunidades: List<OportunidadEntity>): ResumenIndicadores = ResumenIndicadores(
        total = oportunidades.size,
        activas = oportunidades.count { !it.estado.cerrado },
        confirmadas = oportunidades.count { it.estado == EstadoOportunidad.PARTICIPACION_CONFIRMADA },
        propuestasEnviadas = oportunidades.count {
            it.estado == EstadoOportunidad.PROPUESTA_ENVIADA ||
                it.estado == EstadoOportunidad.EN_ESPERA_RESPUESTA
        },
        descartadas = oportunidades.count { it.estado == EstadoOportunidad.DESCARTADA },
        porEstado = oportunidades.groupingBy { it.estado }.eachCount()
    )
}

data class FiltroOportunidades(
    val texto: String = "",
    val estado: EstadoOportunidad? = null,
    val prioridad: Prioridad? = null,
    val tipo: TipoOportunidad? = null,
    val comuna: String? = null,
    val responsableId: Long? = null,
    val desde: String? = null,
    val hasta: String? = null
)

object Filtros {
    fun aplicar(lista: List<OportunidadResumen>, f: FiltroOportunidades): List<OportunidadResumen> =
        lista.filter { o ->
            (f.texto.isBlank() ||
                o.nombre.contains(f.texto.trim(), ignoreCase = true) ||
                (o.organizacionNombre ?: "").contains(f.texto.trim(), ignoreCase = true)) &&
                (f.estado == null || o.estado == f.estado) &&
                (f.prioridad == null || o.prioridad == f.prioridad) &&
                (f.tipo == null || o.tipo == f.tipo) &&
                (f.comuna == null ||
                    (o.eventoComuna ?: "").contains(f.comuna, ignoreCase = true) ||
                    (o.eventoRegion ?: "").contains(f.comuna, ignoreCase = true)) &&
                (f.responsableId == null || o.responsableId == f.responsableId) &&
                (f.desde == null || o.fechaDeteccion >= f.desde) &&
                (f.hasta == null || o.fechaDeteccion <= f.hasta)
        }
}
