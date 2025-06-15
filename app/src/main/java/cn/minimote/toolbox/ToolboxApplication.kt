/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox

import android.app.Application
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class ToolboxApplication : Application(){
    companion object {
        init {
            // SmartRefreshLayout 全局默认配置（优先级最低，会被其他设置覆盖）
            SmartRefreshLayout.setDefaultRefreshInitializer { context, layout ->
                // 开始设置全局的基本参数（可以被下面的 DefaultRefreshHeaderCreator 覆盖）
                layout.setReboundDuration(500);//回弹动画时长（毫秒）
                layout.setEnableLoadMore(true) //是否启用上拉加载功能
                layout.setEnablePureScrollMode(true) //是否启用纯滚动模式
            }

        }
    }
}