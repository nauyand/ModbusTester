package com.teknosolvo.absensi.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE date = :date ORDER BY timestamp ASC")
    fun observeByDate(date: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE id = :id")
    fun observeById(id: Long): Flow<Attendance?>

    @Query("SELECT photoPath FROM attendance")
    suspend fun allPhotoPaths(): List<String>

    @Insert
    suspend fun insert(attendance: Attendance): Long

    @Delete
    suspend fun delete(attendance: Attendance)

    @Query("DELETE FROM attendance")
    suspend fun deleteAll()
}
