package com.teknosolvo.absensi.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.teknosolvo.absensi.util.DATE_TIME
import com.teknosolvo.absensi.util.LOCALE_ID
import com.teknosolvo.absensi.util.formatDistance
import com.teknosolvo.absensi.util.toLocalDateTime
import com.teknosolvo.absensi.util.typeLabel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun DetailScreen(id: Long, onBack: () -> Unit) {
    val context = LocalContext.current
    val app = rememberApp()
    val scope = rememberCoroutineScope()
    val record by remember(id) { app.db.attendanceDao().observeById(id) }
        .collectAsStateWithLifecycle(initialValue = null)
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            BackTopBar("Detail Absen", onBack) {
                IconButton(onClick = { confirmDelete = true }, enabled = record != null) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus")
                }
            }
        },
    ) { padding ->
        val r = record
        if (r == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("Memuat…") }
            return@Scaffold
        }
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AsyncImage(
                model = File(r.photoPath),
                contentDescription = "Foto absen",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
            )
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    InfoRow("Jenis", typeLabel(r.type))
                    InfoRow("Waktu", r.timestamp.toLocalDateTime().format(DATE_TIME))
                    InfoRow("Status", r.status)
                    InfoRow("Koordinat", String.format(LOCALE_ID, "%.6f, %.6f", r.latitude, r.longitude))
                    InfoRow("Akurasi", "±${r.accuracyM.toInt()} m")
                    InfoRow("Jarak ke kantor", formatDistance(r.distanceM))
                    InfoRow("Area", if (r.distanceM == null) "-" else if (r.insideArea) "Di dalam area" else "Di luar area")
                    InfoRow("Alamat", r.address ?: "-")
                    InfoRow("Keterangan", r.note ?: "-")
                }
            }
            Button(
                onClick = {
                    val uri = Uri.parse("geo:${r.latitude},${r.longitude}?q=${r.latitude},${r.longitude}(Absen)")
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Buka di Maps") }
        }

        if (confirmDelete) {
            AlertDialog(
                onDismissRequest = { confirmDelete = false },
                title = { Text("Hapus data absen?") },
                text = { Text("Foto dan data lokasi akan dihapus permanen.") },
                confirmButton = {
                    TextButton(onClick = {
                        confirmDelete = false
                        scope.launch {
                            File(r.photoPath).delete()
                            app.db.attendanceDao().delete(r)
                            onBack()
                        }
                    }) { Text("Hapus") }
                },
                dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Batal") } },
            )
        }
    }
}
