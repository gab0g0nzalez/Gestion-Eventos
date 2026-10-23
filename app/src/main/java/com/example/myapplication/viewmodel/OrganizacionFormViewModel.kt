package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.OrganizacionEntity
import com.example.myapplication.data.Repositorio
import com.example.myapplication.domain.DatosOrganizacion
import com.example.myapplication.domain.Validar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Formulario de una organización nueva. */
class OrganizacionFormViewModel(private val repo: Repositorio) : ViewModel() {

    private val _formulario = MutableStateFlow(Formulario(DatosOrganizacion()))
    val formulario: StateFlow<Formulario<DatosOrganizacion>> = _formulario.asStateFlow()

    fun editar(cambio: (DatosOrganizacion) -> DatosOrganizacion) = _formulario.actualizarDatos(cambio)

    fun guardar(onOk: () -> Unit) {
        val datos = _formulario.value.datos
        val errores = Validar.organizacion(datos)
        if (errores.isNotEmpty()) {
            _formulario.conErrores(errores)
            return
        }
        viewModelScope.launch {
            repo.guardarOrganizacion(
                OrganizacionEntity(
                    nombre = datos.nombre.trim(),
                    tipo = datos.tipo,
                    rubro = datos.rubro.trim(),
                    comuna = datos.comuna.trim(),
                    region = datos.region.trim(),
                    sitioWeb = datos.sitioWeb.trim(),
                    publicoAsociado = datos.publicoAsociado.trim(),
                    observaciones = datos.observaciones.trim()
                )
            )
            onOk()
        }
    }
}
