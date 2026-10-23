package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Estado del escáner: buscando un código o ya detectado. */
sealed interface EstadoEscaner {
    data object Escaneando : EstadoEscaner
    data class Detectado(val contenido: String) : EstadoEscaner
}

/** Permiso de cámara: concedido, y si el usuario lo rechazó (Android ya no muestra el diálogo). */
data class EstadoPermisoCamara(val concedido: Boolean = false, val rechazado: Boolean = false)

/**
 * ViewModel del escáner. Se crea por entrada de navegación, así que cada visita
 * a la pantalla empieza con un escaneo limpio.
 */
class QrViewModel : ViewModel() {

    private val _escaner = MutableStateFlow<EstadoEscaner>(EstadoEscaner.Escaneando)
    val escaner: StateFlow<EstadoEscaner> = _escaner.asStateFlow()

    private val _permiso = MutableStateFlow(EstadoPermisoCamara())
    val permiso: StateFlow<EstadoPermisoCamara> = _permiso.asStateFlow()

    /** Llamado por la cámara. Solo acepta la primera lectura no vacía. */
    fun onQrDetectado(contenido: String) {
        if (contenido.isBlank() || _escaner.value is EstadoEscaner.Detectado) return
        _escaner.value = EstadoEscaner.Detectado(contenido)
    }

    fun escanearOtro() {
        _escaner.value = EstadoEscaner.Escaneando
    }

    /** Resultado del diálogo de permisos. */
    fun onResultadoPermiso(concedido: Boolean) {
        _permiso.value = EstadoPermisoCamara(concedido = concedido, rechazado = !concedido)
    }

    /** Comprobación al volver a la pantalla (p. ej. desde Ajustes). No cambia el indicador de rechazo. */
    fun onPermisoVerificado(concedido: Boolean) {
        _permiso.update { it.copy(concedido = concedido) }
    }
}
