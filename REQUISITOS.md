# Cumplimiento del caso DSY1105 – Gestión móvil de oportunidades y activaciones corporativas

Aplicación Android (Kotlin, Jetpack Compose, Material 3, arquitectura MVVM, Room). Todos los datos son ficticios.

## Funcionalidades mínimas del MVP (sección 6)

| Módulo | Requisito | Dónde se implementa |
|---|---|---|
| Autenticación | Inicio de sesión con cuenta ficticia | `ui/screen/AuthScreens.kt`, `viewmodel/AuthViewModel.kt`, `Repositorio.iniciarSesion` |
| | Identificación del usuario, control de sesión, perfil | `Repositorio` (sesión en SharedPreferences, solo id), `ui/screen/InicioScreens.kt` → `PerfilScreen` |
| Oportunidades | Crear, modificar, consultar | `OportunidadFormScreen.kt`, `OportunidadesScreen.kt`, `OportunidadDetalleScreen.kt` |
| | Asignar responsable, definir prioridad, actualizar estado | Sección "Gestión" del detalle (`OportunidadViewModel.asignarResponsable / cambiarPrioridad / cambiarEstado`) |
| | Registrar observaciones | Campo `observaciones` en la entidad y en el formulario |
| Eventos | Registrar evento, tipo, fecha y horario, comuna y región, organización, público esperado, condiciones | `EventoFormScreen`, `EventosScreen`, `Validar.evento` |
| Organizaciones y contactos | Registrar organizaciones y contactos ficticios, asociar, área/cargo, historial de contacto | `OrganizacionFormScreen`, `ContactoFormScreen`, campo `fechaUltimoContacto` (se actualiza al registrar seguimientos) |
| Seguimiento | Registrar acciones, fecha de contacto, resultado, próxima acción | Diálogo "Seguimiento" → `Repositorio.registrarSeguimiento` |
| Alertas | Eventos próximos, seguimientos pendientes/atrasados, propuestas sin respuesta, oportunidades sin contacto | `domain/Reglas.kt` → `Alertas.calcular`, `AlertasScreen` |
| Búsqueda y filtros | Buscar por nombre; filtrar por fecha, estado, tipo, comuna/región, prioridad y responsable | `OportunidadesScreen` + `Filtros.aplicar` |
| Historial | Acciones, cambios de estado, propuestas, respuestas y resultado final | Tabla `historial` (se escribe en cada cambio) y sección "Historial" del detalle |

## Información del modelo de datos (sección 3.2)

- **Usuarios:** `UsuarioEntity` (id, nombre, correo, clave en hash SHA-256, perfil, área, zona, estado, fechas).
- **Oportunidades:** `OportunidadEntity` (todos los campos del anexo, incluidos potencial y afinidad de 1 a 5).
- **Eventos, organizaciones, contactos, seguimientos, propuestas, historial, resultados:** entidades independientes con claves foráneas lógicas.
- **Estados:** los 7 principales y los 6 complementarios del anexo (`EstadoOportunidad`).
- **Tipos, áreas, tipos de acción, tipos de organización:** enums en `data/Enums.kt`.
- No se registran domicilios, RUT, datos de salud, datos biométricos ni datos financieros individuales.

## Criterios de éxito (sección 4) y restricciones (sección 5)

- Datos precargados ficticios (`data/SeedData.kt`, sección 7 del caso): 4 organizaciones, 4 contactos, 5 eventos, 6 oportunidades, seguimientos, propuestas y un resultado. Las fechas son relativas a hoy para que las alertas se vean.
- Funciona sin conexión: la persistencia es local (Room/SQLite). No hay integraciones externas ni credenciales institucionales.
- La ubicación se modela con comuna y región (sin GPS ni seguimiento de usuarios).
- Notificaciones solo internas (pantalla de alertas), tal como permite el caso.

## Tecnologías del programa (sección 5.1 / 3)

| Tecnología | Estado |
|---|---|
| Kotlin, Android Studio, Jetpack Compose, Material Design 3 | Usado |
| Arquitectura MVVM | ViewModels por área + repositorio único (`data/Repositorio.kt`) |
| Room o SQLite | Room 2.8.4 con migración controlada |
| Recursos nativos del dispositivo | SharedPreferences para la sesión |
| Pruebas unitarias | `app/src/test/.../ReglasTest.kt` (16 pruebas sobre validaciones, alertas, indicadores, filtros, fechas y hash) |
| APK firmado en modo release | Configuración de firma en `app/build.gradle.kts` que lee `keystore.properties` (no versionado) |
| Git y GitHub, Trello | Proceso del equipo; no es código |
| API REST, Retrofit, Spring Boot | **No aplica en el MVP**: el caso permite una base de datos independiente y no exige integración |

## Funcionalidades complementarias (sección 6) no implementadas

Mapa con ubicaciones ficticias, calendario de eventos, exportación de información, perfil supervisor con vistas dedicadas, perfil de marketing con vistas dedicadas, integración con fuentes externas. Son opcionales según el caso.

## Pendientes y notas para el equipo

1. **Firma de release:** crear un keystore propio y `keystore.properties` en la raíz del proyecto (no se incluye).
2. **Versión de KSP:** se corrigió a `2.2.10-2.0.2` para que coincida con Kotlin 2.2.10. Verificar en Android Studio que sincroniza.
3. **Cambio de esquema:** la base pasó a versión 2 con `fallbackToDestructiveMigration`. Reinstalar la app si hay datos antiguos.
4. **Contraseñas:** se guardan como hash SHA-256 sin sal. Aceptable para cuentas ficticias en un MVP académico; no es apto para datos reales.
