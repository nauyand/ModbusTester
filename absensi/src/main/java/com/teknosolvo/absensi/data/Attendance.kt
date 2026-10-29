package com.teknosolvo.absensi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

const val TYPE_IN = "IN"
const val TYPE_OUT = "OUT"

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** [TYPE_IN] atau [TYPE_OUT]. */
    val type: String,
    val timestamp: Long,
    /** yyyy-MM-dd, untuk query per hari. */
    val date: String,
    val latitude: Double,
    val longitude: Double,
    val accuracyM: Float,
    /** Jarak ke kantor dalam meter, null kalau lokasi kantor belum diatur. */
    val distanceM: Float?,
    val insideArea: Boolean,
    val address: String?,
    val photoPath: String,
    val status: String,
    val note: String?,
)
