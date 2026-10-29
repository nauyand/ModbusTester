package com.teknosolvo.absensi.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
    val employeeName: String = "",
    val employeeId: String = "",
    val officeLat: Double? = null,
    val officeLng: Double? = null,
    val radiusM: Int = 100,
    /** HH:mm */
    val workStart: String = "08:00",
    /** HH:mm */
    val workEnd: String = "17:00",
    /** Tolak absen di luar radius kantor. */
    val enforceRadius: Boolean = true,
) {
    val hasOffice: Boolean get() = officeLat != null && officeLng != null
}

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(load())
    val state: StateFlow<AppSettings> = _state.asStateFlow()

    fun update(transform: (AppSettings) -> AppSettings) {
        val next = transform(_state.value)
        save(next)
        _state.value = next
    }

    private fun load() = AppSettings(
        employeeName = prefs.getString("employeeName", "") ?: "",
        employeeId = prefs.getString("employeeId", "") ?: "",
        officeLat = prefs.getString("officeLat", null)?.toDoubleOrNull(),
        officeLng = prefs.getString("officeLng", null)?.toDoubleOrNull(),
        radiusM = prefs.getInt("radiusM", 100),
        workStart = prefs.getString("workStart", "08:00") ?: "08:00",
        workEnd = prefs.getString("workEnd", "17:00") ?: "17:00",
        enforceRadius = prefs.getBoolean("enforceRadius", true),
    )

    private fun save(s: AppSettings) = prefs.edit {
        putString("employeeName", s.employeeName)
        putString("employeeId", s.employeeId)
        putString("officeLat", s.officeLat?.toString())
        putString("officeLng", s.officeLng?.toString())
        putInt("radiusM", s.radiusM)
        putString("workStart", s.workStart)
        putString("workEnd", s.workEnd)
        putBoolean("enforceRadius", s.enforceRadius)
    }
}
