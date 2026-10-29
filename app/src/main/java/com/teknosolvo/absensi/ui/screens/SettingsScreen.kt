package com.teknosolvo.absensi.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.teknosolvo.absensi.util.LocationHelper
import com.teknosolvo.absensi.util.parseTimeOrNull
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = rememberApp()
    val scope = rememberCoroutineScope()
    val initial = remember { app.settings.state.value }

    var name by rememberSaveable { mutableStateOf(initial.employeeName) }
    var employeeId by rememberSaveable { mutableStateOf(initial.employeeId) }
    var lat by rememberSaveable { mutableStateOf(initial.officeLat?.toString() ?: "") }
    var lng by rememberSaveable { mutableStateOf(initial.officeLng?.toString() ?: "") }
    var radius by rememberSaveable { mutableStateOf(initial.radiusM.toString()) }
    var workStart by rememberSaveable { mutableStateOf(initial.workStart) }
    var workEnd by rememberSaveable { mutableStateOf(initial.workEnd) }
    var enforceRadius by rememberSaveable { mutableStateOf(initial.enforceRadius) }
    var locating by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }

    fun useCurrentLocation() {
        locating = true
        scope.launch {
            val loc = runCatching { LocationHelper.current(context) }.getOrNull()
            locating = false
            if (loc == null) {
                Toast.makeText(context, "Lokasi tidak ditemukan. Pastikan GPS aktif.", Toast.LENGTH_LONG).show()
            } else {
                lat = loc.latitude.toString()
                lng = loc.longitude.toString()
            }
        }
    }
    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { ok -> if (ok) useCurrentLocation() }

    val latValue = lat.trim().toDoubleOrNull()
    val lngValue = lng.trim().toDoubleOrNull()
    val officeBlank = lat.isBlank() && lng.isBlank()
    val errors = listOfNotNull(
        "Nama dan NIP wajib diisi".takeIf { name.isBlank() || employeeId.isBlank() },
        "Koordinat kantor tidak valid".takeIf {
            !officeBlank && (latValue == null || lngValue == null || latValue !in -90.0..90.0 || lngValue !in -180.0..180.0)
        },
        "Radius harus angka > 0".takeIf { (radius.toIntOrNull() ?: 0) <= 0 },
        "Format jam harus HH:mm".takeIf { parseTimeOrNull(workStart) == null || parseTimeOrNull(workEnd) == null },
    )

    Scaffold(topBar = { BackTopBar("Pengaturan", onBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Karyawan", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Nama") }, singleLine = true)
            OutlinedTextField(employeeId, { employeeId = it }, Modifier.fillMaxWidth(), label = { Text("NIP") }, singleLine = true)

            HorizontalDivider()
            Text("Lokasi kantor", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                lat, { lat = it }, Modifier.fillMaxWidth(), label = { Text("Latitude") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            OutlinedTextField(
                lng, { lng = it }, Modifier.fillMaxWidth(), label = { Text("Longitude") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            OutlinedButton(
                onClick = {
                    val granted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    if (granted) useCurrentLocation() else locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                },
                enabled = !locating,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (locating) "Mencari lokasi…" else "Pakai lokasi saat ini") }
            OutlinedTextField(
                radius, { radius = it.filter(Char::isDigit) }, Modifier.fillMaxWidth(),
                label = { Text("Radius (meter)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tolak absen di luar radius", Modifier.weight(1f))
                Switch(checked = enforceRadius, onCheckedChange = { enforceRadius = it })
            }

            HorizontalDivider()
            Text("Jam kerja", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(workStart, { workStart = it }, Modifier.weight(1f), label = { Text("Masuk (HH:mm)") }, singleLine = true)
                OutlinedTextField(workEnd, { workEnd = it }, Modifier.weight(1f), label = { Text("Pulang (HH:mm)") }, singleLine = true)
            }

            errors.forEach { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                onClick = {
                    app.settings.update {
                        it.copy(
                            employeeName = name.trim(),
                            employeeId = employeeId.trim(),
                            officeLat = if (officeBlank) null else latValue,
                            officeLng = if (officeBlank) null else lngValue,
                            radiusM = radius.toInt(),
                            workStart = workStart.trim(),
                            workEnd = workEnd.trim(),
                            enforceRadius = enforceRadius,
                        )
                    }
                    Toast.makeText(context, "Pengaturan disimpan", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                enabled = errors.isEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Simpan") }

            HorizontalDivider()
            OutlinedButton(onClick = { confirmReset = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Hapus semua data absen", color = MaterialTheme.colorScheme.error)
            }
        }

        if (confirmReset) {
            AlertDialog(
                onDismissRequest = { confirmReset = false },
                title = { Text("Hapus semua data?") },
                text = { Text("Semua riwayat absen dan fotonya akan dihapus permanen.") },
                confirmButton = {
                    TextButton(onClick = {
                        confirmReset = false
                        scope.launch {
                            val dao = app.db.attendanceDao()
                            dao.allPhotoPaths().forEach { File(it).delete() }
                            dao.deleteAll()
                            Toast.makeText(context, "Data dihapus", Toast.LENGTH_SHORT).show()
                        }
                    }) { Text("Hapus") }
                },
                dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Batal") } },
            )
        }
    }
}
