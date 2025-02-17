/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.others

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.core.content.ContextCompat
import cn.minimote.toolbox.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class IconCacheManager(
    private val context: Context,
) {

    private val packageManager: PackageManager = context.packageManager

    // 定义一个方法来获取缓存路径
    private fun getCachePath(context: Context): File {
        val cacheDir = context.cacheDir
        val appIconDir = File(cacheDir, "app_icon")
        if(!appIconDir.exists()) {
            appIconDir.mkdirs()
        }
        return appIconDir
    }

    // 定义 LruCache
    private val iconCache: LruCache<String, Drawable> = object : LruCache<String, Drawable>(1024) {
        override fun sizeOf(key: String, value: Drawable): Int {
            return if(value is BitmapDrawable) {
                value.bitmap.byteCount / 1024
            } else {
                1
            }
        }
    }


    // 放置图标到内存
    private fun putIconToCache(iconName: String, drawable: Drawable) {
        iconCache.put(iconName, drawable)
    }


    // 保存图标到磁盘
    private fun saveIconToFile(iconName: String, drawable: Drawable) {
        val file = File(getCachePath(context), "$iconName.png")
        val bitmap = drawableToBitmap(drawable)
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch(e: IOException) {
            e.printStackTrace()
        }
    }


    // 从内存中获取图标
    private fun loadIconFromCache(iconName: String): Drawable? {
        return iconCache.get(iconName)
    }


    // 从磁盘获取图标
    private fun loadIconFromFile(iconName: String): Drawable? {
        val file = File(getCachePath(context), "$iconName.png")
        return if(file.exists()) {
            Drawable.createFromPath(file.absolutePath)
        } else {
            null
        }
    }


    // 从 drawable 获取图标
    private fun getIconFromDrawable(iconName: String): Drawable? {
        // 假设有一个映射表来关联 packageName 和 drawable 资源 ID
        val iconMap: Map<String, Int> = mapOf(
            "developer_option" to R.drawable.ic_developer_option,
            "recent_task" to R.drawable.ic_recent_task,
            "accessibility_option" to R.drawable.ic_accessibility_option,
        )

//        var resourceId = R.drawable.ic_default
//        if(iconMap.containsKey(iconName)) {
        val resourceId = iconMap[iconName]
//        }
        return resourceId?.let { ContextCompat.getDrawable(context, it) }
    }


    // 从 packageManager 中获取图标
    private fun getIconFromPackageManager(
        packageName: String,
        activityName: String,
    ): Drawable? {
        try {
            val componentName = ComponentName(packageName, activityName)
            return packageManager.getActivityIcon(componentName)
        } catch(e: Exception) {
            return try {
                packageManager.getApplicationIcon(packageName)
            } catch(e: Exception) {
                getDefaultIcon()
            }
        }
    }


    // 获取默认图标
    private fun getDefaultIcon(): Drawable? {
        return ContextCompat.getDrawable(context, R.drawable.ic_default)
    }


    // 获取图标
    fun getIcon(
        iconName: String,
        packageName: String,
        activityName: String,
    ): Drawable? {
//        try {
//            val componentName = ComponentName(packageName, activityName)
//            return packageManager.getActivityIcon(componentName)
//        } catch(e: Exception) {
//            // 处理异常
//            return getIconFromDrawable(iconName)
//        }

        // 从内存中获取图标
        var appIcon = loadIconFromCache(iconName)
        if(appIcon == null) {
            // 从磁盘中获取图标
            appIcon = loadIconFromFile(iconName)
            if(appIcon == null) {
                // 从 drawable 中获取图标
                appIcon = getIconFromDrawable(iconName)
                if(appIcon == null) {
                    appIcon = getIconFromPackageManager(
                        packageName = packageName,
                        activityName = activityName,
                    )
                }
                if(appIcon != null) {
                    putIconToCache(iconName, appIcon)
                    saveIconToFile(iconName, appIcon)
                }
//                Log.d("IconCacheManager", "从系统中获取了图标：$packageName")
            } else {
//                Log.d("IconCacheManager", "从磁盘中获取了图标：$packageName")
            }
        } else {
//            Log.d("IconCacheManager", "从内存中获取了图标：$packageName")
        }
        return appIcon?.toCircularDrawable()
//        return appIcon.toRoundedCornerDrawable()
    }


    // 将 Drawable 转换为圆形 Drawable，外部透明
    private fun Drawable.toCircularDrawable(
        backgroundColorResId: Int = android.R.color.transparent,
        targetSizeResId: Int = R.dimen.layout_size_1_large,
    ): Drawable {
        val targetSizeDp = context.resources.getDimensionPixelSize(targetSizeResId)

        // 将 dp 转换为 px
        val targetSizePx = (targetSizeDp * context.resources.displayMetrics.density).toInt()

        // 创建一个与目标大小相同的 Bitmap
        val bitmap = Bitmap.createBitmap(targetSizePx, targetSizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 获取背景颜色资源
        val backgroundColor = context.resources.getColor(backgroundColorResId, context.theme)

        // 设置背景颜色
        val backgroundPaint = android.graphics.Paint()
        backgroundPaint.color = backgroundColor
        canvas.drawRect(0f, 0f, targetSizePx.toFloat(), targetSizePx.toFloat(), backgroundPaint)

        // 缩放 Drawable 到目标大小
        val scaledDrawable = resizeDrawable(this, targetSizePx, targetSizePx)

        // 使用 BitmapShader 将 Drawable 转换为 Shader
        val shader = BitmapShader(
            drawableToBitmap(scaledDrawable),
            Shader.TileMode.CLAMP,
            Shader.TileMode.CLAMP
        )
        val paint = android.graphics.Paint()
        paint.shader = shader

        // 设置圆形
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        val rect = RectF(0f, 0f, targetSizePx.toFloat(), targetSizePx.toFloat())
        canvas.drawOval(rect, paint)

        return BitmapDrawable(context.resources, bitmap)
    }


    // 将 Drawable 转换为圆角矩形 Drawable，外部透明
    private fun Drawable.toRoundedCornerDrawable(
        cornerRadiusResId: Int = R.dimen.layout_size_1_small,
        backgroundColorResId: Int = android.R.color.transparent,
        targetSizeResId: Int = R.dimen.layout_size_1_large,
    ): Drawable {
        val cornerRadius = context.resources.getDimensionPixelSize(cornerRadiusResId).toFloat()
        val targetSizeDp = context.resources.getDimensionPixelSize(targetSizeResId)

        // 将 dp 转换为 px
        val targetSizePx = (targetSizeDp * context.resources.displayMetrics.density).toInt()

        // 创建一个与目标大小相同的 Bitmap
        val bitmap = Bitmap.createBitmap(targetSizePx, targetSizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 获取背景颜色资源
        val backgroundColor = context.resources.getColor(backgroundColorResId, context.theme)

        // 设置背景颜色
        val backgroundPaint = android.graphics.Paint()
        backgroundPaint.color = backgroundColor
        canvas.drawRect(0f, 0f, targetSizePx.toFloat(), targetSizePx.toFloat(), backgroundPaint)

        // 缩放 Drawable 到目标大小
        val scaledDrawable = resizeDrawable(this, targetSizePx, targetSizePx)

        // 使用 BitmapShader 将 Drawable 转换为 Shader
        val shader = BitmapShader(
            drawableToBitmap(scaledDrawable),
            Shader.TileMode.CLAMP,
            Shader.TileMode.CLAMP
        )
        val paint = android.graphics.Paint()
        paint.shader = shader

        // 设置圆角
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        val rect = RectF(0f, 0f, targetSizePx.toFloat(), targetSizePx.toFloat())
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

        return BitmapDrawable(context.resources, bitmap)
    }


    // 辅助方法：缩放 Drawable 到指定大小
    private fun resizeDrawable(drawable: Drawable, width: Int, height: Int): Drawable {
        val resizedBitmap =
            Bitmap.createScaledBitmap(drawableToBitmap(drawable), width, height, true)
        return BitmapDrawable(context.resources, resizedBitmap)
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
}
