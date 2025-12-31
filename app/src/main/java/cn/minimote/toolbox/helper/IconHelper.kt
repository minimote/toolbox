/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.Icon.ICON_SIZE
import cn.minimote.toolbox.constant.Icon.IconKey
import cn.minimote.toolbox.dataClass.InstalledApp
import cn.minimote.toolbox.dataClass.Tool
import cn.minimote.toolbox.helper.DimensionHelper.getLayoutSize
import cn.minimote.toolbox.viewModel.MyViewModel
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.request.Disposable
import coil3.request.ImageRequest
import coil3.request.target
import coil3.request.transformations
import coil3.transform.CircleCropTransformation


object IconHelper {


    private val iconMap: Map<String, Int> by lazy {
        mapOf(
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
            IconKey.MEITUAN_PAY_CODE to R.drawable.ic_pay_code_meituan,

            IconKey.NETEASE_CLOUDMUSIC to R.drawable.ic_netease_cloudmusic,

            IconKey.WECHAT to R.drawable.ic_wechat,
            IconKey.WECHAT_SCAN to R.drawable.ic_scan_wechat,
            IconKey.WECHAT_PAY_CODE to R.drawable.ic_pay_code_wechat,
            IconKey.WECHAT_MY_CARD to R.drawable.ic_my_card_wechat,

            IconKey.YUNSHANFU to R.drawable.ic_yunshanfu,
            IconKey.YUNSHANFU_SCAN to R.drawable.ic_scan_yunshanfu,
            IconKey.YUNSHANFU_PAY_CODE to R.drawable.ic_pay_code_yunshanfu,
            IconKey.YUNSHANFU_RIDE_CODE to R.drawable.ic_ride_code_yunshanfu,

            IconKey.ALIPAY to R.drawable.ic_alipay,
            IconKey.ALIPAY_SCAN to R.drawable.ic_scan_alipay,
            IconKey.ALIPAY_PAY_CODE to R.drawable.ic_pay_code_alipay,
            IconKey.ALIPAY_COLLECT_CODE to R.drawable.ic_collect_code_alipay,
            IconKey.ALIPAY_RIDE_CODE to R.drawable.ic_ride_code_alipay,

            IconKey.QQ to R.drawable.ic_qq,

            IconKey.QQ_MUSIC to R.drawable.ic_qq_music,

            )
    }


    private var ImageView.imageDisposable: Disposable?
        get() = getTag(R.id.image_loader_disposable_tag) as? Disposable
        set(value) {
            setTag(R.id.image_loader_disposable_tag, value)
        }


    fun getImageLoader(context: Context): ImageLoader {
        return SingletonImageLoader.get(context)
    }


    // 获取 Coil 的缓存信息
    fun printCoilCacheInfo(context: Context) {
//        val imageLoader = Coil.imageLoader(context)
        val imageLoader = getImageLoader(context)

        // 获取磁盘缓存信息
        val diskCache = imageLoader.diskCache
        if(diskCache != null) {
            LogHelper.e("磁盘缓存目录: ", "${diskCache.directory}")
            LogHelper.e("磁盘缓存最大大小: ", "${diskCache.maxSize} bytes")
            LogHelper.e("磁盘缓存当前大小: ", "${diskCache.size} bytes")
        }

        // 获取内存缓存信息
        val memoryCache = imageLoader.memoryCache
        if(memoryCache != null) {
            LogHelper.e("内存缓存最大大小: ", "${memoryCache.maxSize} bytes")
            LogHelper.e("内存缓存当前大小: ", "${memoryCache.size} bytes")
        }
    }


    fun clearCoilCache(context: Context) {
//        val imageLoader = Coil.imageLoader(context)
//        imageLoader.memoryCache?.clear()
//        imageLoader.diskCache?.clear()

        val imageLoader = getImageLoader(context)
        imageLoader.memoryCache?.clear()
    }


