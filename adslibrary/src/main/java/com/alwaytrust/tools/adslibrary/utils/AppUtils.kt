@file:Suppress("DEPRECATED_IDENTITY_EQUALS")
package com.alwaytrust.tools.adslibrary.utils

import android.content.Context
import android.content.pm.ApplicationInfo

object AppUtils {
    fun isDebuggable(context: Context): Boolean {
        return (context.applicationInfo.flags
                and ApplicationInfo.FLAG_DEBUGGABLE) !== 0
    }
}