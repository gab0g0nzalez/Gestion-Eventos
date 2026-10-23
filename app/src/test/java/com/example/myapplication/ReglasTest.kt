package com.example.myapplication

import com.example.myapplication.data.EstadoOportunidad
import com.example.myapplication.data.EventoEntity
import com.example.myapplication.data.OportunidadEntity
import com.example.myapplication.data.OportunidadResumen
import com.example.myapplication.data.Prioridad
import com.example.myapplication.data.PropuestaEntity
import com.example.myapplication.data.SeguimientoEntity
import com.example.myapplication.data.TipoAccion
import com.example.myapplication.data.TipoOportunidad
import com.example.myapplication.domain.Alertas
import com.example.myapplication.domain.DatosOportunidad
import com.example.myapplication.domain.DatosSeguimiento
import com.example.myapplication.domain.Fechas
import com.example.myapplication.domain.FiltroOportunidades
import com.example.myapplication.domain.Filtros
import com.example.myapplication.domain.Indicadores
import com.example.myapplication.domain.Seguridad
import com.example.myapplication.domain.TipoAlerta
import com.example.myapplication.domain.Validar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasTest {

    private val hoy = "2026-10-13"

    // ---------- Fechas ----------

    @Test
    fun fechaValida_aceptaFormatoIsoYRechazaFechasImposibles() {
        assertTrue(Fechas.esValida("2026-10-13"))
        assertFalse(Fechas.esValida("2026-13-01"))
        assertFalse(Fechas.esValida("2026-02-30"))
        assertFalse(Fechas.esValida("13/10/2026"))
    }

    @Test
    fun sumarDias_cruzaElFinDeMes() {
        assertEquals("2026-11-02", Fechas.sumarDias("2026-10-13", 20))
        assertEquals("2026-10-03", Fechas.sumarDias("2026-10-13", -10))
    }

    // ---------- Validaciones ----------

    @Test
    fun oportunidad_exigeNombreEventoYValoresEntreUnoYCinco() {
        val errores = Validar.oportunidad(
            DatosOportunidad(nombre = "", potencial = "9", afinidad = "0", eventoId = null)
        )
        assertTrue(errores.containsKey("nombre"))
        assertTrue(errores.containsKey("potencial"))
        assertTrue(errores.containsKey("afinidad"))
        assertTrue(errores.containsKey("evento"))
    }

    @Test
    fun oportunidad_valida_noDevuelveErrores() {
        val errores = Validar.oportunidad(
            DatosOportunidad(
                nombre = "Feria demo", fechaDeteccion = hoy, potencial = "4", afinidad = "5", eventoId = 1L
            )
        )
        assertTrue(errores.isEmpty())
    }

    @Test
    fun seguimiento_rechazaProximaFechaAnteriorAlSeguimiento() {
        val errores = Validar.seguimiento(
            DatosSeguimiento(
                oportunidadId = 1L, fecha = "2026-10-13", hora = "10:00",
                resultado = "Contacto", fechaProximoSeguimiento = "2026-10-01"
            )
        )
        assertTrue(errores.containsKey("fechaProximoSeguimiento"))
    }

    @Test
    fun registro_exigeCorreoValidoYClaveDeSeisCaracteres() {
        val errores = Validar.registro("Ana", "correo-malo", "123", "Área", "Zona", true)
        assertTrue(errores.containsKey("correo"))
        assertTrue(errores.containsKey("clave"))
        assertFalse(errores.containsKey("nombre"))
    }

    // ---------- Seguridad ----------

    @Test
    fun hash_esDeterministaYNoGuardaLaClaveEnTextoPlano() {
        assertEquals(Seguridad.hash("123456"), Seguridad.hash("123456"))
        assertNotEquals("123456", Seguridad.hash("123456"))
        assertEquals(64, Seguridad.hash("123456").length)
    }

    // ---------- Alertas ----------

    @Test
    fun alertas_detectanSeguimientoAtrasadoYPropuestaSinRespuesta() {
        val op = oportunidad(id = 1L, nombre = "Stand", estado = EstadoOportunidad.CONTACTO_REALIZADO)
        val seguimientos = listOf(
            seguimiento(oportunidadId = 1L, fecha = "2026-10-01", hora = "10:00", proximo = "2026-10-05"),
            seguimiento(oportunidadId = 1L, fecha = "2026-10-06", hora = "09:00", proximo = "2026-10-08")
        )
        val propuestas = listOf(propuesta(id = 7L, oportunidadId = 1L, respondida = false))

        val alertas = Alertas.calcular(hoy, listOf(op), emptyList(), seguimientos, propuestas)

        // Solo cuenta el último seguimiento (2026-10-08 < hoy): está atrasado.
        assertTrue(alertas.any { it.tipo == TipoAlerta.SEGUIMIENTO_ATRASADO && it.oportunidadId == 1L })
        assertTrue(alertas.any { it.tipo == TipoAlerta.PROPUESTA_SIN_RESPUESTA })
    }

    @Test
    fun alertas_noIncluyenSeguimientoAtrasadoSiYaHuboUnoPosterior() {
        val op = oportunidad(id = 1L, estado = EstadoOportunidad.CONTACTO_REALIZADO)
        val seguimientos = listOf(
            seguimiento(oportunidadId = 1L, fecha = "2026-10-01", hora = "10:00", proximo = "2026-10-05"),
            seguimiento(oportunidadId = 1L, fecha = "2026-10-06", hora = "09:00", proximo = "2026-10-20")
        )
        val alertas = Alertas.calcular(hoy, listOf(op), emptyList(), seguimientos, emptyList())
        assertFalse(alertas.any { it.tipo == TipoAlerta.SEGUIMIENTO_ATRASADO })
    }

    @Test
    fun alertas_eventoProximoSoloDentroDeLaVentana() {
        val cercano = evento(id = 1L, nombre = "Cercano", fechaInicio = "2026-10-20")
        val lejano = evento(id = 2L, nombre = "Lejano", fechaInicio = "2026-12-01")
        val alertas = Alertas.calcular(hoy, emptyList(), listOf(cercano, lejano), emptyList(), emptyList())
        val eventos = alertas.filter { it.tipo == TipoAlerta.EVENTO_PROXIMO }.map { it.titulo }
        assertEquals(listOf("Cercano"), eventos)
    }

    @Test
    fun alertas_oportunidadCerradaNoGeneraAlertasDeSeguimiento() {
        val cerrada = oportunidad(id = 1L, estado = EstadoOportunidad.DESCARTADA)
        val seguimientos = listOf(
            seguimiento(oportunidadId = 1L, fecha = "2026-09-01", hora = "10:00", proximo = "2026-09-05")
        )
        val alertas = Alertas.calcular(hoy, listOf(cerrada), emptyList(), seguimientos, emptyList())
        assertTrue(alertas.isEmpty())
    }

    @Test
    fun alertas_oportunidadEnEstadoInicialSeMarcaSinContacto() {
        val op = oportunidad(id = 1L, estado = EstadoOportunidad.EVENTO_IDENTIFICADO)
        val alertas = Alertas.calcular(hoy, listOf(op), emptyList(), emptyList(), emptyList())
        assertTrue(alertas.any { it.tipo == TipoAlerta.OPORTUNIDAD_SIN_CONTACTO })
    }

    // ---------- Indicadores ----------

    @Test
    fun indicadores_cuentanActivasConfirmadasYDescartadas() {
        val ops = listOf(
            oportunidad(id = 1L, estado = EstadoOportunidad.PARTICIPACION_CONFIRMADA),
            oportunidad(id = 2L, estado = EstadoOportunidad.DESCARTADA),
            oportunidad(id = 3L, estado = EstadoOportunidad.PROPUESTA_ENVIADA),
            oportunidad(id = 4L, estado = EstadoOportunidad.EN_ESPERA_RESPUESTA)
        )
        val r = Indicadores.calcular(ops)
        assertEquals(4, r.total)
        assertEquals(3, r.activas)
        assertEquals(1, r.confirmadas)
        assertEquals(1, r.descartadas)
        assertEquals(2, r.propuestasEnviadas)
    }

    // ---------- Filtros ----------

    @Test
    fun filtros_combinanTextoYEstado() {
        val lista = listOf(
            resumen(id = 1L, nombre = "Feria de convenios", estado = EstadoOportunidad.CONTACTO_REALIZADO),
            resumen(id = 2L, nombre = "Feria institucional", estado = EstadoOportunidad.DESCARTADA),
            resumen(id = 3L, nombre = "Jornada municipal", estado = EstadoOportunidad.CONTACTO_REALIZADO)
        )
        val filtrada = Filtros.aplicar(
            lista,
            FiltroOportunidades(texto = "feria", estado = EstadoOportunidad.CONTACTO_REALIZADO)
        )
        assertEquals(listOf(1L), filtrada.map { it.id })
    }

    @Test
    fun filtros_porFechaDeteccion() {
        val lista = listOf(
            resumen(id = 1L, nombre = "A", fecha = "2026-09-01"),
            resumen(id = 2L, nombre = "B", fecha = "2026-10-05")
        )
        val filtrada = Filtros.aplicar(lista, FiltroOportunidades(desde = "2026-10-01"))
        assertEquals(listOf(2L), filtrada.map { it.id })
    }

    @Test
    fun filtros_porComunaORegionSinDistinguirMayusculas() {
        val lista = listOf(
            resumen(id = 1L, nombre = "A").copy(eventoComuna = "Maipú", eventoRegion = "Región Metropolitana"),
            resumen(id = 2L, nombre = "B").copy(eventoComuna = "Concepción", eventoRegion = "Biobío")
        )
        assertEquals(listOf(1L), Filtros.aplicar(lista, FiltroOportunidades(comuna = "maip")).map { it.id })
        assertEquals(listOf(2L), Filtros.aplicar(lista, FiltroOportunidades(comuna = "BIOBÍO")).map { it.id })
    }

    // ---------- Helpers ----------

    private fun oportunidad(id: Long, nombre: String = "Op $id", estado: EstadoOportunidad) = OportunidadEntity(
        id = id, nombre = nombre, tipo = TipoOportunidad.OTRA, descripcion = "", fuenteInformacion = "",
        fechaDeteccion = hoy, responsableId = null, prioridad = Prioridad.MEDIA,
        potencialComercial = 3, afinidadPublico = 3, estado = estado, eventoId = null,
        organizacionId = null, contactoId = null, fechaUltimaActualizacion = hoy,
        observaciones = "", creadoPorId = null
    )

    private fun seguimiento(oportunidadId: Long, fecha: String, hora: String, proximo: String?) = SeguimientoEntity(
        oportunidadId = oportunidadId, tipoAccion = TipoAccion.SEGUIMIENTO, fecha = fecha, hora = hora,
        resultado = "x", proximaAccion = "", fechaProximoSeguimiento = proximo, observaciones = "", usuarioId = null
    )

    private fun propuesta(id: Long, oportunidadId: Long, respondida: Boolean) = PropuestaEntity(
        id = id, oportunidadId = oportunidadId, fechaEnvio = "2026-10-10", descripcion = "d",
        condicionesParticipacion = "", costoReferencial = 0, respondida = respondida
    )

    private fun evento(id: Long, nombre: String, fechaInicio: String) = EventoEntity(
        id = id, nombre = nombre, tipo = TipoOportunidad.OTRA, descripcion = "", fechaInicio = fechaInicio,
        fechaTermino = fechaInicio, horario = "10:00-12:00", ubicacion = "", comuna = "Maipú",
        region = "Región Metropolitana", organizacionId = null, publicoEsperado = "", publicoObjetivo = "",
        cantidadAsistentes = 0, condicionesParticipacion = "", requisitos = "", costoReferencial = 0,
        espaciosDisponibles = 0, observaciones = ""
    )

    private fun resumen(
        id: Long,
        nombre: String,
        estado: EstadoOportunidad = EstadoOportunidad.EVENTO_IDENTIFICADO,
        fecha: String = hoy
    ) = OportunidadResumen(
        id = id, nombre = nombre, tipo = TipoOportunidad.OTRA, estado = estado, prioridad = Prioridad.MEDIA,
        fechaDeteccion = fecha, responsableId = null, responsableNombre = null, eventoNombre = null,
        eventoFecha = null, eventoComuna = null, eventoRegion = null, organizacionNombre = null,
        fechaUltimaActualizacion = fecha
    )
}
