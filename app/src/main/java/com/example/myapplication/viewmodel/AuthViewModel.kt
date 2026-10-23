package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Repositorio
import com.example.myapplication.domain.Validar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val loginCorreo: String = "",
    val loginClave: String = "",
    val loginError: String? = null,
    val regNombre: String = "",
    val regCorreo: String = "",
    val regClave: String = "",
    val regArea: String = "",
    val regZona: String = "",
    val regTerminos: Boolean = false,
    val regErrores: Map<String, String> = emptyMap()
)

/** Formularios de ingreso y registro. */
class AuthViewModel(private val repo: Repositorio) : ViewModel() {

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    fun editar(cambio: (AuthUiState) -> AuthUiState) {
        _ui.update(cambio)
    }

    fun iniciarSesion(onOk: () -> Unit) {
        val actual = _ui.value
        if (actual.loginCorreo.isBlank() || actual.loginClave.isBlank()) {
            _ui.update { it.copy(loginError = "Ingrese correo y clave") }
            return
        }
        viewModelScope.launch {
            if (repo.iniciarSesion(actual.loginCorreo, actual.loginClave)) {
                _ui.update { AuthUiState() }
                onOk()
            } else {
                _ui.update { it.copy(loginError = "Correo o clave incorrectos") }
            }
        }
    }

    fun registrar(onOk: () -> Unit) {
        val s = _ui.value
        val errores = Validar.registro(
            s.regNombre, s.regCorreo, s.regClave, s.regArea, s.regZona, s.regTerminos
        )
        if (errores.isNotEmpty()) {
            _ui.update { it.copy(regErrores = errores) }
            return
        }
        viewModelScope.launch {
            val creado = repo.registrarUsuario(s.regNombre, s.regCorreo, s.regClave, s.regArea, s.regZona)
            if (creado) {
                _ui.update { AuthUiState() }
                onOk()
            } else {
                _ui.update { it.copy(regErrores = mapOf("correo" to "Ya existe una cuenta con ese correo")) }
            }
        }
    }

    fun limpiarErrores() {
        _ui.update { it.copy(loginError = null, regErrores = emptyMap()) }
    }
}
