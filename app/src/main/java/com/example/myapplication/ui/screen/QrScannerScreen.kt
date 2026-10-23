package com.example.myapplication.ui.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.myapplication.ui.components.QrScanner
import com.example.myapplication.viewmodel.EstadoEscaner
import com.example.myapplication.viewmodel.QrViewModel

/**
 * Escáner de QR. La vista solo muestra el estado del ViewModel y reenvía eventos
 * (permiso concedido, código leído). Al detectar un código, lo entrega a la pantalla anterior.
 */
@Composable
fun QrScannerScreen(
    vm: QrViewModel,
    onQrRead: (String) -> Unit
) {
    val context = LocalContext.current
    val escaner by vm.escaner.collectAsState()
    val permiso by vm.permiso.collectAsState()

    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        vm.onResultadoPermiso(concedido)
        if (concedido) {
            Toast.makeText(context, "Permiso de cámara concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Se necesita permiso de cámara para escanear QR", Toast.LENGTH_LONG).show()
        }
    }

    // Comprueba el permiso al entrar y al volver de Ajustes del sistema
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                vm.onPermisoVerificado(tienePermisoCamara(context))
            }
        }
        lifecycleOwner.lifecycle.addObserver(observador)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observador) }
    }

    LaunchedEffect(Unit) {
        if (!tienePermisoCamara(context)) {
            lanzadorPermiso.launch(Manifest.permission.CAMERA)
        }
    }

    // Lectura completada: se devuelve el contenido a la pantalla anterior
    LaunchedEffect(escaner) {
        val detectado = escaner as? EstadoEscaner.Detectado ?: return@LaunchedEffect
        Toast.makeText(context, "¡Código QR detectado!", Toast.LENGTH_SHORT).show()
        onQrRead(detectado.contenido)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!permiso.concedido) {
            VistaSinPermiso(
                rechazado = permiso.rechazado,
                onSolicitar = { lanzadorPermiso.launch(Manifest.permission.CAMERA) },
                onAbrirAjustes = { abrirAjustesApp(context) }
            )
        } else {
            when (val estado = escaner) {
                EstadoEscaner.Escaneando -> VistaEscaneo(onQrDetectado = vm::onQrDetectado)
                is EstadoEscaner.Detectado -> VistaDetectado(
                    contenido = estado.contenido,
                    onEscanearOtro = vm::escanearOtro
                )
            }
        }
    }
}

@Composable
private fun VistaSinPermiso(
    rechazado: Boolean,
    onSolicitar: () -> Unit,
    onAbrirAjustes: () -> Unit
) {
    Text(
        text = "Permiso de cámara requerido",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(Modifier.height(16.dp))

    Text(
        text = if (rechazado)
            "Rechazaste el permiso de cámara. Actívalo desde los ajustes de la app."
        else
            "Para escanear códigos QR, necesitamos acceso a la cámara.",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(24.dp))

    if (rechazado) {
        // Android no vuelve a mostrar el diálogo tras un rechazo: hay que ir a Ajustes
        Button(onClick = onAbrirAjustes) { Text("Abrir ajustes de la app") }
    } else {
        Button(onClick = onSolicitar) { Text("Conceder permiso de cámara") }
    }
}

@Composable
private fun VistaEscaneo(onQrDetectado: (String) -> Unit) {
    Text(
        text = "Escanea un código QR",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        QrScanner(
            onQrCodeScanned = onQrDetectado,
            modifier = Modifier.fillMaxSize()
        )

        // Marco visual para centrar el código QR
        Surface(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.Center),
            color = Color.Transparent,
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        ) {}
    }

    Spacer(Modifier.height(16.dp))

    Text(
        text = "Enfoca el código QR en el marco central.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = "La cámara debería activarse automáticamente.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun VistaDetectado(contenido: String, onEscanearOtro: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "QR detectado",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = contenido,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = onEscanearOtro, modifier = Modifier.fillMaxWidth()) {
            Text("Escanear otro código QR")
        }
    }
}

private fun tienePermisoCamara(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

private fun abrirAjustesApp(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    context.startActivity(intent)
}
