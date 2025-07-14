/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.dataClass


data class InstalledApp(
    val name: String,
    val packageName: String,
    val activityName: String,
    var iconKey: String = activityName,
    val id: String = activityName,
) {
    // 将安装类型的活动转换为存储类型
    fun toStoredTool(
        width: Int,
    ): StoredTool {
        return StoredTool(
            name = name,
            packageName = packageName,
            activityName = activityName,
            width = width,
            iconKey = iconKey,
            id = id,
        )
    }

}


