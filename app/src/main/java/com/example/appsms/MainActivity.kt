package com.example.appsms

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.appsms.ui.theme.AppSMSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppSMSTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SMSAutoResponderScreen()
                }
            }
        }
    }
}

@Composable
fun SMSAutoResponderScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("AppSMSPrefs", Context.MODE_PRIVATE) }

    var targetNumber by remember { mutableStateOf(prefs.getString("targetNumber", "") ?: "") }
    var autoMessage by remember { mutableStateOf(prefs.getString("autoMessage", "") ?: "") }

    // Lista de permisos completa (Incluye Notificaciones para Android 13+)
    val permissions = mutableListOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.SEND_SMS
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (!results.values.all { it }) {
            Toast.makeText(context, "Acepta todos los permisos para que la app funcione", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(permissions)
    }

    Column(modifier = Modifier.padding(24.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Configurar Auto-Respuesta", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = targetNumber,
            onValueChange = { targetNumber = it },
            label = { Text("Número exacto (ej: 4451826054)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = autoMessage,
            onValueChange = { autoMessage = it },
            label = { Text("Mensaje a enviar") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Button(
            onClick = {
                prefs.edit().putString("targetNumber", targetNumber).putString("autoMessage", autoMessage).apply()
                Toast.makeText(context, "Configuración guardada", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar y Activar")
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Text(
                text = "Escribe el número tal cual aparece en la llamada, o solo los últimos 10 dígitos.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}