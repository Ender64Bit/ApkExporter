package com.example.apkexporter

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ApkExporter {

    private const val FOLDER = "APK Exports"

    /** Exports one app into Downloads/APK Exports. Returns the file's Uri. */
    fun export(ctx: Context, app: AppItem): Uri {
        val safeName = app.label.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val ext = if (app.isSplit) "apks" else "apk"
        val fileName = "${safeName}_${app.versionName}.$ext"
        val mime = if (app.isSplit) "application/octet-stream"
        else "application/vnd.android.package-archive"

        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, mime)
            put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$FOLDER")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val resolver = ctx.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: error("Could not create file for ${app.label}")

        try {
            resolver.openOutputStream(uri)!!.use { out ->
                if (app.isSplit) {
                    // Bundle base + split APKs; installable with a split-APK installer (e.g. SAI)
                    ZipOutputStream(out.buffered()).use { zip ->
                        app.apkPaths.forEach { path ->
                            val f = File(path)
                            zip.putNextEntry(ZipEntry(f.name))
                            f.inputStream().use { it.copyTo(zip) }
                            zip.closeEntry()
                        }
                    }
                } else {
                    File(app.apkPaths.first()).inputStream().use { it.copyTo(out) }
                }
            }
            resolver.update(uri, ContentValues().apply {
                put(MediaStore.Downloads.IS_PENDING, 0)
            }, null, null)
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            throw e
        }
        return uri
    }
}
