/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.Icon
import cn.minimote.toolbox.constant.Icon.IconKey
import cn.minimote.toolbox.viewModel.MyViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class IconCacheHelper(
    private val viewModel: MyViewModel,
) {

    private val context: Context = viewModel.myContext
    private val packageManager: PackageManager = context.packageManager


    private val iconMap: Map<String, Int> = mapOf(
        IconKey.DEVELOPER_OPTION to R.drawable.ic_developer_option,
        IconKey.RECENT_TASK to R.drawable.ic_recent_task,
        IconKey.ACCESSIBILITY_OPTION to R.drawable.ic_accessibility_option,
        IconKey.SYSTEM_UI_DEMO_MODE to R.drawable.ic_system_ui_demo_mode,
        IconKey.ANDROID_EASTER_EGG to R.drawable.ic_android_easter_egg,


        IconKey.WOODEN_FISH to R.drawable.ic_wooden_fish,

        // 不可用
        IconKey.UNAVAILABLE to R.drawable.ic_forbid,

        IconKey.DEFAULT to R.drawable.ic_default,

        IconKey.BILIBILI to R.drawable.ic_bilibili,
        IconKey.DOUYIN to R.drawable.ic_douyin,
        IconKey.AMAP to R.drawable.ic_amap,
//        IconKey.HELLO_BIKE to R.drawable.ic_hello_bike,
        IconKey.HELLO_BIKE_SCAN to R.drawable.ic_scan_hello_bike,
        IconKey.JINGDONG to R.drawable.ic_jingdong,
        IconKey.MEITUAN to R.drawable.ic_meituan,
        IconKey.MEITUAN_SCAN to R.drawable.ic_scan_meituan,
        IconKey.NETEASE_CLOUDMUSIC to R.drawable.ic_netease_cloudmusic,
        IconKey.WECHAT to R.drawable.ic_wechat,
        IconKey.WECHAT_SCAN to R.drawable.ic_scan_wechat,
        IconKey.YUNSHANFU to R.drawable.ic_yunshanfu,
        IconKey.YUNSHANFU_SCAN to R.drawable.ic_scan_yunshanfu,
        IconKey.ALIPAY to R.drawable.ic_alipay,
        IconKey.ALIPAY_SCAN to R.drawable.ic_scan_alipay,
        IconKey.QQ to R.drawable.ic_qq,
        IconKey.QQ_MUSIC to R.drawable.ic_qq_music,

    )


    // 定义一个方法来获取缓存路径
    private fun getCachePath(): File {
        val cacheDir = viewModel.cachePath
        val appIconDir = File(cacheDir, Icon.ICON_CACHE_PATH)
        if(!appIconDir.exists()) {
            appIconDir.mkdirs()
        }
        return appIconDir
    }


    // 根据设备情况动态设置缓存大小
    private fun getMaxMemoryCacheSize(): Int {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
//        LogHelper.e("最大内存","maxMemory: $maxMemory KB")
        // 使用可用内存的 1/8 作为缓存大小，但不低于 1MB，不超过 16MB
        return minOf(maxOf(maxMemory / 8, 1024), 16 * 1024)
    }


    // 定义 LruCache
    private val iconCache: LruCache<String, Drawable> =
        object : LruCache<String, Drawable>(getMaxMemoryCacheSize()) {
            override fun sizeOf(key: String, value: Drawable): Int {
                return if(value is BitmapDrawable) {
                    value.bitmap.byteCount / 1024
                } else {
                    1
                }
            }
        }


    // 放置图标到内存
    private fun putDrawableToCache(iconKey: String, drawable: Drawable) {
        iconCache.put(iconKey, drawable)
    }


    // 保存图标到磁盘
    private fun saveDrawableToFile(iconKey: String, drawable: Drawable) {
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
    private fun loadDrawableFromCache(iconKey: String): Drawable? {
        return iconCache.get(iconKey)
    }


    // 清除内存缓存
    fun clearMemoryCache() {
        iconCache.evictAll()
    }


    // 从磁盘获取图标
    private fun loadDrawableFromFile(iconKey: String): Drawable? {
        val file = File(getCachePath(), "$iconKey.png")
        return if(file.exists()) {
            Drawable.createFromPath(file.absolutePath)
        } else {
            null
        }
    }


    // 从 drawable 获取图标
    fun getDrawableFromDrawable(iconKey: String): Drawable? {

//        var resourceId = R.drawable.ic_default
//        if(iconMap.containsKey(iconKey)) {
        val resourceId = iconMap[iconKey]
//        }
        return resourceId?.let { ContextCompat.getDrawable(context, it) }
    }


//    // 从 packageManager 中获取图标
//    private fun getDrawableFromPackageManager(
//        packageName: String,
//        activityName: String?,
//    ): Drawable? {
//        if(activityName != null) {
//            val componentName = ComponentName(packageName, activityName)
//            try {
//                return packageManager.getActivityIcon(componentName)
//            } catch(_: Exception) {
//                // 忽略异常
//            }
//        }
//
//        return try {
//            packageManager.getApplicationIcon(packageName)
//        } catch(_: Exception) {
//            getDefaultDrawable()
//        }
//    }


    // 从 packageManager 中获取高清图标
    fun getHighResDrawableFromPackageManager(
        packageName: String,
        activityName: String?,
    ): Drawable? {
        if(activityName != null) {
            val componentName = ComponentName(packageName, activityName)
            try {
                // 获取 ActivityInfo 对象
                val activityInfo = packageManager.getActivityInfo(componentName, 0)
                // 从 ActivityInfo 中获取 ApplicationInfo
                val applicationInfo = activityInfo.applicationInfo
                // 通过 ApplicationInfo 获取高清图标
                return packageManager.getApplicationIcon(applicationInfo)
            } catch(_: Exception) {
                // 忽略异常
            }
        }

        return try {
            // 获取 ApplicationInfo 对象
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            // 获取高清图标
            packageManager.getApplicationIcon(applicationInfo)
        } catch(_: Exception) {
            getDefaultDrawable()
        }
    }


    // 获取默认图标
    private fun getDefaultDrawable(): Drawable? {
        return ContextCompat.getDrawable(context, R.drawable.ic_default)
    }



    // 从所有渠道获取 Drawable 图标
    fun loadDrawableFromAllSources(
        iconKey: String,
        packageName: String,
        activityName: String?,
    ): Drawable {

        return getDrawableFromDrawable(iconKey) ?: getHighResDrawableFromPackageManager(
            packageName = packageName,
            activityName = activityName,
        )!!

//        Log.e("加载图标", "iconKey: $iconKey, packageName: $packageName, activityName: $activityName")
//        try {
//            val componentName = ComponentName(packageName, activityName)
//            return packageManager.getActivityIcon(componentName)
//        } catch(e: Exception) {
//            // 处理异常
//            return getIconFromDrawable(iconKey)
//        }

//        // 从内存中获取图标
//        var appIcon = loadDrawableFromCache(iconKey)
//        if(appIcon == null) {
//            // 从磁盘中获取图标
//            appIcon = loadDrawableFromFile(iconKey)
//            if(appIcon == null) {
//                // 从 drawable 中获取图标
//                appIcon = getDrawableFromDrawable(iconKey)
//                if(appIcon == null) {
//                    appIcon = getHighResDrawableFromPackageManager(
//                        packageName = packageName,
//                        activityName = activityName,
//                    )
//                }
//                if(appIcon != null) {
//                    putDrawableToCache(iconKey, appIcon)
//                    saveDrawableToFile(iconKey, appIcon)
//                }
////                Log.d("IconCacheManager", "从系统中获取了图标：$packageName")
//            } else {
////                Log.d("IconCacheManager", "从磁盘中获取了图标：$packageName")
//            }
//        } else {
////            Log.d("IconCacheManager", "从内存中获取了图标：$packageName")
//        }
//        return appIcon!!
//        return appIcon.toRoundedCornerDrawable()
    }

}
