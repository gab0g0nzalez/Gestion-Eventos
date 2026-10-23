package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Repositorio
import com.example.myapplication.domain.Alerta
import com.example.myapplication.domain.Indicadores
import com.example.myapplication.domain.ResumenIndicadores
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Datos del panel de inicio: indicadores y alertas. */
class HomeViewModel(repo: Repositorio) : ViewModel() {

    val indicadores: StateFlow<ResumenIndicadores> = repo.observarOportunidadesEntidades()
        .map { Indicadores.calcular(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResumenIndicadores())

    val alertas: StateFlow<List<Alerta>> = repo.observarAlertas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
