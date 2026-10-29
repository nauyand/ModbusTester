package com.teknosolvo.absensi.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.teknosolvo.absensi.data.Attendance
import com.teknosolvo.absensi.ui.theme.Success
import com.teknosolvo.absensi.ui.theme.Warning
import com.teknosolvo.absensi.util.DATE_KEY
import com.teknosolvo.absensi.util.DATE_LONG
import com.teknosolvo.absensi.util.TIME
import com.teknosolvo.absensi.util.formatDistance
import com.teknosolvo.absensi.util.toLocalDateTime
import com.teknosolvo.absensi.util.typeLabel
import java.io.File
import java.time.LocalDate

@Composable
fun HistoryScreen(onOpen: (Long) -> Unit, onBack: () -> Unit) {
    val app = rememberApp()
    val records by remember { app.db.attendanceDao().observeAll() }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val byDate = records.groupBy { it.date }

    Scaffold(topBar = { BackTopBar("Riwayat Absen", onBack) }) { padding ->
        if (records.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Belum ada data absen.")
            }
            return@Scaffold
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            byDate.forEach { (date, list) ->
                item(key = "header_$date") {
                    Text(
                        LocalDate.parse(date, DATE_KEY).format(DATE_LONG),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(list, key = { it.id }) { record ->
                    HistoryItem(record, onClick = { onOpen(record.id) })
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(record: Attendance, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = File(record.photoPath),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
            )
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    "${typeLabel(record.type)} • ${record.timestamp.toLocalDateTime().format(TIME)}",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    record.status,
                    color = if (record.status == "Tepat waktu") Success else Warning,
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    if (record.distanceM == null) "Jarak ke kantor: -"
                    else "${formatDistance(record.distanceM)} dari kantor" +
                        if (record.insideArea) "" else " (di luar area)",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
