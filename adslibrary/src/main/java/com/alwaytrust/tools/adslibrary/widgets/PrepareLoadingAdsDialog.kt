package com.alwaytrust.tools.adslibrary.widgets

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.alwaytrust.tools.adslibrary.R

class PrepareLoadingAdsDialog(context: Context) : Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_prepair_loading_ads)
    }
}