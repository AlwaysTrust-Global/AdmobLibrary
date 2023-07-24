package com.alwaytrust.tools.adslibrary

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import com.alwaytrust.tools.adslibrary.utils.AppUtils
import com.alwaytrust.tools.adslibrary.utils.NetworkUtils
import com.alwaytrust.tools.adslibrary.widgets.PrepareLoadingAdsDialog
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.lang.ref.WeakReference

class AdmobFactory(
    private val builder: Builder
) {
    private val admobPlayer by lazy {
        AdmobPlayer(onShow = builder::shower)
    }

    fun initialize() {
        if(builder.context == null) {
            throw NullPointerException("Chưa truyền context vào AdmobManagement.Builder")
        }
        builder.context?.get()?.let {
            MobileAds.initialize(it)
            val requestConfiguration = RequestConfiguration.Builder()
                .setTestDeviceIds((builder as InitializeBuilder).deviceIds)
                .build()
            MobileAds.setRequestConfiguration(requestConfiguration)
        }
    }

    fun lazyBuild(): AdmobPlayer {
        return admobPlayer
    }

    class BannerBuilder(
        private val anchorView: ViewGroup
    ) : Builder() {
        private val adView by lazy {
            AdView(context?.get()!!)
        }

        private val adListener = object : AdListener() {
            override fun onAdLoaded() {
                onAdmobLoaded.invoke()
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                onLoadAdmobFailure.invoke()
            }
        }

        override fun lazyLoad(): AdmobPlayer {
            context?.get()?.let { ctx ->
                if(!NetworkUtils.isNetworkAvailable(ctx)) {
                    onLoadAdmobFailure.invoke()
                } else {
                    onPreLoad.invoke()

                    adView.adUnitId = if(AppUtils.isDebuggable(ctx)) admobDebugId else admobId
                    val adSize: AdSize = getAdBannerSize(ctx)
                    adView.setAdSize(adSize)
                    adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                    adView.loadAd(AdRequest.Builder().build())
                    adView.adListener = adListener
                }
            }

            return AdmobFactory(this).lazyBuild()
        }

        override fun shower() {
            anchorView.addView(adView)
        }

        private fun getAdBannerSize(ctx: Context): AdSize {
            // Step 2 - Determine the screen width (less decorations) to use for the ad width.
            val displayMetrics = Resources.getSystem().displayMetrics
            val widthPixels = displayMetrics.widthPixels.toFloat()
            val density = displayMetrics.density
            val adWidth = (widthPixels / density).toInt()

            // Step 3 - Get adaptive ad size and return for setting on the ad view.
            return AdSize.getPortraitAnchoredAdaptiveBannerAdSize(ctx, adWidth)
        }
    }

    class InterstitialBuilder(
        private val isShowDefaultLoading: Boolean = false
    ) : Builder() {
        private val prepareLoadingDialog by lazy {
            createDialogFullScreen(activity?.get()!!)
        }
        var interstitialAd: InterstitialAd? = null

        private val interstitialCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                onAdmobClosed.invoke()
                super.onAdDismissedFullScreenContent()
            }
        }

        override fun lazyLoad(): AdmobPlayer {
            activity?.get()?.let { ctx ->
                if(isShowDefaultLoading) prepareLoadingDialog.show()
                onPreLoad.invoke()
                if(isPassWhenNetworkAvailable && !NetworkUtils.isNetworkAvailable(ctx)) {
                    onLoadAdmobFailure.invoke()
                    if(isShowDefaultLoading && prepareLoadingDialog.isShowing) prepareLoadingDialog.hide()
                } else {
                    InterstitialAd.load(ctx, if(AppUtils.isDebuggable(ctx)) admobDebugId
                    else admobId, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            super.onAdFailedToLoad(p0)
                            isLoadFailed = true
                            onLoadAdmobFailure.invoke()
                            if(isShowDefaultLoading && prepareLoadingDialog.isShowing) prepareLoadingDialog.hide()
                        }

                        override fun onAdLoaded(p0: InterstitialAd) {
                            super.onAdLoaded(p0)
                            isLoadFailed = false
                            isLoadDone = true
                            interstitialAd = p0
                            interstitialAd?.fullScreenContentCallback = interstitialCallback
                            onAdmobLoaded.invoke()

                            if(isCallingShowing) {
                                shower()
                            }
                        }
                    })
                }
            }

            return AdmobFactory(this).lazyBuild()
        }

        private fun createDialogFullScreen(ctx: Context): Dialog = PrepareLoadingAdsDialog(ctx)

        override fun shower() {
            if(activity?.get() == null) {
               throw NullPointerException("Set Activity cho interstitial sử dụng")
            }
            if(isLoadFailed) {
                onAdmobClosed.invoke()
            }

            activity?.get()?.let {
                isCallingShowing = true
                if(isLoadDone) {
                    if(isShowDefaultLoading && prepareLoadingDialog.isShowing) prepareLoadingDialog.hide()
                    interstitialAd?.show(it)
                }
            }
        }
    }

    class InitializeBuilder : Builder() {
        internal var deviceIds = mutableListOf<String>()

        fun setDeviceIds(deviceIds: MutableList<String>): InitializeBuilder {
            this.deviceIds.clear()
            this.deviceIds.addAll(deviceIds)
            return this
        }

        fun setDeviceIds(vararg deviceIds: String): InitializeBuilder {
            this.deviceIds.clear()
            this.deviceIds.addAll(deviceIds)
            return this
        }

        override fun build() {
            AdmobFactory(this).initialize()
        }

        override fun lazyLoad(): AdmobPlayer {
            return AdmobFactory(this).lazyBuild()
        }

        override fun shower() {

        }
    }

    abstract class Builder {
        protected var isCallingShowing = false
        protected var isLoadDone = false
        internal var context: WeakReference<Context>? = null
        internal var activity: WeakReference<Activity>? = null
        internal var isLoadFailed: Boolean = false
        internal var admobId: String = ""
        internal var admobDebugId: String = ""
        internal var onAdmobClosed: () -> Unit = {}
        internal var onPreLoad: () -> Unit = {}
        internal var onAdmobLoaded: () -> Unit = {}
        internal var onLoadAdmobFailure: () -> Unit = {}
        internal var onAdmobAvailable: () -> Unit = {}
        internal var isPassWhenNetworkAvailable = false

        fun setOnAdmobLoaded(onAdmobLoaded: () -> Unit): Builder {
            this.onAdmobLoaded = onAdmobLoaded
            return this
        }

        fun setOnAdmobPreLoad(onPreLoad: () -> Unit): Builder {
            this.onPreLoad = onPreLoad
            return this
        }

        fun setOnLoadAdmobFailure(onLoadAdmobFailure: () -> Unit): Builder {
            this.onLoadAdmobFailure = onLoadAdmobFailure
            return this
        }

        fun setContext(context: Context): Builder {
            this.context = WeakReference(context)
            return this
        }

        fun setActivity(activity: Activity): Builder {
            this.activity = WeakReference(activity)
            return this
        }

        fun setOnAdmobAvailable(onAdmobAvailable: () -> Unit): Builder {
            this.onAdmobAvailable = onAdmobAvailable
            return this
        }

        fun setAdmobDebugId(admobIdDebug: String): Builder {
            this.admobDebugId = admobIdDebug
            return this
        }

        fun setAdmobId(admobId: String): Builder {
            this.admobId = admobId
            return this
        }

        fun setOnAdmobClosed(onAdmobClosed: () -> Unit): Builder {
            this.onAdmobClosed = onAdmobClosed
            return this
        }

        /**
         * @param isPassWhenNetwork
         * Nếu mạng không khả dụng sẽ gọi hàm onLoadAdmobFailure
         */
        fun setIsPassWhenNetworkConnected(isPassWhenNetwork: Boolean): Builder {
            this.isPassWhenNetworkAvailable = isPassWhenNetwork
            return this
        }

        fun initialize(builder: Builder) {
            AdmobFactory(this).initialize()
        }

        open fun build() {}

        abstract fun lazyLoad(): AdmobPlayer
        abstract fun shower()
    }

    class AdmobPlayer(
        private val onShow: () -> Unit
    ) {
        fun show() = onShow.invoke()
    }
}