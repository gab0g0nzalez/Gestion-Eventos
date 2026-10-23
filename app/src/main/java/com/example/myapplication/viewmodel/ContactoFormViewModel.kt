package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ContactoEntity
import com.example.myapplication.data.OrganizacionEntity
import com.example.myapplication.data.Repositorio
import com.example.myapplication.domain.DatosContacto
import com.example.myapplication.domain.Validar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Formulario de un contacto nuevo asociado a una organización. */
class ContactoFormViewModel(private val repo: Repositorio) : ViewModel() {

    val organizaciones: StateFlow<List<OrganizacionEntity>> =
        repo.observarOrganizaciones().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _formulario = MutableStateFlow(Formulario(DatosContacto()))
    val formulario: StateFlow<Formulario<DatosContacto>> = _formulario.asStateFlow()

    fun editar(cambio: (DatosContacto) -> DatosContacto) = _formulario.actualizarDatos(cambio)

    fun guardar(onOk: () -> Unit) {
        val datos = _formulario.value.datos
        val errores = Validar.contacto(datos)
        val organizacionId = datos.organizacionId
        if (errores.isNotEmpty() || organizacionId == null) {
            _formulario.conErrores(errores)
            return
        }
        viewModelScope.launch {
            repo.guardarContacto(
                ContactoEntity(
                    nombre = datos.nombre.trim(),
                    organizacionId = organizacionId,
                    cargo = datos.cargo.trim(),
                    area = datos.area,
                    correo = datos.correo.trim(),
                    telefono = datos.telefono.trim(),
                    medioPreferido = datos.medio,
                    fechaUltimoContacto = null,
                    observaciones = datos.observaciones.trim()
                )
            )
            onOk()
        }
    }
}
