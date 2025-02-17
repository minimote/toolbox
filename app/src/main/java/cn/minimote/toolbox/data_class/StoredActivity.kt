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
    var iconName: String = activityName,
    // 在主页显示的昵称
    var nickName: String = appName,
    // 列数
    var widgetSize: Int,
    // 整行默认显示名称
    var showName: Boolean = true,
) {
    // 用一个新的 StoredActivity 的属性覆盖原有的属性
    fun update(storedActivity: StoredActivity) {
        this.iconName = storedActivity.iconName
        this.nickName = storedActivity.nickName
        this.widgetSize = storedActivity.widgetSize
        this.showName = storedActivity.showName
    }
}
