/*
 * Copyright (c) 2024 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class IconCacheManager(private val context: Context) {
    private val packageManager: PackageManager = context.packageManager

    // 定义一个方法来获取缓存路径
    private fun getAppIconCachePath(context: Context): File {
        val cacheDir = context.cacheDir
        val appIconDir = File(cacheDir, "app_icon")
        if (!appIconDir.exists()) {
            appIconDir.mkdirs()
        }
        return appIconDir
    }

    // 定义 LruCache
    private val iconCache: LruCache<String, Drawable> = object : LruCache<String, Drawable>(100) {
        override fun sizeOf(key: String, value: Drawable): Int {
            return if (value is BitmapDrawable) {
                value.bitmap.byteCount / 1024
            } else {
                1
            }
        }
    }

    // 保存图标文件
    private fun saveIconToFile(packageName: String, drawable: Drawable) {
        val file = File(getAppIconCachePath(context), "$packageName.png")
        val bitmap = drawableToBitmap(drawable)
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // 从文件加载图标
    private fun loadIconFromFile(packageName: String): Drawable? {
        val file = File(getAppIconCachePath(context), "$packageName.png")
        return if (file.exists()) {
            Drawable.createFromPath(file.absolutePath)
        } else {
            null
        }
    }

    // 将 Drawable 转换为 Bitmap
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    // 获取图标
    fun getAppIcon(packageName: String): Drawable {
        // 尝试从内存缓存中获取图标
        var appIcon = iconCache.get(packageName)
        if (appIcon == null) {
            // 尝试从磁盘缓存中加载图标
            appIcon = loadIconFromFile(packageName)
            if (appIcon == null) {
                // 从包管理器获取图标并保存到磁盘缓存
                appIcon = packageManager.getApplicationIcon(packageName)
                saveIconToFile(packageName, appIcon)
                iconCache.put(packageName, appIcon)
//                Log.d("IconCacheManager", "从系统中获取了图标：$packageName")
            } else {
//                Log.d("IconCacheManager", "从磁盘中获取了图标：$packageName")
            }
        } else {
//            Log.d("IconCacheManager", "从内存中获取了图标：$packageName")
        }
        return appIcon
    }
}
