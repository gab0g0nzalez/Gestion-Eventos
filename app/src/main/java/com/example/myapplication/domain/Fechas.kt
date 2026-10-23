package com.example.myapplication.domain

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Utilidades de fecha. Se usan cadenas ISO para que compararlas en orden cronológico sea directo. */
object Fechas {
    private const val FORMATO_FECHA = "yyyy-MM-dd"
    private const val FORMATO_FECHA_HORA = "yyyy-MM-dd HH:mm"
    private val regexFecha = Regex("^\\d{4}-\\d{2}-\\d{2}$")
    private val regexHora = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")

    fun hoy(): String = SimpleDateFormat(FORMATO_FECHA, Locale.US).format(Date())

    fun ahora(): String = SimpleDateFormat(FORMATO_FECHA_HORA, Locale.US).format(Date())

    fun horaActual(): String = SimpleDateFormat("HH:mm", Locale.US).format(Date())

    fun sumarDias(fecha: String, dias: Int): String {
        val calendario = Calendar.getInstance(Locale.US)
        calendario.time = parsear(fecha) ?: Date()
        calendario.add(Calendar.DAY_OF_YEAR, dias)
        return SimpleDateFormat(FORMATO_FECHA, Locale.US).format(calendario.time)
    }

    fun esValida(fecha: String): Boolean {
        if (!regexFecha.matches(fecha)) return false
        return parsear(fecha) != null
    }

    fun esHoraValida(hora: String): Boolean = regexHora.matches(hora)

    private fun parsear(fecha: String): Date? {
        return try {
            SimpleDateFormat(FORMATO_FECHA, Locale.US).apply { isLenient = false }.parse(fecha)
        } catch (e: ParseException) {
            null
        }
    }
}
