/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

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
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.Icon
import cn.minimote.toolbox.constant.Icon.IconKey
import cn.minimote.toolbox.dataClass.InstalledActivity
import cn.minimote.toolbox.dataClass.ToolActivity
import cn.minimote.toolbox.viewModel.ToolboxViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class IconCacheHelper(
    private val viewModel: ToolboxViewModel,
) {

    private val context: Context = viewModel.myContext
    private val packageManager: PackageManager = context.packageManager

    // 定义一个方法来获取缓存路径
    private fun getCachePath(): File {
        val cacheDir = viewModel.cachePath
        val appIconDir = File(cacheDir, Icon.ICON_CACHE_PATH)
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
    private fun putIconToCache(iconKey: String, drawable: Drawable) {
        iconCache.put(iconKey, drawable)
    }


    // 保存图标到磁盘
    private fun saveIconToFile(iconKey: String, drawable: Drawable) {
        val file = File(getCachePath(), "$iconKey.png")
        val bitmap = drawable.toBitmap()
        try {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
        } catch(e: IOException) {
            e.printStackTrace()
        }
    }


    // 从内存中获取图标
    private fun loadIconFromCache(iconKey: String): Drawable? {
        return iconCache.get(iconKey)
    }


    // 从磁盘获取图标
    private fun loadIconFromFile(iconKey: String): Drawable? {
        val file = File(getCachePath(), "$iconKey.png")
        return if(file.exists()) {
            Drawable.createFromPath(file.absolutePath)
        } else {
            null
        }
    }


    // 从 drawable 获取图标
    private fun getIconFromDrawable(iconKey: String): Drawable? {
        // 假设有一个映射表来关联 packageName 和 drawable 资源 ID
        val iconMap: Map<String, Int> = mapOf(
            IconKey.DEVELOPER_OPTION to R.drawable.ic_developer_option,
            IconKey.RECENT_TASK to R.drawable.ic_recent_task,
            IconKey.ACCESSIBILITY_OPTION to R.drawable.ic_accessibility_option,
        )

//        var resourceId = R.drawable.ic_default
//        if(iconMap.containsKey(iconKey)) {
        val resourceId = iconMap[iconKey]
//        }
        return resourceId?.let { ContextCompat.getDrawable(context, it) }
    }


    // 从 packageManager 中获取图标
    private fun getIconFromPackageManager(
        packageName: String,
        activityName: String?,
    ): Drawable? {
        if(activityName != null) {
            val componentName = ComponentName(packageName, activityName)
            return try {
                packageManager.getActivityIcon(componentName)
            } catch(_: Exception) {
                // 忽略异常并尝试获取应用图标
                try {
                    packageManager.getApplicationIcon(packageName)
                } catch(_: Exception) {
                    getDefaultIcon()
                }
            }
        }

        return try {
            packageManager.getApplicationIcon(packageName)
        } catch(_: Exception) {
            getDefaultIcon()
        }
    }


    // 获取默认图标
    private fun getDefaultIcon(): Drawable? {
        return ContextCompat.getDrawable(context, R.drawable.ic_default)
    }


    // 获取 Drawable 图标
    fun getIconDrawable(
        tool: ToolActivity,
    ): Drawable {
        return loadDrawableFromAllSources(
            iconKey = tool.iconKey,
            packageName = tool.packageName,
            activityName = tool.activityName,
        )
    }

    fun getIconDrawable(
        installedActivity: InstalledActivity,
    ): Drawable {
        return loadDrawableFromAllSources(
            iconKey = installedActivity.iconKey,
            packageName = installedActivity.packageName,
            activityName = installedActivity.activityName,
        )
    }


    // 获取圆形 Drawable 图标
    fun getIconCircularDrawable(
        tool: ToolActivity,
    ): Drawable {
        return getIconDrawable(tool).toCircularDrawable()
    }

    fun getIconCircularDrawable(
        installedActivity: InstalledActivity,
    ): Drawable {
        return getIconDrawable(installedActivity).toCircularDrawable()
    }


    // 获取 Icon 图标
    fun getIconIcon(
        tool: ToolActivity,
    ): android.graphics.drawable.Icon {
        return getIconDrawable(tool).toIcon(context)
    }


    // 从所有渠道获取 Drawable 图标
    private fun loadDrawableFromAllSources(
        iconKey: String,
        packageName: String,
        activityName: String?,
    ): Drawable {
//        Log.e("加载图标", "iconKey: $iconKey, packageName: $packageName, activityName: $activityName")
//        try {
//            val componentName = ComponentName(packageName, activityName)
//            return packageManager.getActivityIcon(componentName)
//        } catch(e: Exception) {
//            // 处理异常
//            return getIconFromDrawable(iconKey)
//        }

        // 从内存中获取图标
        var appIcon = loadIconFromCache(iconKey)
        if(appIcon == null) {
            // 从磁盘中获取图标
            appIcon = loadIconFromFile(iconKey)
            if(appIcon == null) {
                // 从 drawable 中获取图标
                appIcon = getIconFromDrawable(iconKey)
                if(appIcon == null) {
                    appIcon = getIconFromPackageManager(
                        packageName = packageName,
                        activityName = activityName,
                    )
                }
                if(appIcon != null) {
                    putIconToCache(iconKey, appIcon)
                    saveIconToFile(iconKey, appIcon)
                }
//                Log.d("IconCacheManager", "从系统中获取了图标：$packageName")
            } else {
//                Log.d("IconCacheManager", "从磁盘中获取了图标：$packageName")
            }
        } else {
//            Log.d("IconCacheManager", "从内存中获取了图标：$packageName")
        }
        return appIcon!!
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
        val bitmap = createBitmap(targetSizePx, targetSizePx)
        val canvas = Canvas(bitmap)

        // 获取背景颜色资源
        val backgroundColor = context.resources.getColor(backgroundColorResId, context.theme)

        // 设置背景颜色
        val backgroundPaint = android.graphics.Paint()
        backgroundPaint.color = backgroundColor
        canvas.drawRect(0f, 0f, targetSizePx.toFloat(), targetSizePx.toFloat(), backgroundPaint)

        // 缩放 Drawable 到目标大小
        val scaledDrawable = this.resize(targetSizePx, targetSizePx)

        // 使用 BitmapShader 将 Drawable 转换为 Shader
        val shader = BitmapShader(
            scaledDrawable.toBitmap(),
            Shader.TileMode.CLAMP,
            Shader.TileMode.CLAMP
        )
        val paint = android.graphics.Paint()
        paint.shader = shader

        // 设置圆形
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        val rect = RectF(0f, 0f, targetSizePx.toFloat(), targetSizePx.toFloat())
        canvas.drawOval(rect, paint)

        return bitmap.toDrawable(context.resources)
    }


    /*
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
    val bitmap = createBitmap(targetSizePx, targetSizePx)
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

    return bitmap.toDrawable(context.resources)
    }
    */


    // 辅助方法：缩放 Drawable 到指定大小
    private fun Drawable.resize(width: Int, height: Int): Drawable {
        val resizedBitmap =
            this.toBitmap().scale(width, height)
        return resizedBitmap.toDrawable(context.resources)
    }


    // 将 Drawable 转换为 Bitmap
    private fun Drawable.toBitmap(): Bitmap {
        val bitmap = createBitmap(this.intrinsicWidth, this.intrinsicHeight)
        val canvas = Canvas(bitmap)
        this.setBounds(0, 0, canvas.width, canvas.height)
        this.draw(canvas)
        return bitmap
    }


    fun Drawable.toIcon(context: Context): android.graphics.drawable.Icon {
        val bitmap = this.toBitmap()
        return IconCompat.createWithBitmap(bitmap).toIcon(context)
    }

}
