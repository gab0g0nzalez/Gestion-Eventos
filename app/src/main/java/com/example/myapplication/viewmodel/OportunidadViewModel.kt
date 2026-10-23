package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.OportunidadResumen
import com.example.myapplication.data.Repositorio
import com.example.myapplication.domain.FiltroOportunidades
import com.example.myapplication.domain.Filtros
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/** Lista de oportunidades con búsqueda y filtros. */
class OportunidadViewModel(repo: Repositorio) : ViewModel() {

    private val filtro = MutableStateFlow(FiltroOportunidades())
    val filtroActual: StateFlow<FiltroOportunidades> = filtro.asStateFlow()

    val lista: StateFlow<List<OportunidadResumen>> =
        combine(repo.observarResumenOportunidades(), filtro) { datos, f -> Filtros.aplicar(datos, f) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun cambiarFiltro(cambio: (FiltroOportunidades) -> FiltroOportunidades) {
        filtro.update(cambio)
    }
}
