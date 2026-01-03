package com.bookkeeping

import android.app.Application
import com.google.firebase.FirebaseApp

class BookkeepingApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
