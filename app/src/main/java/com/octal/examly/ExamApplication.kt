package com.octal.examly

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ExamApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initializeApp()
    }

    private fun initializeApp() {
    }
}