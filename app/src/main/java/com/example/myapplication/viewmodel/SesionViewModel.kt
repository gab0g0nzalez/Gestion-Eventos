package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Repositorio
import com.example.myapplication.data.UsuarioEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado de la sesión y arranque de la app (siembra de datos ficticios si la base está vacía). */
class SesionViewModel(private val repo: Repositorio) : ViewModel() {

    /** Define la pantalla inicial: con sesión guardada se entra directo al inicio. */
    val sesionIniciada: Boolean = repo.sesionIniciada()

    val usuario: StateFlow<UsuarioEntity?> =
        repo.usuarioActual().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch { repo.sembrarSiVacio() }
    }

    fun cerrarSesion(onSalir: () -> Unit) {
        repo.cerrarSesion()
        onSalir()
    }
}
