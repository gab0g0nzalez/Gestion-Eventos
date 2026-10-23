package com.example.myapplication.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de las validaciones de formularios: qué entradas se aceptan y cuáles generan errores. */
class ValidarTest {

    // ---------- Correo ----------

    @Test
    fun correoValido_aceptaFormatoBasico() {
        assertTrue(Validar.correoValido("ana@ejemplo.cl"))
        assertTrue(Validar.correoValido("  ana@ejemplo.cl  ")) // se recorta antes de validar
    }

    @Test
    fun correoValido_rechazaFormatosInvalidos() {
        assertFalse(Validar.correoValido(""))
        assertFalse(Validar.correoValido("ana.ejemplo.cl"))
        assertFalse(Validar.correoValido("ana@ejemplo"))
        assertFalse(Validar.correoValido("ana @ejemplo.cl"))
        assertFalse(Validar.correoValido("@ejemplo.cl"))
    }

    // ---------- Registro de usuario ----------

    @Test
    fun registro_datosCompletosNoTieneErrores() {
        val errores = Validar.registro("Ana", "ana@ejemplo.cl", "123456", "Eventos", "Centro", true)
        assertTrue(errores.isEmpty())
    }

    @Test
    fun registro_claveDeMenosDeSeisCaracteresFalla() {
        val errores = Validar.registro("Ana", "ana@ejemplo.cl", "12345", "Eventos", "Centro", true)
        assertEquals(setOf("clave"), errores.keys)
    }

    @Test
    fun registro_sinTerminosFalla() {
        val errores = Validar.registro("Ana", "ana@ejemplo.cl", "123456", "Eventos", "Centro", false)
        assertEquals(setOf("terminos"), errores.keys)
    }

    @Test
    fun registro_camposVaciosSeReportanTodos() {
        val errores = Validar.registro("  ", "malo", "", " ", "", false)
        assertEquals(setOf("nombre", "correo", "clave", "area", "zona", "terminos"), errores.keys)
        assertEquals("Campo obligatorio", errores["nombre"])
    }

    // ---------- Oportunidad ----------

    private fun oportunidadValida() = DatosOportunidad(
        nombre = "Feria norte",
        fechaDeteccion = "2026-10-23",
        potencial = "3",
        afinidad = "4",
        eventoId = 1L
    )

    @Test
    fun oportunidad_datosValidosNoTienenErrores() {
        assertTrue(Validar.oportunidad(oportunidadValida()).isEmpty())
    }

    @Test
    fun oportunidad_potencialFueraDeRangoFalla() {
        assertEquals(setOf("potencial"), Validar.oportunidad(oportunidadValida().copy(potencial = "6")).keys)
        assertEquals(setOf("potencial"), Validar.oportunidad(oportunidadValida().copy(potencial = "0")).keys)
    }

    @Test
    fun oportunidad_potencialNoNumericoFalla() {
        assertEquals(setOf("potencial"), Validar.oportunidad(oportunidadValida().copy(potencial = "alto")).keys)
        assertEquals(setOf("potencial"), Validar.oportunidad(oportunidadValida().copy(potencial = "")).keys)
    }

    @Test
    fun oportunidad_limitesDeRangoSonInclusivos() {
        assertTrue(Validar.oportunidad(oportunidadValida().copy(potencial = "1", afinidad = "5")).isEmpty())
    }

    @Test
    fun oportunidad_fechaMalFormadaOImposibleFalla() {
        assertEquals(setOf("fechaDeteccion"), Validar.oportunidad(oportunidadValida().copy(fechaDeteccion = "23-10-2026")).keys)
        assertEquals(setOf("fechaDeteccion"), Validar.oportunidad(oportunidadValida().copy(fechaDeteccion = "2026-02-30")).keys)
    }

    @Test
    fun oportunidad_sinNombreOSinEventoFalla() {
        val errores = Validar.oportunidad(oportunidadValida().copy(nombre = " ", eventoId = null))
        assertEquals(setOf("nombre", "evento"), errores.keys)
    }

    // ---------- Evento ----------

    private fun eventoValido() = DatosEvento(
        nombre = "Feria",
        fechaInicio = "2026-11-01",
        fechaTermino = "2026-11-02",
        horario = "10:00-12:00",
        ubicacion = "Parque",
        comuna = "Providencia",
        region = "Metropolitana",
        organizacionId = 1L,
        asistentes = "100",
        costo = "0",
        espacios = "50"
    )

    @Test
    fun evento_datosValidosNoTienenErrores() {
        assertTrue(Validar.evento(eventoValido()).isEmpty())
    }

    @Test
    fun evento_terminoAnteriorAlInicioFalla() {
        val errores = Validar.evento(eventoValido().copy(fechaTermino = "2026-10-30"))
        assertEquals(setOf("fechaTermino"), errores.keys)
        assertEquals("Debe ser igual o posterior a la fecha de inicio", errores["fechaTermino"])
    }

    @Test
    fun evento_mismoDiaDeInicioYTerminoEsValido() {
        assertTrue(Validar.evento(eventoValido().copy(fechaTermino = "2026-11-01")).isEmpty())
    }

    @Test
    fun evento_horarioAceptaEspaciosAlrededorDelGuion() {
        assertTrue(Validar.evento(eventoValido().copy(horario = "10:00 - 12:00")).isEmpty())
    }

    @Test
    fun evento_horarioMalFormadoFalla() {
        assertEquals(setOf("horario"), Validar.evento(eventoValido().copy(horario = "10-12")).keys)
        assertEquals(setOf("horario"), Validar.evento(eventoValido().copy(horario = "25:00-26:00")).keys)
        assertEquals(setOf("horario"), Validar.evento(eventoValido().copy(horario = "9:00-12:00")).keys)
    }

