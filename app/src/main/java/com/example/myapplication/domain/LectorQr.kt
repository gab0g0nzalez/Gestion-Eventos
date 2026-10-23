package com.example.myapplication.domain

import com.example.myapplication.data.ContactoEntity
import com.example.myapplication.data.EstadoOportunidad
import com.example.myapplication.data.EventoEntity
import com.example.myapplication.data.Prioridad
import com.example.myapplication.data.TipoOportunidad
import com.example.myapplication.data.UsuarioEntity
import org.json.JSONObject
import java.text.Normalizer

/**
 * Interpreta el contenido de un código QR y lo convierte en datos de oportunidad.
 * Acepta un objeto JSON o texto con pares "clave: valor" / "clave=valor",
 * separados por salto de línea, ";" o "|".
 */
object LectorQr {

    data class Resultado(val datos: DatosOportunidad, val mensaje: String)

    fun interpretar(
        contenido: String,
        actual: DatosOportunidad,
        eventos: List<EventoEntity>,
        contactos: List<ContactoEntity>,
        usuarios: List<UsuarioEntity>
    ): Resultado {
        val campos = leerCampos(contenido)

        fun campo(vararg nombres: String): String? =
            nombres.firstNotNullOfOrNull { nombre ->
                campos[normalizarClave(nombre)]?.takeIf { it.isNotBlank() }
            }

        val tipoTexto = campo("tipo", "tipo oportunidad", "tipo_oportunidad")
        val prioridadTexto = campo("prioridad")
        val estadoTexto = campo("estado")
        val eventoTexto = campo("evento", "evento asociado", "nombre evento")
        val contactoTexto = campo("contacto", "contacto corporativo", "nombre contacto")
        val responsableTexto = campo("responsable", "responsable interno")

        // Los IDs numéricos del QR tienen prioridad sobre los nombres
        val eventoIdTexto = campo("eventoId", "evento id")?.toLongOrNull()
        val contactoIdTexto = campo("contactoId", "contacto id")?.toLongOrNull()
        val responsableIdTexto = campo("responsableId", "responsable id")?.toLongOrNull()

        val evento = eventoIdTexto?.let { id -> eventos.firstOrNull { it.id == id } }
            ?: eventoTexto?.let { valor ->
                eventos.firstOrNull { normalizarClave(it.nombre) == normalizarClave(valor) }
            }
        val contacto = contactoIdTexto?.let { id -> contactos.firstOrNull { it.id == id } }
            ?: contactoTexto?.let { valor ->
                contactos.firstOrNull { normalizarClave(it.nombre) == normalizarClave(valor) }
            }
        val responsable = responsableIdTexto?.let { id -> usuarios.firstOrNull { it.id == id } }
            ?: responsableTexto?.let { valor ->
                usuarios.firstOrNull { normalizarClave(it.nombre) == normalizarClave(valor) }
            }

        val datos = actual.copy(
            nombre = campo("nombre", "nombre oportunidad", "oportunidad") ?: actual.nombre,
            tipo = tipoTexto?.let { valor ->
                TipoOportunidad.entries.firstOrNull { coincide(it.name, it.etiqueta, valor) }
            } ?: actual.tipo,
            descripcion = campo("descripcion", "descripción") ?: actual.descripcion,
            fuente = campo("fuente", "fuente informacion", "fuente de información") ?: "Código QR",
            fechaDeteccion = campo("fechaDeteccion", "fecha deteccion", "fecha detección", "fecha")
                ?: actual.fechaDeteccion,
            prioridad = prioridadTexto?.let { valor ->
                Prioridad.entries.firstOrNull { coincide(it.name, it.etiqueta, valor) }
            } ?: actual.prioridad,
            potencial = campo("potencial", "potencial comercial") ?: actual.potencial,
            afinidad = campo("afinidad", "afinidad publico", "afinidad público", "afinidad con el público objetivo")
                ?: actual.afinidad,
            // "NUEVA" es el estado inicial de una oportunidad recién detectada
            estado = estadoTexto?.let { valor ->
                if (normalizarClave(valor) == "nueva") EstadoOportunidad.EVENTO_IDENTIFICADO
                else EstadoOportunidad.entries.firstOrNull { coincide(it.name, it.etiqueta, valor) }
            } ?: actual.estado,
            eventoId = evento?.id ?: actual.eventoId,
            contactoId = contacto?.id ?: actual.contactoId,
            responsableId = responsable?.id ?: actual.responsableId,
            observaciones = campo("observaciones", "notas") ?: actual.observaciones
        )

        val reconocidos = listOf(
            "nombre", "descripcion", "tipo", "evento", "contacto", "responsable",
            "fecha", "prioridad", "estado", "potencial", "afinidad", "observaciones"
        ).any { campos.containsKey(normalizarClave(it)) }

        val mensaje = if (reconocidos) {
            "Datos del QR cargados. Revisa los campos antes de guardar."
        } else {
            "Se leyó el QR, pero no se reconocieron campos. Usa JSON o pares clave: valor."
        }
        return Resultado(datos, mensaje)
    }

    /** Acepta un objeto JSON o texto con pares clave: valor / clave=valor. */
    fun leerCampos(contenido: String): Map<String, String> {
        val resultado = mutableMapOf<String, String>()
        val texto = contenido.trim()
        try {
            val json = JSONObject(texto)
            val claves = json.keys()
            while (claves.hasNext()) {
                val clave = claves.next()
                val valor = json.opt(clave)
                if (valor != null && valor != JSONObject.NULL && valor !is JSONObject) {
                    resultado[normalizarClave(clave)] = valor.toString().trim()
                }
            }
            return resultado
        } catch (_: Exception) {
            // No es JSON: se procesa como texto legible.
        }

        texto.split("\n", ";", "|").forEach { linea ->
            val separador = when {
                linea.contains(":") -> ":"
                linea.contains("=") -> "="
                else -> null
            }
            if (separador != null) {
                val partes = linea.split(separador, limit = 2)
                if (partes.size == 2) resultado[normalizarClave(partes[0])] = partes[1].trim()
            }
        }
        return resultado
    }

    fun normalizarClave(valor: String): String =
        Normalizer.normalize(valor.trim().lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()

    private fun coincide(nombre: String, etiqueta: String, valor: String): Boolean {
        val buscado = normalizarClave(valor)
        return normalizarClave(nombre) == buscado || normalizarClave(etiqueta) == buscado
    }
}
