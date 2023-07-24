package com.alwaytrust.tools.adslibrary.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.alwaytrust.tools.adslibrary.AdmobFactory
import com.alwaytrust.tools.adslibrary.R
import com.facebook.shimmer.ShimmerFrameLayout

class BannerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    init {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.BannerView)
        inflate(context, R.layout.layout_banner_ads, this)
        val bannerId = typeArray.getString(R.styleable.BannerView_bnv_admob_id) ?: ""
        val bannerDebugId = typeArray.getString(R.styleable.BannerView_bnv_admob_debug_id) ?: ""
        typeArray.recycle()
        val shimmer = findViewById<ShimmerFrameLayout>(R.id.shimmerContainerBanner)
        val adContainer = findViewById<FrameLayout>(R.id.bannerContainer)
        AdmobFactory
            .BannerBuilder(adContainer)
            .setContext(context)
            .setAdmobId(bannerId)
            .setAdmobDebugId(bannerDebugId)
            .setIsPassWhenNetworkConnected(true)
            .setOnAdmobPreLoad {
                displayView(shimmer, true)
                shimmer.startShimmer()
            }.setOnLoadAdmobFailure {
                displayView(this, false)
                displayView(shimmer, false)
                displayView(adContainer, false)
            }.setOnAdmobLoaded {
                shimmer.stopShimmer()
                displayView(shimmer, false)
                displayView(adContainer, true)
                displayView(this, true)
            }.lazyLoad().show()
    }

    private fun displayView(view: View, isVisible: Boolean) {
        view.visibility = if(isVisible) View.VISIBLE else View.GONE
    }
}