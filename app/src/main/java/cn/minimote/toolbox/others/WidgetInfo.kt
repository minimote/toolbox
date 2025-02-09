package cn.minimote.toolbox.others///*
// * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
// * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
// */
//
//package cn.minimote.toolbox
//
//import android.graphics.drawable.Drawable
//import cn.minimote.toolbox.others.StorageAppInfo
//
//
//data class WidgetInfo(
//    val appIcon: Drawable? = null,
//    val appName: String,
//    val packageName: String,
//    val activityName: String,
//    var nickName: String = appName,
//    // 1 表示整行，2 表示半行，3 表示三分之一行
//    val widgetSize: Int,
//    // 整行默认显示名称
//    var showName: Boolean = (widgetType == 1),
////    var position: Int,
//)
//
//// 将存储类型转换为小组件类型
//fun StorageAppInfoToWidgetInfo(storageAppInfo: StorageAppInfo, appIcon: Drawable?): WidgetInfo {
//    return WidgetInfo(
//        appIcon = appIcon,
//        appName = storageAppInfo.appName,
//        packageName = storageAppInfo.packageName,
//        activityName = storageAppInfo.activityName,
//        nickName = storageAppInfo.nickName,
//        widgetSize = storageAppInfo.widgetSize,
////        position = storageAppInfo.position,
//    )
//}