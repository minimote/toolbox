/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

object ViewList {
    // 编辑列表
    val editList by lazy {
        listOf(
            ViewType.Edit.PACKAGE_NAME, // 包名
            ViewType.Edit.ACTIVITY_NAME, // 活动名
            ViewType.Edit.NICKNAME, // 显示的名称
            ViewType.Edit.SHOW_NAME, // 是否显示名称
            ViewType.Edit.SIZE, // 组件大小
            ViewType.Edit.DELETE, // 删除组件
        )
    }

    // 我的列表
    val myList by lazy {
        listOf(
            ViewType.My.APP_INFO, // 应用信息
            ViewType.My.CLEAR_CACHE, // 清理缓存
            ViewType.My.CLEAR_DATA, // 清除数据
            ViewType.My.SETTING, // 设置
            ViewType.My.SUPPORT_AUTHOR, // 支持作者
            ViewType.My.ABOUT_PROJECT, // 关于项目
            ViewType.My.INSTRUCTION, // 使用说明
            ViewType.My.UPDATE_LOG, // 更新日志,
            ViewType.My.PROBLEM_FEEDBACK, // 问题反馈
            ViewType.My.CHECK_UPDATE, // 检查更新
        )
    }

    // 支持作者
    val supportAuthorViewList by lazy {
        listOf(
            ViewType.SupportAuthor.WELCOME, // 欢迎
            ViewType.SupportAuthor.NOTICE, // 说明
            ViewType.SupportAuthor.QR_ALIPAY, // 支付宝收款码
            ViewType.SupportAuthor.OPERATE_ALIPAY, // 支付宝相关操作
            ViewType.SupportAuthor.QR_WECHAT, // 微信收款码
            ViewType.SupportAuthor.OPERATE_WECHAT, // 微信相关操作
        )
    }

    // 关于项目
    val aboutProjectViewList by lazy {
        listOf(
            ViewType.AboutProject.NOTICE, // 说明
            ViewType.AboutProject.PROJECT_PATH_NAME, // 项目地址
            ViewType.AboutProject.PROJECT_PATH_GITEE, // Gitee 地址
            ViewType.AboutProject.PROJECT_PATH_GITHUB, // Github 地址
            ViewType.AboutProject.LICENSE_TITLE, // 开源许可标题
            ViewType.AboutProject.LICENSE, // 开放源代码许可
        )
    }

    // 设置
    val settingViewList by lazy {
        listOf(
            ViewType.Setting.TITLE_NETWORK, // 网络设置标题
            ViewType.Setting.NETWORK_ACCESS_MODE_MOBILE, // 移动网络
            ViewType.Setting.NETWORK_ACCESS_MODE_BLUETOOTH, // 蓝牙网络
            ViewType.Setting.NETWORK_ACCESS_MODE_WIFI, // WIFI网络
            ViewType.Setting.NETWORK_ACCESS_MODE_OTHER, // 其他网络
            ViewType.Setting.TITLE_CHECK_UPDATE, // 更新设置的标题
            ViewType.Setting.UPDATE_CHECK_FREQUENCY, // 更新检查频率
            ViewType.Setting.TITLE_VIBRATION, // 振动设置的标题
            ViewType.Setting.VIBRATION_MODE, // 振动模式
//        ViewTypes.Setting.RESTORE_DEFAULT, // 恢复默认
        )
    }

}
