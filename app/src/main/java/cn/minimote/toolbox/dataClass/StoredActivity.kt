/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.dataClass

import android.content.Intent
import cn.minimote.toolbox.constant.LaunchTypes.PACKAGE_AND_ACTIVITY
import java.util.UUID


// 用于存储的数据类(继承自工具类，因为比工具类属性多)
class StoredActivity(
    // 唯一标识符
    id: String = UUID.randomUUID().toString(),

    // 启动相关
    launchType: String = PACKAGE_AND_ACTIVITY,
    intentAction: String = Intent.ACTION_VIEW,
    intentCategory: String = Intent.CATEGORY_DEFAULT,
    intentFlag: Int = Intent.FLAG_ACTIVITY_NEW_TASK,
    intentExtras: Map<String, Any>? = null,
    intentUri: String? = null,

    appName: String,
    var nickName: String = appName, // 昵称
    packageName: String,
    activityName: String? = null,

    // 图标的标识符
    iconKey: String = activityName ?: intentUri ?: id,
    var width: Int, // 宽度
    var height: Int = 1, // 高度
    var showName: Boolean = true, // 是否显示名称
    var showIcon: Boolean = true, // 是否显示图标
    description: String = "", // 描述

    // 创建时间
    val createTime: Long = System.currentTimeMillis(),
    // 最后一次修改时间
    var modifyTime: Long = System.currentTimeMillis(),
    // 最后一次启动时间
    var lastLaunchTime: Long = -1,
    // 启动次数
    var launchCount: Int = 0,
) : ToolActivity(
    id,
    launchType,
    intentAction,
    intentCategory,
    intentFlag,
    intentExtras,
    intentUri,
    appName,
    packageName,
    activityName,
    iconKey,
    description,
) {
    // 用一个新的 StoredActivity 的属性覆盖原有的属性
    fun update(storedActivity: StoredActivity) {
        this.nickName = storedActivity.nickName
        this.iconKey = storedActivity.iconKey
        this.width = storedActivity.width
        this.height = storedActivity.height
        this.showName = storedActivity.showName
        this.showIcon = storedActivity.showIcon
        this.description = storedActivity.description
        this.modifyTime = storedActivity.modifyTime
        this.lastLaunchTime = storedActivity.lastLaunchTime
        this.launchCount = storedActivity.launchCount
    }

    fun copy(): StoredActivity {
    return StoredActivity(
        id = this.id,
        launchType = this.launchType,
        intentAction = this.intentAction,
        intentCategory = this.intentCategory,
        intentFlag = this.intentFlag,
        intentExtras = this.intentExtras?.toMap(), // 复制为新 Map 避免引用共享
        intentUri = this.intentUri,
        appName = this.appName,
        nickName = this.nickName,
        packageName = this.packageName,
        activityName = this.activityName,
        iconKey = this.iconKey,
        width = this.width,
        height = this.height,
        showName = this.showName,
        showIcon = this.showIcon,
        description = this.description,
        createTime = this.createTime,
        modifyTime = this.modifyTime,
        lastLaunchTime = this.lastLaunchTime,
        launchCount = this.launchCount
    )
}

}
