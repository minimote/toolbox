/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox

import android.graphics.drawable.Drawable
import kotlin.random.Random

// 用于显示的数据类
data class DisplayAppInfo(
    val appName: String,
    val packageName: String,
    val activityName: String,
    val appIcon: Drawable? = null,
    var isSwitchOn: Boolean = false,
)

// 用于存储的数据类
data class StorageAppInfo(
    val appName: String,
    val packageName: String,
    val activityName: String,
    var nickName: String = appName,
    // 1 表示满的，2 表示一半，3 表示三分之一
    val widgetType: Int,
    var position: Int,
) {
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as StorageAppInfo
//
//        return activityName == other.activityName
//    }
//
//    override fun hashCode(): Int {
//        return activityName.hashCode()
//    }

//    // 深拷贝
//    fun deepCopy(): StorageAppInfo {
//        return StorageAppInfo(
//            appName = this.appName,
//            packageName = this.packageName,
//            activityName = this.activityName,
//            nickName = this.nickName,
//            widgetSize = this.widgetSize,
//            position = this.position
//        )
//    }
}


// 将显示类型转换为存储类型
fun displayAppInfoToStorageAppInfo(
    displayAppInfo: DisplayAppInfo,
    widgetType: Int = 1,
    position: Int = Int.MAX_VALUE,
): StorageAppInfo {
    return StorageAppInfo(
        appName = displayAppInfo.appName,
        packageName = displayAppInfo.packageName,
        activityName = displayAppInfo.activityName,
        widgetType = widgetType,
        position = position,
    )
}
