package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.myapplication.navigation.Rutas

/** Campo de texto con mensaje de error opcional. */
@Composable
fun CampoTexto(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    error: String? = null,
    lineas: Int = 1,
    teclado: KeyboardType = KeyboardType.Text,
    contrasena: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        singleLine = lineas == 1,
        minLines = lineas,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        visualTransformation = if (contrasena) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = modifier
    )
}

/** Selector desplegable sencillo (reemplaza los combos de Android). */
@Composable
fun <T> SelectorDesplegable(
    etiqueta: String,
    opciones: List<T>,
    seleccionado: T?,
    etiquetaDe: (T) -> String,
    onSeleccion: (T) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    error: String? = null,
    textoVacio: String = "Seleccionar"
) {
    var expandido by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(etiqueta, style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(onClick = { expandido = true }, modifier = Modifier.fillMaxWidth()) {
                Text(seleccionado?.let(etiquetaDe) ?: textoVacio)
            }
            DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(etiquetaDe(opcion)) },
                        onClick = {
                            onSeleccion(opcion)
                            expandido = false
                        }
                    )
                }
            }
        }
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun Insignia(texto: String, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.secondaryContainer) {
    Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp), color = color) {
        Text(
            texto,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun TarjetaInfo(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            contenido()
        }
    }
}

@Composable
fun MenuInferior(rutaActual: String, onNavegar: (String) -> Unit) {
    val opciones = listOf(
        Triple(Rutas.INICIO, "Inicio", Icons.Default.Home),
        Triple(Rutas.OPORTUNIDADES, "Oportunidades", Icons.Default.List),
        Triple(Rutas.ALERTAS, "Alertas", Icons.Default.Notifications),
        Triple(Rutas.PERFIL, "Perfil", Icons.Default.Person)
    )
    NavigationBar {
        opciones.forEach { (ruta, etiqueta, icono) ->
            NavigationBarItem(
                selected = rutaActual == ruta,
                onClick = { onNavegar(ruta) },
                icon = { Icon(icono, contentDescription = etiqueta) },
                label = { Text(etiqueta) }
            )
        }
    }
}

/** Pantalla principal con barra inferior y, opcionalmente, un botón flotante. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(
    titulo: String,
    rutaActual: String,
    onNavegar: (String) -> Unit,
    accion: (@Composable () -> Unit)? = null,
    contenido: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(titulo) }) },
        bottomBar = { MenuInferior(rutaActual, onNavegar) },
        floatingActionButton = { accion?.invoke() }
    ) { padding -> contenido(padding) }
}

/** Pantalla de detalle o formulario con botón de volver. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSecundaria(
    titulo: String,
    onAtras: () -> Unit,
    contenido: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = { TextButton(onClick = onAtras) { Text("Volver") } }
            )
        }
    ) { padding -> contenido(padding) }
}

/** Formato de pesos chilenos para montos ficticios: $ 150.000. */
fun pesos(valor: Long): String {
    val digitos = valor.toString().reversed().chunked(3).joinToString(".").reversed()
    return "\$ $digitos"
}
