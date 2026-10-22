package com.example.apkexporter

import android.graphics.drawable.Drawable

data class AppItem(
    val label: String,
    val packageName: String,
    val versionName: String,
    val icon: Drawable,
    val apkPaths: List<String>,   // base APK first, then any split APKs
    val sizeBytes: Long,
    var selected: Boolean = false
) {
    val isSplit get() = apkPaths.size > 1
}