    private fun ImageView.enqueueImageRequest(request: ImageRequest) {
        this.cancelLoadImage()
        this.imageDisposable = getImageLoader(context).enqueue(request)
    }


    // 设置图标
    fun ImageView.loadImage(
        viewModel: MyViewModel,
        tool: Tool,
        progressBar: ProgressBar? = null,
    ) {

        val request = ImageRequest.Builder(this.context)
            .data(viewModel.getDrawable(tool))
            .transformations(CircleCropTransformation())
            .target(this)
            .listener(
                onStart = {
                    // 加载开始
                    progressBar?.visibility = View.VISIBLE
                },
                onSuccess = { _, _ ->
                    // 加载成功
                    progressBar?.visibility = View.INVISIBLE
                },
                onError = { _, _ ->
                    // 加载失败
                    progressBar?.visibility = View.INVISIBLE
                },
                onCancel = { _ ->
                    // 加载取消
                    progressBar?.visibility = View.INVISIBLE
                },
            )
            .memoryCacheKey(tool.iconKey)
            .diskCacheKey(tool.iconKey)
            .build()

        this.enqueueImageRequest(request)

    }

    fun ImageView.loadImage(
        viewModel: MyViewModel,
        installedApp: InstalledApp,
        progressBar: ProgressBar? = null,
    ) {

        val request = ImageRequest.Builder(this.context)
            .data(viewModel.getDrawable(installedApp))
            .transformations(CircleCropTransformation())
            .target(this)
            .listener(
                onStart = {
                    // 加载开始
                    progressBar?.visibility = View.VISIBLE
                },
                onSuccess = { _, _ ->
                    // 加载成功
                    progressBar?.visibility = View.INVISIBLE
                },
                onError = { _, _ ->
                    // 加载失败
                    progressBar?.visibility = View.INVISIBLE
                },
                onCancel = { _ ->
                    // 加载取消
                    progressBar?.visibility = View.INVISIBLE
                },
            )
            .memoryCacheKey(installedApp.iconKey)
            .diskCacheKey(installedApp.iconKey)
            .build()

        this.enqueueImageRequest(request)

    }


    // 取消图片加载
    fun ImageView.cancelLoadImage() {
//        this.dispose()
//        this.imageDisposable?.dispose()
//        this.imageDisposable = null
    }


    // 获取 Drawable 图标
    fun MyViewModel.getDrawable(
        tool: Tool,
    ): Drawable {
        return getDrawable(
            iconKey = tool.iconKey,
            packageName = tool.packageName,
            activityName = tool.activityName,
        )
//        return this.iconCacheHelper.loadDrawableFromAllSources(
//            iconKey = tool.iconKey,
//            packageName = tool.packageName,
//            activityName = tool.activityName,
//        )
    }

    fun MyViewModel.getDrawable(
        installedApp: InstalledApp,
    ): Drawable {
        return getDrawable(
            iconKey = installedApp.iconKey,
            packageName = installedApp.packageName,
            activityName = installedApp.activityName,
        )
//        return this.iconCacheHelper.loadDrawableFromAllSources(
//            iconKey = installedApp.iconKey,
//            packageName = installedApp.packageName,
//            activityName = installedApp.activityName,
//        )
    }

    private fun MyViewModel.getDrawable(
        iconKey: String,
        packageName: String,
        activityName: String?,
    ): Drawable {
        return getDrawableFromDrawable(iconKey) ?: getHighResDrawableFromPackageManager(
            packageName = packageName,
            activityName = activityName,
        ) ?: getDefaultDrawable()
    }


//    fun getIconResourceId(iconKey: String): Int? {
//        return iconMap[iconKey]
//    }


