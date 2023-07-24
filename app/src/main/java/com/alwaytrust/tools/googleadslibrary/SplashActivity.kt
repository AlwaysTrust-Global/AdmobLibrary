package com.alwaytrust.tools.googleadslibrary

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alwaytrust.tools.adslibrary.AdmobFactory

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdmobFactory
            .InterstitialBuilder(true)
            .setActivity(this)
            .setAdmobDebugId("ca-app-pub-3940256099942544/3419835294")
            .setIsPassWhenNetworkConnected(true)
            .setOnAdmobClosed(::startMain)
            .setOnLoadAdmobFailure(::startMain)
            .lazyLoad().show()
    }

    private fun startMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}