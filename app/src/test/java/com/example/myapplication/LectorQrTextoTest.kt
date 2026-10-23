package com.example.myapplication.domain

import com.example.myapplication.data.AreaContacto
import com.example.myapplication.data.ContactoEntity
import com.example.myapplication.data.EstadoOportunidad
import com.example.myapplication.data.EventoEntity
import com.example.myapplication.data.MedioContacto
import com.example.myapplication.data.Prioridad
import com.example.myapplication.data.TipoOportunidad
import com.example.myapplication.data.UsuarioEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas del lector de QR con contenido en texto "clave: valor" (no requiere org.json). */
class LectorQrTextoTest {

    private val eventos = listOf(
        EventoEntity(
            id = 7, nombre = "Feria Norte", tipo = TipoOportunidad.FERIA_EMPRESARIAL,
            descripcion = "", fechaInicio = "2026-11-01", fechaTermino = "2026-11-02",
            horario = "10:00-12:00", ubicacion = "", comuna = "Providencia", region = "RM",
            organizacionId = 3L, publicoEsperado = "", publicoObjetivo = "",
            cantidadAsistentes = 0, condicionesParticipacion = "", requisitos = "",
            costoReferencial = 0, espaciosDisponibles = 0, observaciones = ""
        )
    )

    private val contactos = listOf(
        ContactoEntity(
            id = 9, nombre = "Ana Pérez", organizacionId = 3L, cargo = "Gerente",
            area = AreaContacto.EVENTOS, correo = "ana@ejemplo.cl", telefono = "",
            medioPreferido = MedioContacto.CORREO, fechaUltimoContacto = null, observaciones = ""
        )
    )

    private val usuarios = listOf(
        UsuarioEntity(
            id = 5, nombre = "Luis Soto", correo = "luis@ejemplo.cl", claveHash = "x",
            area = "Eventos", zona = "Centro", fechaCreacion = "2026-01-01", fechaActualizacion = "2026-01-01"
        )
    )

    private val base = DatosOportunidad(nombre = "Original", observaciones = "obs previa", fuente = "")

    private fun interpretar(qr: String, actual: DatosOportunidad = base) =
        LectorQr.interpretar(qr, actual, eventos, contactos, usuarios)

    // ---------- Separadores y claves ----------

    @Test
    fun leerCampos_separaPorSaltoDeLinea() {
        val campos = LectorQr.leerCampos("nombre: Feria Norte\ntipo: Feria\nprioridad=ALTA")
        assertEquals("Feria Norte", campos["nombre"])
        assertEquals("Feria", campos["tipo"])
        assertEquals("ALTA", campos["prioridad"])
    }

    @Test
    fun leerCampos_separaPorPuntoYComaYBarra() {
        val campos = LectorQr.leerCampos("nombre: A; estado: nueva | responsable: Ana")
        assertEquals("A", campos["nombre"])
        assertEquals("nueva", campos["estado"])
        assertEquals("Ana", campos["responsable"])
    }

    @Test
    fun leerCampos_ignoraLineasSinSeparador() {
        val campos = LectorQr.leerCampos("texto suelto\nnombre: Válida")
        assertEquals(setOf("nombre"), campos.keys)
    }

    @Test
    fun leerCampos_conservaDosPuntosDentroDelValor() {
        val campos = LectorQr.leerCampos("horario: 10:00-12:00")
        assertEquals("10:00-12:00", campos["horario"])
    }

    @Test
    fun normalizarClave_quitaTildesMayusculasYSignos() {
        assertEquals("descripcion", LectorQr.normalizarClave("Descripción"))
        assertEquals("fecha deteccion", LectorQr.normalizarClave("  Fecha_Detección "))
        assertEquals("evento id", LectorQr.normalizarClave("evento-ID"))
    }

    // ---------- Interpretación ----------

