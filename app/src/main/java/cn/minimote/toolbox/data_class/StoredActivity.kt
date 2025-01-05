/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.data_class


// 用于存储的数据类
data class StoredActivity(
    val appName: String,
    val packageName: String,
    val activityName: String,
    // 在主页显示的昵称
    var nickName: String = appName,
    // 1 表示整行，2 表示半行，3 表示三分之一行
    val widgetType: Int = 1,
    // 整行默认显示名称
    var showName: Boolean = (widgetType == 1),
)