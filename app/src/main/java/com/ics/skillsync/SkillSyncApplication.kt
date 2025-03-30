package com.ics.skillsync

import android.app.Application
import com.google.firebase.FirebaseApp

class SkillSyncApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
} 