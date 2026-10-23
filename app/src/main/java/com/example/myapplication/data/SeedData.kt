package com.example.myapplication.data

import com.example.myapplication.domain.Fechas
import com.example.myapplication.domain.Seguridad

/**
 * Datos 100% ficticios para demostración y pruebas (caso DSY1105, sección 7).
 * No corresponden a personas, empresas ni contactos reales. Las fechas son relativas a hoy
 * para que las alertas se vean con datos recientes.
 * Credenciales de prueba: comercial@ejemplo.com / 123456
 */
object SeedData {

    suspend fun cargar(dao: AppDao) {
        val ahora = Fechas.ahora()
        val hoy = Fechas.hoy()
        fun d(dias: Int): String = Fechas.sumarDias(hoy, dias)

        // ---------- Usuarios ficticios ----------
        val comercial = dao.insertarUsuario(
            UsuarioEntity(
                nombre = "Ejecutivo Demo", correo = "comercial@ejemplo.com",
                claveHash = Seguridad.hash("123456"), perfil = Perfil.COMERCIAL,
                area = "Convenios corporativos", zona = "Región Metropolitana",
                fechaCreacion = ahora, fechaActualizacion = ahora
            )
        )
        val supervisor = dao.insertarUsuario(
            UsuarioEntity(
                nombre = "Supervisora Demo", correo = "supervisor@ejemplo.com",
                claveHash = Seguridad.hash("123456"), perfil = Perfil.SUPERVISOR,
                area = "Supervisión comercial", zona = "Región Metropolitana",
                fechaCreacion = ahora, fechaActualizacion = ahora
            )
        )
        dao.insertarUsuario(
            UsuarioEntity(
                nombre = "Marketing Demo", correo = "marketing@ejemplo.com",
                claveHash = Seguridad.hash("123456"), perfil = Perfil.MARKETING,
                area = "Comunicaciones", zona = "Valparaíso",
                fechaCreacion = ahora, fechaActualizacion = ahora
            )
        )

        // ---------- Organizaciones ficticias ----------
        val org1 = dao.insertarOrganizacion(
            OrganizacionEntity(
                nombre = "Clínica Dental Demo Norte", tipo = TipoOrganizacion.EMPRESA,
                rubro = "Salud dental", comuna = "Puente Alto", region = "Región Metropolitana",
                sitioWeb = "https://ejemplo.com/clinica-demo", publicoAsociado = "Trabajadores y familias",
                observaciones = "Registro ficticio"
            )
        )
        val org2 = dao.insertarOrganizacion(
            OrganizacionEntity(
                nombre = "Municipalidad Demo", tipo = TipoOrganizacion.MUNICIPALIDAD,
                rubro = "Gobierno local", comuna = "Maipú", region = "Región Metropolitana",
                sitioWeb = "https://ejemplo.com/municipio-demo", publicoAsociado = "Vecinos",
                observaciones = "Registro ficticio"
            )
        )
        val org3 = dao.insertarOrganizacion(
            OrganizacionEntity(
                nombre = "Servicios Automotrices Demo Ltda.", tipo = TipoOrganizacion.EMPRESA,
                rubro = "Servicios automotrices", comuna = "Santiago", region = "Región Metropolitana",
                sitioWeb = "", publicoAsociado = "Trabajadores del sector",
                observaciones = "Registro ficticio"
            )
        )
        val org4 = dao.insertarOrganizacion(
            OrganizacionEntity(
                nombre = "Fundación Educativa Demo", tipo = TipoOrganizacion.ASOCIACION,
                rubro = "Educación", comuna = "Concepción", region = "Biobío",
                sitioWeb = "", publicoAsociado = "Comunidad educativa",
                observaciones = "Registro ficticio"
            )
        )

        // ---------- Contactos ficticios ----------
        val c1 = dao.insertarContacto(
            ContactoEntity(
                nombre = "Contacto Demo A", organizacionId = org1, cargo = "Coordinadora de bienestar",
                area = AreaContacto.BIENESTAR, correo = "contacto.a@ejemplo.com",
                telefono = "+56 9 0000 0001", medioPreferido = MedioContacto.CORREO,
                fechaUltimoContacto = null, observaciones = ""
            )
        )
        val c2 = dao.insertarContacto(
            ContactoEntity(
                nombre = "Contacto Demo B", organizacionId = org2, cargo = "Jefa de desarrollo comunitario",
                area = AreaContacto.EVENTOS, correo = "contacto.b@ejemplo.com",
                telefono = "+56 9 0000 0002", medioPreferido = MedioContacto.TELEFONO,
                fechaUltimoContacto = null, observaciones = ""
            )
        )
        val c3 = dao.insertarContacto(
            ContactoEntity(
                nombre = "Contacto Demo C", organizacionId = org3, cargo = "Encargado de recursos humanos",
                area = AreaContacto.RECURSOS_HUMANOS, correo = "contacto.c@ejemplo.com",
                telefono = "+56 9 0000 0003", medioPreferido = MedioContacto.CORREO,
                fechaUltimoContacto = null, observaciones = ""
            )
        )
        val c4 = dao.insertarContacto(
            ContactoEntity(
                nombre = "Contacto Demo D", organizacionId = org4, cargo = "Coordinador de actividades",
                area = AreaContacto.EVENTOS, correo = "contacto.d@ejemplo.com",
                telefono = "+56 9 0000 0004", medioPreferido = MedioContacto.PRESENCIAL,
                fechaUltimoContacto = null, observaciones = ""
            )
        )

        // ---------- Eventos ficticios ----------
        val e1 = dao.insertarEvento(
            EventoEntity(
                nombre = "Feria de beneficios demo", tipo = TipoOportunidad.FERIA_EMPRESARIAL,
                descripcion = "Feria ficticia de convenios para trabajadores",
                fechaInicio = d(5), fechaTermino = d(5), horario = "10:00-18:00",
                ubicacion = "Espacio ficticio central", comuna = "Puente Alto",
                region = "Región Metropolitana", organizacionId = org1,
                publicoEsperado = "Trabajadores y familias", publicoObjetivo = "Trabajadores de empresas",
                cantidadAsistentes = 300, condicionesParticipacion = "Stand sujeto a disponibilidad",
                requisitos = "Propuesta enviada con 3 días de anticipación",
                costoReferencial = 150000, espaciosDisponibles = 4, observaciones = "Dato ficticio"
            )
        )
        val e2 = dao.insertarEvento(
            EventoEntity(
                nombre = "Jornada municipal de salud demo", tipo = TipoOportunidad.JORNADA_BENEFICIOS,
                descripcion = "Jornada ficticia de beneficios para vecinos",
                fechaInicio = d(20), fechaTermino = d(20), horario = "09:00-14:00",
                ubicacion = "Plaza ficticia", comuna = "Maipú", region = "Región Metropolitana",
                organizacionId = org2, publicoEsperado = "Vecinos y familias",
                publicoObjetivo = "Vecinos de la comuna", cantidadAsistentes = 500,
                condicionesParticipacion = "Inscripción previa", requisitos = "Stand de 3 metros",
                costoReferencial = 80000, espaciosDisponibles = 6, observaciones = "Dato ficticio"
            )
        )
        val e3 = dao.insertarEvento(
            EventoEntity(
                nombre = "Actividad de trabajadores demo", tipo = TipoOportunidad.ACTIVIDAD_TRABAJADORES,
                descripcion = "Actividad ficticia de bienestar",
                fechaInicio = d(40), fechaTermino = d(40), horario = "12:00-16:00",
                ubicacion = "Sala ficticia", comuna = "Santiago", region = "Región Metropolitana",
                organizacionId = org3, publicoEsperado = "150 trabajadores",
                publicoObjetivo = "Trabajadores del sector", cantidadAsistentes = 150,
                condicionesParticipacion = "Cupos limitados", requisitos = "Confirmar aforo",
                costoReferencial = 60000, espaciosDisponibles = 2, observaciones = "Dato ficticio"
            )
        )
        val e4 = dao.insertarEvento(
            EventoEntity(
                nombre = "Feria institucional demo", tipo = TipoOportunidad.FERIA_INSTITUCIONAL,
                descripcion = "Feria ficticia institucional",
                fechaInicio = d(10), fechaTermino = d(10), horario = "11:00-17:00",
                ubicacion = "Recinto ficticio", comuna = "Concepción", region = "Biobío",
                organizacionId = org4, publicoEsperado = "800 personas",
                publicoObjetivo = "Comunidad educativa", cantidadAsistentes = 800,
                condicionesParticipacion = "Postulación", requisitos = "Documentos de postulación",
                costoReferencial = 120000, espaciosDisponibles = 5, observaciones = "Dato ficticio"
            )
        )
        val e5 = dao.insertarEvento(
            EventoEntity(
                nombre = "Evento corporativo demo finalizado", tipo = TipoOportunidad.EVENTO_CORPORATIVO,
                descripcion = "Evento ficticio ya realizado",
                fechaInicio = d(-10), fechaTermino = d(-10), horario = "15:00-18:00",
                ubicacion = "Oficina ficticia", comuna = "Santiago", region = "Región Metropolitana",
                organizacionId = org3, publicoEsperado = "80 personas",
                publicoObjetivo = "Colaboradores", cantidadAsistentes = 80,
                condicionesParticipacion = "Invitación", requisitos = "Ninguno",
                costoReferencial = 0, espaciosDisponibles = 1, observaciones = "Dato ficticio"
            )
        )

        // ---------- Oportunidades ficticias ----------
        val o1 = dao.guardarOportunidad(
            OportunidadEntity(
                nombre = "Stand de convenios en feria de beneficios", tipo = TipoOportunidad.FERIA_EMPRESARIAL,
                descripcion = "Evaluar stand para ofrecer convenio dental", fuenteInformacion = "Sitio web ficticio",
                fechaDeteccion = d(-12), responsableId = comercial, prioridad = Prioridad.ALTA,
                potencialComercial = 4, afinidadPublico = 4, estado = EstadoOportunidad.CONTACTO_REALIZADO,
                eventoId = e1, organizacionId = org1, contactoId = c1,
                fechaUltimaActualizacion = ahora, observaciones = "Interés inicial", creadoPorId = comercial
            )
        )
        val o2 = dao.guardarOportunidad(
            OportunidadEntity(
                nombre = "Participación en jornada municipal", tipo = TipoOportunidad.ACTIVIDAD_MUNICIPAL,
                descripcion = "Presentar propuesta de participación", fuenteInformacion = "Redes sociales ficticias",
                fechaDeteccion = d(-15), responsableId = comercial, prioridad = Prioridad.MEDIA,
                potencialComercial = 3, afinidadPublico = 5, estado = EstadoOportunidad.PROPUESTA_ENVIADA,
                eventoId = e2, organizacionId = org2, contactoId = c2,
                fechaUltimaActualizacion = ahora, observaciones = "", creadoPorId = comercial
            )
        )
        val o3 = dao.guardarOportunidad(
            OportunidadEntity(
                nombre = "Actividad de bienestar para trabajadores", tipo = TipoOportunidad.ACTIVIDAD_TRABAJADORES,
                descripcion = "Detectada con poca anticipación", fuenteInformacion = "Referencia ficticia",
                fechaDeteccion = d(-2), responsableId = null, prioridad = Prioridad.ALTA,
                potencialComercial = 5, afinidadPublico = 3, estado = EstadoOportunidad.EVENTO_IDENTIFICADO,
                eventoId = e3, organizacionId = org3, contactoId = c3,
                fechaUltimaActualizacion = ahora, observaciones = "Sin responsable asignado", creadoPorId = comercial
            )
        )
        val o4 = dao.guardarOportunidad(
            OportunidadEntity(
                nombre = "Feria institucional en Concepción", tipo = TipoOportunidad.FERIA_INSTITUCIONAL,
                descripcion = "Participación confirmada en feria", fuenteInformacion = "Correo ficticio",
                fechaDeteccion = d(-20), responsableId = supervisor, prioridad = Prioridad.MEDIA,
                potencialComercial = 4, afinidadPublico = 4, estado = EstadoOportunidad.PARTICIPACION_CONFIRMADA,
                eventoId = e4, organizacionId = org4, contactoId = c4,
                fechaUltimaActualizacion = ahora, observaciones = "", creadoPorId = supervisor
            )
        )
        val o5 = dao.guardarOportunidad(
            OportunidadEntity(
                nombre = "Evento corporativo finalizado", tipo = TipoOportunidad.EVENTO_CORPORATIVO,
                descripcion = "Oportunidad cerrada por fecha", fuenteInformacion = "Contacto directo ficticio",
                fechaDeteccion = d(-30), responsableId = null, prioridad = Prioridad.BAJA,
                potencialComercial = 2, afinidadPublico = 2, estado = EstadoOportunidad.DESCARTADA,
                eventoId = e5, organizacionId = org3, contactoId = c3,
                fechaUltimaActualizacion = ahora, observaciones = "Descartada", creadoPorId = comercial
            )
        )
        val o6 = dao.guardarOportunidad(
            OportunidadEntity(
                nombre = "Activación comercial de convenios", tipo = TipoOportunidad.ACTIVACION_COMERCIAL,
                descripcion = "Activación en terreno", fuenteInformacion = "Comunicación comercial ficticia",
                fechaDeteccion = d(-8), responsableId = comercial, prioridad = Prioridad.ALTA,
                potencialComercial = 5, afinidadPublico = 4, estado = EstadoOportunidad.EN_ESPERA_RESPUESTA,
                eventoId = e2, organizacionId = org2, contactoId = c2,
                fechaUltimaActualizacion = ahora, observaciones = "", creadoPorId = comercial
            )
        )

        // ---------- Seguimientos ----------
        dao.insertarSeguimiento(
            SeguimientoEntity(
                oportunidadId = o1, tipoAccion = TipoAccion.INVESTIGACION_INICIAL, fecha = d(-14), hora = "10:00",
                resultado = "Evento confirmado en fuentes públicas ficticias", proximaAccion = "Primer contacto",
                fechaProximoSeguimiento = d(-12), observaciones = "", usuarioId = comercial
            )
        )
        // Último seguimiento con fecha vencida: genera alerta de seguimiento atrasado.
        dao.insertarSeguimiento(
            SeguimientoEntity(
                oportunidadId = o1, tipoAccion = TipoAccion.PRIMER_CONTACTO, fecha = d(-12), hora = "11:30",
                resultado = "Contacto inicial sin respuesta", proximaAccion = "Llamada de seguimiento",
                fechaProximoSeguimiento = d(-3), observaciones = "", usuarioId = comercial
            )
        )
        dao.insertarSeguimiento(
            SeguimientoEntity(
                oportunidadId = o2, tipoAccion = TipoAccion.ENVIO_PROPUESTA, fecha = d(-5), hora = "09:00",
                resultado = "Propuesta enviada", proximaAccion = "Confirmar recepción",
                fechaProximoSeguimiento = d(3), observaciones = "", usuarioId = comercial
            )
        )
        dao.insertarSeguimiento(
            SeguimientoEntity(
                oportunidadId = o4, tipoAccion = TipoAccion.RECEPCION_RESPUESTA, fecha = d(-3), hora = "15:00",
                resultado = "Participación confirmada", proximaAccion = "Coordinar stand",
                fechaProximoSeguimiento = d(5), observaciones = "", usuarioId = supervisor
            )
        )
        dao.insertarSeguimiento(
            SeguimientoEntity(
                oportunidadId = o6, tipoAccion = TipoAccion.LLAMADA, fecha = d(-1), hora = "16:00",
                resultado = "Solicitud de condiciones", proximaAccion = "Revisar respuesta",
                fechaProximoSeguimiento = d(2), observaciones = "", usuarioId = comercial
            )
        )
        dao.insertarSeguimiento(
            SeguimientoEntity(
                oportunidadId = o5, tipoAccion = TipoAccion.CIERRE, fecha = d(-30), hora = "17:00",
                resultado = "Oportunidad descartada por fecha", proximaAccion = "",
                fechaProximoSeguimiento = null, observaciones = "", usuarioId = comercial
            )
        )

        // ---------- Propuestas ----------
        dao.insertarPropuesta(
            PropuestaEntity(
                oportunidadId = o2, fechaEnvio = d(-5),
                descripcion = "Convenio con stand y charla informativa (ficticio)",
                condicionesParticipacion = "Valor referencial ficticio", costoReferencial = 90000,
                respondida = false
            )
        )
        dao.insertarPropuesta(
            PropuestaEntity(
                oportunidadId = o6, fechaEnvio = d(-1),
                descripcion = "Activación con atención en terreno (ficticia)",
                condicionesParticipacion = "Requiere confirmar fechas", costoReferencial = 45000,
                respondida = false
            )
        )
        dao.insertarPropuesta(
            PropuestaEntity(
                oportunidadId = o4, fechaEnvio = d(-20),
                descripcion = "Participación institucional básica (ficticia)",
                condicionesParticipacion = "Stand básico", costoReferencial = 120000,
                respondida = true, respuesta = "Confirmada"
            )
        )

        // ---------- Historial inicial ----------
        listOf(o1, o2, o3, o4, o5, o6).forEach { id ->
            dao.insertarHistorial(
                HistorialEntity(
                    oportunidadId = id, fecha = ahora, tipo = "CREACION",
                    descripcion = "Oportunidad creada (dato ficticio)", usuarioId = comercial
                )
            )
        }
    }
}