    @Test
    fun evento_costoNegativoONoNumericoFalla() {
        assertEquals(setOf("costo"), Validar.evento(eventoValido().copy(costo = "-1")).keys)
        assertEquals(setOf("costo"), Validar.evento(eventoValido().copy(costo = "gratis")).keys)
    }

    @Test
    fun evento_asistentesYEspaciosFueraDeRangoFallan() {
        assertEquals(setOf("asistentes"), Validar.evento(eventoValido().copy(asistentes = "-5")).keys)
        assertEquals(setOf("espacios"), Validar.evento(eventoValido().copy(espacios = "10001")).keys)
    }

    @Test
    fun evento_sinOrganizacionFalla() {
        assertEquals(setOf("organizacion"), Validar.evento(eventoValido().copy(organizacionId = null)).keys)
    }

    // ---------- Organización ----------

    @Test
    fun organizacion_obligatoriosVacios() {
        val errores = Validar.organizacion(DatosOrganizacion(nombre = "Org", rubro = "", comuna = "X", region = ""))
        assertEquals(setOf("rubro", "region"), errores.keys)
        assertTrue(Validar.organizacion(DatosOrganizacion("Org", rubro = "Salud", comuna = "X", region = "Y")).isEmpty())
    }

    // ---------- Contacto ----------

    private fun contactoValido() = DatosContacto(
        nombre = "Ana",
        organizacionId = 1L,
        cargo = "Gerente",
        correo = "ana@ejemplo.cl"
    )

    @Test
    fun contacto_conCorreoOTelefonoEsValido() {
        assertTrue(Validar.contacto(contactoValido()).isEmpty())
        assertTrue(Validar.contacto(contactoValido().copy(correo = "", telefono = "+56 9 0000 0000")).isEmpty())
    }

    @Test
    fun contacto_sinCorreoNiTelefonoFalla() {
        val errores = Validar.contacto(contactoValido().copy(correo = " ", telefono = ""))
        assertEquals(setOf("medio"), errores.keys)
    }

    @Test
    fun contacto_correoInvalidoFalla() {
        assertEquals(setOf("correo"), Validar.contacto(contactoValido().copy(correo = "ana@")).keys)
    }

    @Test
    fun contacto_sinOrganizacionFalla() {
        assertEquals(setOf("organizacion"), Validar.contacto(contactoValido().copy(organizacionId = null)).keys)
    }

    // ---------- Seguimiento ----------

    private fun seguimientoValido() = DatosSeguimiento(
        oportunidadId = 1L,
        fecha = "2026-10-23",
        hora = "09:30",
        resultado = "Llamada realizada"
    )

    @Test
    fun seguimiento_datosValidosNoTienenErrores() {
        assertTrue(Validar.seguimiento(seguimientoValido()).isEmpty())
    }

    @Test
    fun seguimiento_horaInvalidaFalla() {
        assertEquals(setOf("hora"), Validar.seguimiento(seguimientoValido().copy(hora = "9:30")).keys)
        assertEquals(setOf("hora"), Validar.seguimiento(seguimientoValido().copy(hora = "24:00")).keys)
    }

    @Test
    fun seguimiento_resultadoObligatorio() {
        assertEquals(setOf("resultado"), Validar.seguimiento(seguimientoValido().copy(resultado = "")).keys)
    }

    @Test
    fun seguimiento_proximoSeguimientoOpcionalPeroNoAnterior() {
        assertTrue(Validar.seguimiento(seguimientoValido().copy(fechaProximoSeguimiento = "")).isEmpty())
        assertTrue(Validar.seguimiento(seguimientoValido().copy(fechaProximoSeguimiento = "2026-10-23")).isEmpty())
        assertEquals(
            setOf("fechaProximoSeguimiento"),
            Validar.seguimiento(seguimientoValido().copy(fechaProximoSeguimiento = "2026-10-22")).keys
        )
        assertEquals(
            setOf("fechaProximoSeguimiento"),
            Validar.seguimiento(seguimientoValido().copy(fechaProximoSeguimiento = "pronto")).keys
        )
    }

    // ---------- Propuesta ----------

    @Test
    fun propuesta_validaYCamposObligatorios() {
        val valida = DatosPropuesta(oportunidadId = 1L, fechaEnvio = "2026-10-23", descripcion = "Stand", costo = "150000")
        assertTrue(Validar.propuesta(valida).isEmpty())
        assertEquals(setOf("descripcion"), Validar.propuesta(valida.copy(descripcion = "")).keys)
        assertEquals(setOf("costo"), Validar.propuesta(valida.copy(costo = "-10")).keys)
        assertEquals(setOf("fechaEnvio"), Validar.propuesta(valida.copy(fechaEnvio = "hoy")).keys)
    }

    // ---------- Resultado ----------

    private fun resultadoValido() = DatosResultado(
        oportunidadId = 1L,
        fechaRealizacion = "2026-10-23",
        estadoFinal = "Realizada",
        asistentes = "120",
        contactos = "10",
        interesadas = "5",
        inscripciones = "3"
    )

    @Test
    fun resultado_datosValidosNoTienenErrores() {
        assertTrue(Validar.resultado(resultadoValido()).isEmpty())
    }

    @Test
    fun resultado_contadoresNegativosONoNumericosFallan() {
        val errores = Validar.resultado(resultadoValido().copy(asistentes = "-1", inscripciones = "tres"))
        assertEquals(setOf("asistentes", "inscripciones"), errores.keys)
    }

    @Test
    fun resultado_estadoFinalObligatorio() {
        assertEquals(setOf("estadoFinal"), Validar.resultado(resultadoValido().copy(estadoFinal = " ")).keys)
    }
}
