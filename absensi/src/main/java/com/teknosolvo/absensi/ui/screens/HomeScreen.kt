package com.teknosolvo.absensi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teknosolvo.absensi.data.Attendance
import com.teknosolvo.absensi.data.TYPE_IN
import com.teknosolvo.absensi.data.TYPE_OUT
import com.teknosolvo.absensi.ui.theme.Success
import com.teknosolvo.absensi.ui.theme.Warning
import com.teknosolvo.absensi.util.DATE_KEY
import com.teknosolvo.absensi.util.DATE_LONG
import com.teknosolvo.absensi.util.TIME
import com.teknosolvo.absensi.util.toLocalDateTime
import kotlinx.coroutines.delay
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onCheck: (String) -> Unit, onHistory: () -> Unit, onSettings: () -> Unit) {
    val app = rememberApp()
    val settings by app.settings.state.collectAsStateWithLifecycle()
    val now by produceState(LocalDateTime.now()) {
        while (true) {
            value = LocalDateTime.now()
            delay(1000)
        }
    }
    val today = now.toLocalDate().format(DATE_KEY)
    val records by remember(today) { app.db.attendanceDao().observeByDate(today) }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val checkIn = records.firstOrNull { it.type == TYPE_IN }
    val checkOut = records.lastOrNull { it.type == TYPE_OUT }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Absensi") },
                actions = {
                    IconButton(onClick = onHistory) { Icon(Icons.Default.DateRange, "Riwayat") }
                    IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, "Pengaturan") }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(settings.employeeName, style = MaterialTheme.typography.titleLarge)
                    Text("NIP ${settings.employeeId}", style = MaterialTheme.typography.bodyMedium)
                    Text(now.format(DATE_LONG), Modifier.padding(top = 12.dp))
                    Text(now.format(TIME), style = MaterialTheme.typography.displayMedium)
                    Text(
                        "Jam kerja ${settings.workStart} – ${settings.workEnd}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TodayCard("Masuk", checkIn, Modifier.weight(1f))
                TodayCard("Pulang", checkOut, Modifier.weight(1f))
            }

            if (!settings.hasOffice) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        "Lokasi kantor belum diatur. Buka Pengaturan lalu tekan \"Pakai lokasi saat ini\" " +
                            "agar validasi radius aktif.",
                        Modifier.padding(16.dp),
                    )
                }
            }

            Button(
                onClick = { onCheck(TYPE_IN) },
                enabled = checkIn == null,
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) { Text(if (checkIn == null) "Absen Masuk" else "Sudah absen masuk") }
            OutlinedButton(
                onClick = { onCheck(TYPE_OUT) },
                enabled = checkIn != null && checkOut == null,
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) { Text(if (checkOut == null) "Absen Pulang" else "Sudah absen pulang") }
        }
    }
}

@Composable
private fun TodayCard(label: String, record: Attendance?, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(
                record?.timestamp?.toLocalDateTime()?.format(TIME) ?: "--:--:--",
                style = MaterialTheme.typography.headlineSmall,
            )
            if (record != null) {
                Text(
                    record.status,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (record.status == "Tepat waktu") Success else Warning,
                )
            }
        }
    }
}