    @Test
    fun interpretar_enlazaEventoContactoYResponsablePorNombreNormalizado() {
        val r = interpretar("evento: feria norte\ncontacto: ANA PEREZ\nresponsable: luis soto")
        assertEquals(7L, r.datos.eventoId)
        assertEquals(9L, r.datos.contactoId)
        assertEquals(5L, r.datos.responsableId)
    }

    @Test
    fun interpretar_idNumericoTienePrioridadSobreElNombre() {
        val r = interpretar("eventoId: 7\nevento: Otro evento que no existe")
        assertEquals(7L, r.datos.eventoId)
    }

    @Test
    fun interpretar_eventoDesconocidoConservaElValorActual() {
        val actual = base.copy(eventoId = 99L)
        val r = interpretar("evento: No existe", actual)
        assertEquals(99L, r.datos.eventoId)
    }

    @Test
    fun interpretar_tipoPorNombreOEtiqueta() {
        assertEquals(TipoOportunidad.FERIA_EMPRESARIAL, interpretar("tipo: FERIA_EMPRESARIAL").datos.tipo)
        assertEquals(TipoOportunidad.FERIA_EMPRESARIAL, interpretar("tipo: feria empresarial").datos.tipo)
    }

    @Test
    fun interpretar_prioridadYEstadoPorNombreOEtiqueta() {
        val r = interpretar("prioridad: alta\nestado: Propuesta enviada")
        assertEquals(Prioridad.ALTA, r.datos.prioridad)
        assertEquals(EstadoOportunidad.PROPUESTA_ENVIADA, r.datos.estado)
    }

    @Test
    fun interpretar_estadoNuevaSeMapeaAEventoIdentificado() {
        assertEquals(EstadoOportunidad.EVENTO_IDENTIFICADO, interpretar("estado: NUEVA").datos.estado)
    }

    @Test
    fun interpretar_valoresDesconocidosConservanElActual() {
        val actual = base.copy(prioridad = Prioridad.BAJA, tipo = TipoOportunidad.PATROCINIO)
        val r = interpretar("prioridad: urgentísima\ntipo: algo raro", actual)
        assertEquals(Prioridad.BAJA, r.datos.prioridad)
        assertEquals(TipoOportunidad.PATROCINIO, r.datos.tipo)
    }

    @Test
    fun interpretar_fuenteSeLlenaConCodigoQrSiNoViene() {
        assertEquals("Código QR", interpretar("nombre: X").datos.fuente)
        assertEquals("Diario", interpretar("fuente: Diario").datos.fuente)
    }

    @Test
    fun interpretar_camposDeTextoYPuntuacionSeCopian() {
        val r = interpretar("nombre: Nueva\ndescripción: Detalle\npotencial: 4\nafinidad: 2\nfecha: 2026-12-01\nnotas: sin novedades")
        assertEquals("Nueva", r.datos.nombre)
        assertEquals("Detalle", r.datos.descripcion)
        assertEquals("4", r.datos.potencial)
        assertEquals("2", r.datos.afinidad)
        assertEquals("2026-12-01", r.datos.fechaDeteccion)
        assertEquals("sin novedades", r.datos.observaciones)
    }

    @Test
    fun interpretar_contenidoSinCamposReconocidosAvisaYNoModifica() {
        val r = interpretar("hola mundo")
        assertTrue(r.mensaje.startsWith("Se leyó el QR"))
        assertEquals(base, r.datos)
    }

    @Test
    fun interpretar_contenidoReconocidoDaMensajeDeExito() {
        val r = interpretar("nombre: X")
        assertEquals("Datos del QR cargados. Revisa los campos antes de guardar.", r.mensaje)
    }

    @Test
    fun interpretar_sinCatalogosNoInventaRelaciones() {
        val r = LectorQr.interpretar("evento: Feria Norte\ncontacto: Ana Pérez", base, emptyList(), emptyList(), emptyList())
        assertNull(r.datos.eventoId)
        assertNull(r.datos.contactoId)
    }
}
