package com.example.myapplication.data

/** Tipos de oportunidad (también usados como tipo de evento). Caso DSY1105, sección 3.2. */
enum class TipoOportunidad(val etiqueta: String) {
    EVENTO_CORPORATIVO("Evento corporativo"),
    FERIA_EMPRESARIAL("Feria empresarial"),
    ACTIVIDAD_MUNICIPAL("Actividad municipal"),
    JORNADA_BENEFICIOS("Jornada de beneficios"),
    FERIA_INSTITUCIONAL("Feria institucional"),
    ACTIVIDAD_TRABAJADORES("Actividad de trabajadores"),
    ACTIVACION_COMERCIAL("Activación comercial"),
    PATROCINIO("Patrocinio"),
    OTRA("Otra instancia relevante")
}

/** Estados de la oportunidad. Los marcados como cerrados no aparecen en alertas de gestión activa. */
enum class EstadoOportunidad(val etiqueta: String, val cerrado: Boolean = false) {
    EVENTO_IDENTIFICADO("Evento identificado"),
    CONTACTO_PENDIENTE("Contacto pendiente"),
    CONTACTO_REALIZADO("Contacto realizado"),
    PROPUESTA_ENVIADA("Propuesta enviada"),
    EN_ESPERA_RESPUESTA("En espera de respuesta"),
    PARTICIPACION_CONFIRMADA("Participación confirmada"),
    DESCARTADA("Oportunidad descartada", cerrado = true),
    EN_EVALUACION("En evaluación"),
    INFORMACION_INCOMPLETA("Información incompleta"),
    REQUIERE_SEGUIMIENTO("Requiere seguimiento"),
    ACTIVACION_REALIZADA("Activación realizada", cerrado = true),
    ACTIVACION_CANCELADA("Activación cancelada", cerrado = true),
    OPORTUNIDAD_CERRADA("Oportunidad cerrada", cerrado = true)
}

enum class Prioridad(val etiqueta: String) {
    ALTA("Alta"),
    MEDIA("Media"),
    BAJA("Baja")
}

enum class TipoOrganizacion(val etiqueta: String) {
    EMPRESA("Empresa"),
    MUNICIPALIDAD("Municipalidad"),
    INSTITUCION_PUBLICA("Institución pública"),
    INSTITUCION_PRIVADA("Institución privada"),
    ORGANIZADOR_EVENTOS("Organizador de eventos"),
    CENTRO_COMERCIAL("Centro comercial"),
    ASOCIACION("Asociación"),
    OTRA("Otra entidad")
}

enum class AreaContacto(val etiqueta: String) {
    EVENTOS("Eventos"),
    ACTIVACIONES("Activaciones"),
    MARKETING("Marketing"),
    COMUNICACIONES("Comunicaciones"),
    RECURSOS_HUMANOS("Recursos Humanos"),
    BIENESTAR("Bienestar"),
    BENEFICIOS("Beneficios"),
    ADMINISTRACION("Administración")
}

enum class TipoAccion(val etiqueta: String) {
    INVESTIGACION_INICIAL("Investigación inicial"),
    BUSQUEDA_CONTACTO("Búsqueda de contacto"),
    PRIMER_CONTACTO("Primer contacto"),
    ENVIO_PRESENTACION("Envío de presentación"),
    ENVIO_PROPUESTA("Envío de propuesta"),
    SOLICITUD_ANTECEDENTES("Solicitud de antecedentes"),
    REUNION("Reunión"),
    LLAMADA("Llamada"),
    SEGUIMIENTO("Seguimiento"),
    RECEPCION_RESPUESTA("Recepción de respuesta"),
    CONFIRMACION("Confirmación"),
    CIERRE("Cierre")
}

enum class Perfil(val etiqueta: String) {
    COMERCIAL("Ejecutivo comercial"),
    SUPERVISOR("Supervisor o responsable"),
    MARKETING("Marketing o comunicaciones")
}

enum class MedioContacto(val etiqueta: String) {
    CORREO("Correo"),
    TELEFONO("Teléfono"),
    PRESENCIAL("Presencial"),
    REDES_SOCIALES("Redes sociales")
}
