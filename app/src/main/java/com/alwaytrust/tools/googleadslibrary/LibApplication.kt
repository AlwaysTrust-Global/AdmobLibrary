package com.alwaytrust.tools.googleadslibrary

import android.app.Application
import com.alwaytrust.tools.adslibrary.AdmobFactory

class LibApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AdmobFactory
            .InitializeBuilder()
            .setContext(this)
            .build()
    }
}