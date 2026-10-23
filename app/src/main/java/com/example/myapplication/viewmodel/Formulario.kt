package com.example.myapplication.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** Datos de un formulario junto con sus errores de validación (mapa vacío = sin errores). */
data class Formulario<T>(val datos: T, val errores: Map<String, String> = emptyMap())

/** Helpers internos para los ViewModels que mantienen formularios en un MutableStateFlow. */
internal fun <T> MutableStateFlow<Formulario<T>>.actualizarDatos(cambio: (T) -> T) {
    update { it.copy(datos = cambio(it.datos)) }
}

internal fun <T> MutableStateFlow<Formulario<T>>.conErrores(errores: Map<String, String>) {
    update { it.copy(errores = errores) }
}
