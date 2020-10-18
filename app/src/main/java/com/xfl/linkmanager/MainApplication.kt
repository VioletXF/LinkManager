package com.xfl.linkmanager

import android.app.Application
import android.content.pm.PackageManager

class MainApplication: Application() {
    companion object {
        fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
            return try {
                packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }
}