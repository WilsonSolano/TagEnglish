package com.example.tagenglish

import android.app.Application
import com.example.tagenglish.data.local.database.AppDatabase
import com.example.tagenglish.data.preferences.AppPreferences

class TagEnglishApp : Application() {

    // Instancias globales accesibles desde cualquier parte de la app
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val preferences: AppPreferences by lazy {
        AppPreferences(this)
    }

    override fun onCreate() {
        super.onCreate()
        // La DB se inicializa lazy — el seed ocurre automáticamente
        // la primera vez que se accede gracias al SeedCallback
    }
}
