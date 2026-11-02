package dev.apktwin

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build

data class ApkInfo(val packageName: String, val label: String, val versionName: String?)

object ApkInspector {
    @Suppress("DEPRECATION")
    fun inspect(context: Context, uri: Uri): ApkInfo? {
        val cacheFile = java.io.File(context.cacheDir, "inspect-${System.nanoTime()}.apk")
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                cacheFile.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            val pm = context.packageManager
            val flags = PackageManager.GET_META_DATA
            val info = if (Build.VERSION.SDK_INT >= 33) {
                pm.getPackageArchiveInfo(cacheFile.absolutePath, PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                pm.getPackageArchiveInfo(cacheFile.absolutePath, flags)
            } ?: return null
            val appInfo = info.applicationInfo ?: return null
            appInfo.sourceDir = cacheFile.absolutePath
            appInfo.publicSourceDir = cacheFile.absolutePath
            ApkInfo(
                packageName = info.packageName,
                label = pm.getApplicationLabel(appInfo).toString(),
                versionName = info.versionName
            )
        } catch (_: Exception) {
            null
        } finally {
            cacheFile.delete()
        }
    }
}
