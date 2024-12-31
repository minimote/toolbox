/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox

import android.graphics.drawable.Drawable
import kotlin.random.Random


data class WidgetInfo(
    val appIcon: Drawable? = null,
    val appName: String,
    val packageName: String,
    val activityName: String,
    var nickName: String = appName,
    // 1 表示满的，2 表示一半，3 表示三分之一
    val widgetType: Int = 1,
    var position: Int = Int.MAX_VALUE,
)

//// 将存储类型转换为小组件类型
//fun StorageAppInfoToWidgetInfo(storageAppInfo: StorageAppInfo, appIcon: Drawable?): WidgetInfo {
//    return WidgetInfo(
//        appIcon = appIcon,
//        appName = storageAppInfo.appName,
//        packageName = storageAppInfo.packageName,
//        activityName = storageAppInfo.activityName,
//        nickName = storageAppInfo.nickName,
//        widgetSize = storageAppInfo.widgetSize,
//        position = storageAppInfo.position,
//    )
//}