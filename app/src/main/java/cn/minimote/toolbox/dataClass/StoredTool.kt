/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.dataClass

import android.content.Intent
import cn.minimote.toolbox.constant.LaunchType.PACKAGE_AND_ACTIVITY
import cn.minimote.toolbox.constant.StoredTool.DisplayMode.ICON_AND_NAME


// 用于存储的数据类(继承自工具类，因为比工具类属性多)
class StoredTool(
    // 唯一标识符
    id: String,

    // 启动相关
    intentType: String = PACKAGE_AND_ACTIVITY,
    intentAction: String = Intent.ACTION_VIEW,
    intentCategory: String = Intent.CATEGORY_DEFAULT,
    intentFlag: Int = Intent.FLAG_ACTIVITY_NEW_TASK,
    intentExtras: Map<String, Any>? = null,
    intentUri: String? = null,

    name: String,
    nickName: String = name, // 昵称
    packageName: String,
    activityName: String? = null,

    // 图标的标识符
    iconKey: String = activityName ?: intentUri ?: id,
    var width: Int, // 宽度
    var height: Int = 1, // 高度
    var displayMode: Int = ICON_AND_NAME,
    description: String = "", // 描述

    // 创建时间
    val createdTime: Long = System.currentTimeMillis(),
    // 最后一次修改时间
    var lastModifiedTime: Long = -1,
    // 最后一次使用时间
    var lastUsedTime: Long = -1,
    // 使用次数
    var useCount: ULong = 0u,
) : Tool(
    id = id,
    intentType = intentType,
    intentAction = intentAction,
    intentCategory = intentCategory,
    intentFlag = intentFlag,
    intentExtras = intentExtras,
    intentUri = intentUri,
    name = name,
    nickname = nickName,
    packageName = packageName,
    activityName = activityName,
    iconKey = iconKey,
    description = description,
) {
    // 用一个新的 StoredActivity 的属性覆盖原有的属性
    fun update(storedTool: StoredTool) {
        this.nickname = storedTool.nickname
        this.iconKey = storedTool.iconKey
        this.width = storedTool.width
        this.height = storedTool.height
        this.displayMode = storedTool.displayMode
        this.description = storedTool.description
        this.lastModifiedTime = storedTool.lastModifiedTime
        this.lastUsedTime = storedTool.lastUsedTime
        this.useCount = storedTool.useCount
    }

    fun copy(): StoredTool {
        return StoredTool(
            id = this.id,
            intentType = this.intentType,
            intentAction = this.intentAction,
            intentCategory = this.intentCategory,
            intentFlag = this.intentFlag,
            intentExtras = this.intentExtras?.toMap(), // 复制为新 Map 避免引用共享
            intentUri = this.intentUri,
            name = this.name,
            nickName = this.nickname,
            packageName = this.packageName,
            activityName = this.activityName,
            iconKey = this.iconKey,
            width = this.width,
            height = this.height,
            displayMode = this.displayMode,
            description = this.description,
            createdTime = this.createdTime,
            lastModifiedTime = this.lastModifiedTime,
            lastUsedTime = this.lastUsedTime,
            useCount = this.useCount,
        )
    }

}