    // 从 drawable 获取图标
    private fun MyViewModel.getDrawableFromDrawable(iconKey: String): Drawable? {

//        var resourceId = R.drawable.ic_default
//        if(iconMap.containsKey(iconKey)) {
        val resourceId = iconMap[iconKey]
//        }
        return resourceId?.let { ContextCompat.getDrawable(this.myContext, it) }
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
    private fun MyViewModel.getHighResDrawableFromPackageManager(
        packageName: String,
        activityName: String?,
    ): Drawable? {

        val packageManager = this.myContext.packageManager

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
            null
        }
    }


    // 获取默认图标
    private fun MyViewModel.getDefaultDrawable(): Drawable {
        return ContextCompat.getDrawable(this.myContext, R.drawable.ic_default)!!
    }


    // 获取 Icon 图标
    private fun MyViewModel.getIcon(
        tool: Tool,
    ): android.graphics.drawable.Icon {
        return getDrawable(tool).toIcon(myContext)
    }
    // 获取高清 Icon 图标
    fun MyViewModel.getHighResIcon(
        tool: Tool,
    ): android.graphics.drawable.Icon {
        // 优先从 drawable 中获取图标
        // 然后从 packageManager 中获取高清图标
        // 最后采用默认策略
        return getDrawableFromDrawable(tool.iconKey)?.toIcon(myContext)
            ?: getHighResDrawableFromPackageManager(
                packageName = tool.packageName,
                activityName = tool.activityName,
            )?.toIcon(myContext) ?: getIcon(tool)
    }


    // 获取圆形 Drawable 图标
    fun MyViewModel.getCircularDrawable(
        tool: Tool,
    ): Drawable {
        return getDrawable(tool).toCircularDrawable(myContext)
    }


    fun MyViewModel.getCircularDrawable(
        installedApp: InstalledApp,
    ): Drawable {
        return getDrawable(installedApp).toCircularDrawable(myContext)
    }


    // 将 Drawable 转换为圆形 Drawable，外部透明
    fun Drawable.toCircularDrawable(
        context: Context,
        backgroundColorResId: Int = android.R.color.transparent,
        targetSizeResId: Int = R.dimen.layout_size_1_large,
    ): Drawable {
        val targetSize = getLayoutSize(context, targetSizeResId)

        // 创建一个与目标大小相同的 Bitmap
        val bitmap = createBitmap(targetSize, targetSize)
        val canvas = Canvas(bitmap)

        // 获取背景颜色资源
        val backgroundColor = context.getColor(backgroundColorResId)

        // 设置背景颜色
        val backgroundPaint = android.graphics.Paint()
        backgroundPaint.color = backgroundColor
        canvas.drawRect(0f, 0f, targetSize.toFloat(), targetSize.toFloat(), backgroundPaint)

        // 缩放 Drawable 到目标大小
        val scaledDrawable = this.resize(context, targetSize, targetSize)

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
        val rect = RectF(0f, 0f, targetSize.toFloat(), targetSize.toFloat())
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
    val cornerRadius = getLayoutSize(context, cornerRadiusResId)

    // 将 dp 转换为 px
    val targetSizePx = getLayoutSize(context, targetSizeResId)

    // 创建一个与目标大小相同的 Bitmap
    val bitmap = createBitmap(targetSizePx, targetSizePx)
    val canvas = Canvas(bitmap)

    // 获取背景颜色资源
    val backgroundColor = context.getColor(backgroundColorResId)

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
    fun Drawable.resize(
        context: Context,
        width: Int,
        height: Int,
    ): Drawable {
        val resizedBitmap = this.toBitmap().scale(width, height, filter = true)
        return resizedBitmap.toDrawable(context.resources)
    }


    // 将 Drawable 转换为 Bitmap
    fun Drawable.toBitmap(): Bitmap {
        val width = if(this.intrinsicWidth > 0) {
            this.intrinsicWidth
        } else {
            ICON_SIZE
        }
        val height = if(this.intrinsicHeight > 0) {
            this.intrinsicHeight
        } else {
            ICON_SIZE
        }
        val bitmap = createBitmap(width, height)
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