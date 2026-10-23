package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.CampoTexto
import com.example.myapplication.ui.components.PantallaSecundaria
import com.example.myapplication.viewmodel.AuthUiState

@Composable
fun LoginScreen(
    ui: AuthUiState,
    onEditar: ((AuthUiState) -> AuthUiState) -> Unit,
    onIngresar: () -> Unit,
    onIrRegistro: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Gestión de oportunidades", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Ingrese con una cuenta ficticia.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        CampoTexto(
            etiqueta = "Correo",
            valor = ui.loginCorreo,
            onCambio = { v -> onEditar { it.copy(loginCorreo = v, loginError = null) } },
            teclado = KeyboardType.Email
        )
        Spacer(Modifier.height(8.dp))
        CampoTexto(
            etiqueta = "Clave",
            valor = ui.loginClave,
            onCambio = { v -> onEditar { it.copy(loginClave = v, loginError = null) } },
            contrasena = true,
            teclado = KeyboardType.Password
        )
        ui.loginError?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onIngresar, modifier = Modifier.fillMaxWidth()) {
            Text("Ingresar")
        }
        TextButton(onClick = onIrRegistro, modifier = Modifier.fillMaxWidth()) {
            Text("Crear cuenta ficticia")
        }

    }
}

@Composable
fun RegistroScreen(
    ui: AuthUiState,
    onEditar: ((AuthUiState) -> AuthUiState) -> Unit,
    onRegistrar: () -> Unit,
    onAtras: () -> Unit
) {
    PantallaSecundaria(titulo = "Crear cuenta ficticia", onAtras = onAtras) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            
            CampoTexto(
                "Nombre ficticio", ui.regNombre,
                { v -> onEditar { it.copy(regNombre = v) } },
                error = ui.regErrores["nombre"]
            )
            CampoTexto(
                "Correo ficticio", ui.regCorreo,
                { v -> onEditar { it.copy(regCorreo = v) } },
                error = ui.regErrores["correo"], teclado = KeyboardType.Email
            )
            CampoTexto(
                "Clave (mínimo 6 caracteres)", ui.regClave,
                { v -> onEditar { it.copy(regClave = v) } },
                error = ui.regErrores["clave"], contrasena = true, teclado = KeyboardType.Password
            )
            CampoTexto(
                "Área", ui.regArea,
                { v -> onEditar { it.copy(regArea = v) } },
                error = ui.regErrores["area"]
            )
            CampoTexto(
                "Zona o sucursal (ficticia)", ui.regZona,
                { v -> onEditar { it.copy(regZona = v) } },
                error = ui.regErrores["zona"]
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = ui.regTerminos,
                    onCheckedChange = { v -> onEditar { it.copy(regTerminos = v) } }
                )
                Spacer(Modifier.width(8.dp))
                Text("Acepto los términos y condiciones de uso académico")
            }
            ui.regErrores["terminos"]?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Button(onClick = onRegistrar, modifier = Modifier.fillMaxWidth()) {
                Text("Crear cuenta")
            }
        }
    }
}
