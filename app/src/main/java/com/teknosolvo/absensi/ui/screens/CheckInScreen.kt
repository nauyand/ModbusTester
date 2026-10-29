package com.teknosolvo.absensi.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.teknosolvo.absensi.data.Attendance
import com.teknosolvo.absensi.ui.theme.Success
import com.teknosolvo.absensi.util.DATE_KEY
import com.teknosolvo.absensi.util.DATE_TIME
import com.teknosolvo.absensi.util.LOCALE_ID
import com.teknosolvo.absensi.util.LocationHelper
import com.teknosolvo.absensi.util.PhotoStamper
import com.teknosolvo.absensi.util.attendanceStatus
import com.teknosolvo.absensi.util.formatDistance
import com.teknosolvo.absensi.util.typeLabel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId

private sealed interface LocState {
    data object Loading : LocState
    data class Error(val message: String) : LocState
    data class Ready(val location: Location) : LocState
}

private val PERMISSIONS = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

@Composable
fun CheckInScreen(type: String, onDone: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val app = rememberApp()
    val settings by app.settings.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    fun hasPermissions() = PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
    var granted by remember { mutableStateOf(hasPermissions()) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted = hasPermissions() }
    LaunchedEffect(Unit) { if (!granted) permissionLauncher.launch(PERMISSIONS) }

    var locState by remember { mutableStateOf<LocState>(LocState.Loading) }
    var locRefresh by remember { mutableIntStateOf(0) }
    LaunchedEffect(granted, locRefresh) {
        if (!granted) return@LaunchedEffect
        locState = LocState.Loading
        locState = try {
            LocationHelper.current(context)?.let { LocState.Ready(it) }
                ?: LocState.Error("Lokasi tidak ditemukan. Pastikan GPS aktif lalu coba lagi.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LocState.Error("Gagal membaca lokasi: ${e.message}")
        }
    }

    var photo by remember { mutableStateOf<File?>(null) }
    var note by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    // Hapus foto yang tidak jadi dipakai.
    DisposableEffect(Unit) { onDispose { if (!saved) photo?.delete() } }

    val location = (locState as? LocState.Ready)?.location
    val distance = if (location != null && settings.officeLat != null && settings.officeLng != null) {
        LocationHelper.distance(location.latitude, location.longitude, settings.officeLat!!, settings.officeLng!!)
    } else {
        null
    }
    val inside = distance != null && distance <= settings.radiusM
    val mock = location != null && LocationHelper.isMock(location)
    val blockReason = when {
        !granted -> "Izin kamera dan lokasi diperlukan."
        location == null -> "Menunggu lokasi GPS…"
        mock -> "Terdeteksi lokasi palsu (fake GPS). Matikan aplikasi mock location."
        settings.enforceRadius && settings.hasOffice && !inside ->
            "Anda berada di luar radius kantor (${settings.radiusM} m)."
        photo == null -> "Ambil foto selfie terlebih dahulu."
        else -> null
    }

    fun submit(loc: Location, file: File) {
        saving = true
        scope.launch {
            try {
                val now = LocalDateTime.now()
                val address = LocationHelper.address(context, loc.latitude, loc.longitude)
                val coords = String.format(LOCALE_ID, "%.6f, %.6f (±%.0f m)", loc.latitude, loc.longitude, loc.accuracy)
                withContext(Dispatchers.Default) {
                    PhotoStamper.stamp(
                        file,
                        listOfNotNull(
                            "${typeLabel(type).uppercase()} • ${settings.employeeName} (${settings.employeeId})",
                            now.format(DATE_TIME),
                            coords,
                            address,
                        ),
                    )
                }
                app.db.attendanceDao().insert(
                    Attendance(
                        type = type,
                        timestamp = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        date = now.toLocalDate().format(DATE_KEY),
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        accuracyM = loc.accuracy,
                        distanceM = distance,
                        insideArea = inside,
                        address = address,
                        photoPath = file.absolutePath,
                        status = attendanceStatus(type, now.toLocalTime(), settings),
                        note = note.trim().ifEmpty { null },
                    )
                )
                saved = true
                Toast.makeText(context, "Absen ${typeLabel(type).lowercase()} tersimpan", Toast.LENGTH_SHORT).show()
                onDone()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                saving = false
                Toast.makeText(context, "Gagal menyimpan: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(topBar = { BackTopBar("Absen ${typeLabel(type)}", onBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!granted) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Aplikasi butuh izin kamera dan lokasi untuk absen.")
                        TextButton(onClick = { permissionLauncher.launch(PERMISSIONS) }) { Text("Beri izin") }
                    }
                }
            }

            // Lokasi
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Lokasi", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        TextButton(onClick = { locRefresh++ }, enabled = granted) { Text("Perbarui") }
                    }
                    when (val s = locState) {
                        LocState.Loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            Text("  Mencari lokasi GPS…")
                        }
                        is LocState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error)
                        is LocState.Ready -> {
                            InfoRow("Koordinat", String.format(LOCALE_ID, "%.6f, %.6f", s.location.latitude, s.location.longitude))
                            InfoRow("Akurasi", "±${s.location.accuracy.toInt()} m")
                            InfoRow("Jarak ke kantor", if (settings.hasOffice) formatDistance(distance) else "Kantor belum diatur")
                            if (settings.hasOffice) {
                                Text(
                                    if (inside) "✓ Di dalam area kantor" else "✗ Di luar area kantor",
                                    color = if (inside) Success else MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                            if (mock) {
                                Text("⚠ Lokasi palsu terdeteksi", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Foto
            val photoModifier = Modifier.fillMaxWidth().aspectRatio(3f / 4f).clip(RoundedCornerShape(16.dp))
            val current = photo
            if (current == null) {
                if (granted) {
                    CameraCapture(
                        onCaptured = { photo = it },
                        onFailure = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() },
                        modifier = photoModifier,
                    )
                }
            } else {
                Box {
                    AsyncImage(
                        model = current,
                        contentDescription = "Foto absen",
                        contentScale = ContentScale.Crop,
                        modifier = photoModifier,
                    )
                }
                OutlinedButton(
                    onClick = { current.delete(); photo = null },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Ulangi foto") }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Keterangan (opsional)") },
                modifier = Modifier.fillMaxWidth(),
            )

            if (blockReason != null) {
                Text(blockReason, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Button(
                onClick = { submit(location!!, photo!!) },
                enabled = blockReason == null && !saving,
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                if (saving) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Kirim Absen ${typeLabel(type)}")
                }
            }
        }
    }
}
