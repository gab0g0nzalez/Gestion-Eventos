package com.example.myapplication.navigation

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val INICIO = "inicio"
    const val OPORTUNIDADES = "oportunidades"
    const val OPORTUNIDAD = "oportunidad/{id}"
    const val FORMULARIO_OPORTUNIDAD = "oportunidad_form/{id}"
    const val EVENTOS = "eventos"
    const val EVENTO_NUEVO = "evento_nuevo"
    const val ORGANIZACIONES = "organizaciones"
    const val ORGANIZACION_NUEVA = "organizacion_nueva"
    const val CONTACTOS = "contactos"
    const val CONTACTO_NUEVO = "contacto_nuevo"
    const val ALERTAS = "alertas"
    const val PERFIL = "perfil"

    const val QR = "qr"

    /** Id 0 significa "nueva oportunidad". */
    fun oportunidad(id: Long) = "oportunidad/$id"
    fun formularioOportunidad(id: Long) = "oportunidad_form/$id"
}
