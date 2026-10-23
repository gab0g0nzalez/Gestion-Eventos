package com.example.myapplication.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de validación de fechas (AAAA-MM-DD) y horas (HH:MM). */
class FechasTest {

    @Test
    fun esValida_aceptaFechasReales() {
        assertTrue(Fechas.esValida("2026-10-23"))
        assertTrue(Fechas.esValida("2024-02-29")) // año bisiesto
    }

    @Test
    fun esValida_rechazaFechasImposibles() {
        assertFalse(Fechas.esValida("2026-02-30"))
        assertFalse(Fechas.esValida("2025-02-29"))
        assertFalse(Fechas.esValida("2026-13-01"))
    }

    @Test
    fun esValida_rechazaFormatosDistintosAAAAMMDD() {
        assertFalse(Fechas.esValida("23-10-2026"))
        assertFalse(Fechas.esValida("2026/10/23"))
        assertFalse(Fechas.esValida("2026-1-3"))
        assertFalse(Fechas.esValida("2026-10-23 "))
        assertFalse(Fechas.esValida(""))
    }

    @Test
    fun esHoraValida_aceptaHorasDelDia() {
        assertTrue(Fechas.esHoraValida("00:00"))
        assertTrue(Fechas.esHoraValida("09:30"))
        assertTrue(Fechas.esHoraValida("23:59"))
    }

    @Test
    fun esHoraValida_rechazaHorasInvalidas() {
        assertFalse(Fechas.esHoraValida("24:00"))
        assertFalse(Fechas.esHoraValida("12:60"))
        assertFalse(Fechas.esHoraValida("9:30"))
        assertFalse(Fechas.esHoraValida("0930"))
    }

    @Test
    fun sumarDias_cruzaFinDeAnio() {
        assertEquals("2027-01-01", Fechas.sumarDias("2026-12-31", 1))
    }

    @Test
    fun sumarDias_negativoRetrocede() {
        assertEquals("2026-02-28", Fechas.sumarDias("2026-03-01", -1))
    }
}
