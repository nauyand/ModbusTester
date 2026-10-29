package com.teknosolvo.absensi.util

import com.teknosolvo.absensi.data.AppSettings
import com.teknosolvo.absensi.data.TYPE_IN
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val LOCALE_ID: Locale = Locale.forLanguageTag("id-ID")
val DATE_KEY: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
val TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
val TIME_SHORT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
val DATE_LONG: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", LOCALE_ID)
val DATE_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

fun Long.toLocalDateTime(): LocalDateTime =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()

fun parseTimeOrNull(text: String): LocalTime? =
    runCatching { LocalTime.parse(text.trim(), TIME_SHORT) }.getOrNull()

fun typeLabel(type: String) = if (type == TYPE_IN) "Masuk" else "Pulang"

fun formatDistance(meters: Float?): String = when {
    meters == null -> "-"
    meters >= 1000 -> String.format(LOCALE_ID, "%.2f km", meters / 1000)
    else -> "${meters.toInt()} m"
}

/** Status kehadiran dibanding jam kerja di pengaturan. */
fun attendanceStatus(type: String, time: LocalTime, settings: AppSettings): String {
    return if (type == TYPE_IN) {
        val start = parseTimeOrNull(settings.workStart) ?: return "Masuk"
        val late = Duration.between(start, time).toMinutes()
        if (late > 0) "Terlambat $late menit" else "Tepat waktu"
    } else {
        val end = parseTimeOrNull(settings.workEnd) ?: return "Pulang"
        val early = Duration.between(time, end).toMinutes()
        if (early > 0) "Pulang cepat $early menit" else "Tepat waktu"
    }
}
