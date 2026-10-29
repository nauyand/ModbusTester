package com.teknosolvo.absensi

import android.app.Application
import com.teknosolvo.absensi.data.AppDatabase
import com.teknosolvo.absensi.data.SettingsStore

class AbsensiApp : Application() {
    lateinit var db: AppDatabase
        private set
    lateinit var settings: SettingsStore
        private set

    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.build(this)
        settings = SettingsStore(this)
    }
}
