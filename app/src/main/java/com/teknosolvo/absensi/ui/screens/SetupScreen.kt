package com.teknosolvo.absensi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Pendaftaran karyawan pertama kali (simulasi login). */
@Composable
fun SetupScreen(onDone: () -> Unit) {
    val app = rememberApp()
    var name by rememberSaveable { mutableStateOf("") }
    var employeeId by rememberSaveable { mutableStateOf("") }

    Scaffold { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            Text("Selamat datang", style = MaterialTheme.typography.headlineMedium)
            Text("Isi data karyawan untuk mulai absen.", style = MaterialTheme.typography.bodyLarge)
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Nama lengkap") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = employeeId, onValueChange = { employeeId = it },
                label = { Text("NIP / ID karyawan") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    app.settings.update { it.copy(employeeName = name.trim(), employeeId = employeeId.trim()) }
                    onDone()
                },
                enabled = name.isNotBlank() && employeeId.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Mulai") }
        }
    }
}
