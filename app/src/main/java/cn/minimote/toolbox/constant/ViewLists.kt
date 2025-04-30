/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

object ViewLists {
    // 编辑列表
//    val editViewTypes = EditViewTypes
    val editList = listOf(
        ViewTypes.Edit.PACKAGE_NAME, // 包名
        ViewTypes.Edit.ACTIVITY_NAME, // 活动名
        ViewTypes.Edit.NICKNAME, // 显示的名称
        ViewTypes.Edit.SHOW_NAME, // 是否显示名称
        ViewTypes.Edit.SIZE, // 组件大小
        ViewTypes.Edit.DELETE, // 删除组件
    )

    // 我的列表
//    private val myViewTypes = MyViewTypes
    val myList = listOf(
        ViewTypes.My.APP_INFO, // 应用信息
        ViewTypes.My.CLEAR_CACHE, // 清理缓存
        ViewTypes.My.CLEAR_DATA, // 清除数据
        ViewTypes.My.SETTING, // 设置
        ViewTypes.My.SUPPORT_AUTHOR, // 支持作者
        ViewTypes.My.ABOUT_PROJECT, // 关于项目
        ViewTypes.My.INSTRUCTION, // 使用说明
        ViewTypes.My.UPDATE_LOG, // 更新日志,
        ViewTypes.My.PROBLEM_FEEDBACK, // 问题反馈
        ViewTypes.My.CHECK_UPDATE, // 检查更新
    )

    // 支持作者
//    private val supportAuthorViewTypes = SupportAuthorViewTypes
    val supportAuthorViewList = listOf(
        ViewTypes.SupportAuthor.WELCOME, // 欢迎
        ViewTypes.SupportAuthor.NOTICE, // 说明
        ViewTypes.SupportAuthor.QR_ALIPAY, // 支付宝收款码
        ViewTypes.SupportAuthor.OPERATE_ALIPAY, // 支付宝相关操作
        ViewTypes.SupportAuthor.QR_WECHAT, // 微信收款码
        ViewTypes.SupportAuthor.OPERATE_WECHAT, // 微信相关操作
    )

    // 关于项目
    val aboutProjectViewList = listOf(
        ViewTypes.AboutProject.NOTICE, // 说明
        ViewTypes.AboutProject.PROJECT_PATH_NAME, // 项目地址
        ViewTypes.AboutProject.PROJECT_PATH_GITEE, // Gitee 地址
        ViewTypes.AboutProject.PROJECT_PATH_GITHUB, // Github 地址
    )

    // 设置
    val settingViewList = listOf(
        ViewTypes.Setting.TITLE_NETWORK, // 网络设置标题
        ViewTypes.Setting.NETWORK_ACCESS_MODE_MOBILE, // 移动网络
        ViewTypes.Setting.NETWORK_ACCESS_MODE_BLUETOOTH, // 蓝牙网络
        ViewTypes.Setting.NETWORK_ACCESS_MODE_WIFI, // WIFI网络
        ViewTypes.Setting.NETWORK_ACCESS_MODE_OTHER, // 其他网络
        ViewTypes.Setting.TITLE_CHECK_UPDATE, // 更新设置的标题
        ViewTypes.Setting.UPDATE_CHECK_FREQUENCY, // 更新检查频率
        ViewTypes.Setting.TITLE_VIBRATION, // 振动设置的标题
        ViewTypes.Setting.VIBRATION_MODE, // 振动模式
    )
}