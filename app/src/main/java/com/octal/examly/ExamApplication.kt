package com.octal.examly

import android.app.Application
import com.octal.examly.data.local.database.DatabaseSeeder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ExamApplication : Application() {

    @Inject
    lateinit var databaseSeeder: DatabaseSeeder

    override fun onCreate() {
        super.onCreate()

        initializeApp()
    }

    private fun initializeApp() {
        databaseSeeder.seedDatabase()
    }
}