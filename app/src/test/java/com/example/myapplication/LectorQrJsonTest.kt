package com.example.myapplication.domain

import com.example.myapplication.data.Prioridad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Pruebas del lector de QR con contenido JSON.
 * Requiere la implementación real de org.json en pruebas JVM:
 *   testImplementation("org.json:json:20231013")
 * (El android.jar que se usa en pruebas locales lanza "Stub!" para org.json.)
 */
class LectorQrJsonTest {

    private val base = DatosOportunidad(nombre = "Original", observaciones = "previa")

    @Test
    fun leerCampos_objetoJsonPlano() {
        val campos = LectorQr.leerCampos("""{"Nombre":"Feria","prioridad":"BAJA","eventoId":7}""")
        assertEquals("Feria", campos["nombre"])
        assertEquals("BAJA", campos["prioridad"])
        assertEquals("7", campos["eventoid"])
    }

    @Test
    fun leerCampos_ignoraValoresNulosYObjetosAnidados() {
        val campos = LectorQr.leerCampos("""{"nombre":"X","nota":null,"extra":{"a":1}}""")
        assertEquals(setOf("nombre"), campos.keys)
    }

    @Test
    fun interpretar_jsonActualizaPrioridadYNombre() {
        val r = LectorQr.interpretar("""{"nombre":"Nueva","prioridad":"alta"}""", base, emptyList(), emptyList(), emptyList())
        assertEquals("Nueva", r.datos.nombre)
        assertEquals(Prioridad.ALTA, r.datos.prioridad)
        assertEquals("previa", r.datos.observaciones)
        assertFalse(r.mensaje.startsWith("Se leyó el QR"))
    }
}
