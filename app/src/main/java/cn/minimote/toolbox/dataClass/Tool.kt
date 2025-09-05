/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.dataClass

import android.content.Intent
import cn.minimote.toolbox.constant.IntentType.PACKAGE_AND_ACTIVITY
import cn.minimote.toolbox.constant.ToolConstants


// 工具类
open class Tool(
    // 唯一标识符
    val id: String,

    // 启动相关
    val intentType: String = PACKAGE_AND_ACTIVITY,
    val intentAction: String = Intent.ACTION_VIEW,
    val intentCategory: String = Intent.CATEGORY_DEFAULT,
    val intentFlag: Int = Intent.FLAG_ACTIVITY_NEW_TASK,
    val intentExtras: Map<String, Any>? = null,
    val intentUri: String? = null,

    val name: String,
    var nickname: String = name, // 昵称
    val packageName: String,
    val activityName: String? = null,

    // 图标的标识符
    var iconKey: String = activityName ?: intentUri ?: id,
//    var width: Int, // 宽度
//    var height: Int = 1, // 高度
//    var showName: Boolean = true, // 是否显示名称
//    var showIcon: Boolean = true, // 是否显示图标
    val description: String? = null, // 描述
    val warningMessage: String? = null, // 警告信息

//    // 创建时间
//    val createTime: Long = System.currentTimeMillis(),
//    // 最后一次修改时间
//    var modifyTime: Long = System.currentTimeMillis(),
//    // 最后一次启动时间
//    var lastLaunchTime: Long = -1,
//    // 启动次数
//    var launchCount: Int = 0,
) {

    // 转换为存储类型
    fun toStoredTool(
        width: Int = ToolConstants.MAX_WIDGET_WIDTH,
    ): StoredTool {
        return StoredTool(
            id = id,
            name = name,
            packageName = packageName,
            activityName = activityName,
            iconKey = iconKey,
            description = description,
            warningMessage = warningMessage,
            intentType = intentType,
            intentAction = intentAction,
            intentCategory = intentCategory,
            intentFlag = intentFlag,
            intentExtras = intentExtras,
            intentUri = intentUri,
            width = width,
        )
    }

}
