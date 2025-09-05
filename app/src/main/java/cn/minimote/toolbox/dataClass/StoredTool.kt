/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.dataClass

import android.content.Intent
import cn.minimote.toolbox.constant.IntentType.PACKAGE_AND_ACTIVITY
import cn.minimote.toolbox.constant.ToolConstants


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
    var width: Int = ToolConstants.MAX_WIDGET_WIDTH, // 宽度
    var height: Int = 1, // 高度
    var displayMode: String = ToolConstants.DisplayMode.String.ICON_AND_NAME,
    var alignment: String = ToolConstants.Alignment.CENTER,

    description: String? = null, // 描述
    warningMessage: String? = null, // 警告信息

    // 创建时间
    val createdTime: Long = System.currentTimeMillis(),
    // 最后一次修改时间
    var lastModifiedTime: Long = -1,
    // 最后一次使用时间
    var lastUsedTime: Long = -1,
    // 使用次数
    var useCount: Int = 0,
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
    warningMessage = warningMessage,
) {
    // 用一个新的 StoredActivity 的属性覆盖原有的属性
    fun update(storedTool: StoredTool) {
        this.nickname = storedTool.nickname
        this.iconKey = storedTool.iconKey
        this.width = storedTool.width
        this.height = storedTool.height
        this.displayMode = storedTool.displayMode
        this.alignment = storedTool.alignment
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
            alignment = this.alignment,
            description = this.description,
            warningMessage = this.warningMessage,
            createdTime = this.createdTime,
            lastModifiedTime = this.lastModifiedTime,
            lastUsedTime = this.lastUsedTime,
            useCount = this.useCount,
        )
    }

    companion object {
        fun getFlagNameList(flags: Int): List<String> {
            val flagNames = mutableListOf<String>()

            // 只检查Activity相关的flag
            if(flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0) {
                flagNames.add("FLAG_ACTIVITY_NEW_TASK")
            }
            if(flags and Intent.FLAG_ACTIVITY_CLEAR_TOP != 0) {
                flagNames.add("FLAG_ACTIVITY_CLEAR_TOP")
            }
            if(flags and Intent.FLAG_ACTIVITY_SINGLE_TOP != 0) {
                flagNames.add("FLAG_ACTIVITY_SINGLE_TOP")
            }
            if(flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0) {
                flagNames.add("FLAG_ACTIVITY_BROUGHT_TO_FRONT")
            }
            if(flags and Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS != 0) {
                flagNames.add("FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS")
            }
            if(flags and Intent.FLAG_ACTIVITY_FORWARD_RESULT != 0) {
                flagNames.add("FLAG_ACTIVITY_FORWARD_RESULT")
            }
            if(flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0) {
                flagNames.add("FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY")
            }
            if(flags and Intent.FLAG_ACTIVITY_MULTIPLE_TASK != 0) {
                flagNames.add("FLAG_ACTIVITY_MULTIPLE_TASK")
            }
            if(flags and Intent.FLAG_ACTIVITY_NO_ANIMATION != 0) {
                flagNames.add("FLAG_ACTIVITY_NO_ANIMATION")
            }
            if(flags and Intent.FLAG_ACTIVITY_NO_HISTORY != 0) {
                flagNames.add("FLAG_ACTIVITY_NO_HISTORY")
            }
            if(flags and Intent.FLAG_ACTIVITY_NO_USER_ACTION != 0) {
                flagNames.add("FLAG_ACTIVITY_NO_USER_ACTION")
            }
            if(flags and Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP != 0) {
                flagNames.add("FLAG_ACTIVITY_PREVIOUS_IS_TOP")
            }
            if(flags and Intent.FLAG_ACTIVITY_REORDER_TO_FRONT != 0) {
                flagNames.add("FLAG_ACTIVITY_REORDER_TO_FRONT")
            }
            if(flags and Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED != 0) {
                flagNames.add("FLAG_ACTIVITY_RESET_TASK_IF_NEEDED")
            }
            if(flags and Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS != 0) {
                flagNames.add("FLAG_ACTIVITY_RETAIN_IN_RECENTS")
            }
            if(flags and Intent.FLAG_ACTIVITY_TASK_ON_HOME != 0) {
                flagNames.add("FLAG_ACTIVITY_TASK_ON_HOME")
            }

            // 一些通用flags，也可能在Activity中使用
            if(flags and Intent.FLAG_DEBUG_LOG_RESOLUTION != 0) {
                flagNames.add("FLAG_DEBUG_LOG_RESOLUTION")
            }
            if(flags and Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION != 0) {
                flagNames.add("FLAG_GRANT_PERSISTABLE_URI_PERMISSION")
            }
            if(flags and Intent.FLAG_GRANT_PREFIX_URI_PERMISSION != 0) {
                flagNames.add("FLAG_GRANT_PREFIX_URI_PERMISSION")
            }
            if(flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0) {
                flagNames.add("FLAG_GRANT_READ_URI_PERMISSION")
            }
            if(flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION != 0) {
                flagNames.add("FLAG_GRANT_WRITE_URI_PERMISSION")
            }

            return flagNames
        }
    }
}
