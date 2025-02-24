/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.dataClass


// 用于存储的数据类
data class InstalledActivity(
    val appName: String,
    val packageName: String,
    val activityName: String,
    var iconName: String = activityName,
)