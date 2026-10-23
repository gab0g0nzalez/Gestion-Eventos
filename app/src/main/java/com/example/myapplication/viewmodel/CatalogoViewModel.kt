package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ContactoEntity
import com.example.myapplication.data.EventoEntity
import com.example.myapplication.data.OrganizacionEntity
import com.example.myapplication.data.Repositorio
import com.example.myapplication.data.UsuarioEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Listas de catálogos compartidas por varias pantallas (eventos, organizaciones, contactos, usuarios). */
class CatalogoViewModel(repo: Repositorio) : ViewModel() {

    val eventos: StateFlow<List<EventoEntity>> =
        repo.observarEventos().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val organizaciones: StateFlow<List<OrganizacionEntity>> =
        repo.observarOrganizaciones().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val contactos: StateFlow<List<ContactoEntity>> =
        repo.observarContactos().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val usuarios: StateFlow<List<UsuarioEntity>> =
        repo.observarUsuarios().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}
