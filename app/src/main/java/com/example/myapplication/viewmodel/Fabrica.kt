package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.Repositorio

/**
 * Crea ViewModels con el repositorio inyectado, sin librerías adicionales (Hilt).
 * Uso: viewModel(factory = fabrica.vm { AuthViewModel(it) }) o con argumentos:
 *      viewModel(viewModelStoreOwner = entrada, factory = fabrica.vm { DetalleViewModel(it, id) })
 */
class Fabrica(private val repo: Repositorio) {

    fun <VM : ViewModel> vm(crear: (Repositorio) -> VM): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = crear(repo) as T
        }
}
