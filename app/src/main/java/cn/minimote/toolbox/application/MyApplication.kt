/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.application

import android.app.Application
import androidx.core.graphics.drawable.toDrawable
import cn.minimote.toolbox.R
import cn.minimote.toolbox.helper.PathHelper
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.asImage
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.request.transitionFactory
import coil3.transition.CrossfadeTransition
import com.scwang.smart.refresh.layout.SmartRefreshLayout


class MyApplication : Application(), SingletonImageLoader.Factory {
    companion object {
        init {
            // SmartRefreshLayout 全局默认配置（优先级最低，会被其他设置覆盖）
            SmartRefreshLayout.setDefaultRefreshInitializer { _, layout ->
                // 开始设置全局的基本参数（可以被下面的 DefaultRefreshHeaderCreator 覆盖）
                layout.setReboundDuration(500)//回弹动画时长（毫秒）
                layout.setEnablePureScrollMode(true) //是否启用纯滚动模式
            }
        }
    }


    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(this)
//            .memoryCache {
//                MemoryCache.Builder(this)
//                    .maxSizePercent(0.25)  // 默认内存缓存占应用内存的25%
//                    .build()
//            }
            .diskCache {
                DiskCache.Builder()
                    .directory(PathHelper.getIconCachePath(this))  // 默认缓存目录
//                    .maxSizePercent(0.02)  // 默认磁盘缓存占可用空间的2%
                    .build()
            }
            // 设置加载占位符
            .placeholder(R.color.black.toDrawable().asImage())
            // 设置加载错误占位符
            .error(R.color.black.toDrawable().asImage())
//            .crossfade(true)  // 启用交叉淡入效果
            .transitionFactory(CrossfadeTransition.Factory(
                durationMillis = 100,
//                preferExactIntrinsicSize = true,
            )) // 使用交叉淡入
            // 或
//            .transitionFactory(NoneTransition.Factory()) // 不使用过渡效果
            .build()
    }
}