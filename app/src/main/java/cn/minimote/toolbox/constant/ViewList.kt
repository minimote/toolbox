/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

object ViewList {
    // 编辑列表
    val editList by lazy {
        listOf(
            ViewTypes.Edit.PREVIEW, // 预览

            ViewTypes.Edit.NICKNAME, // 昵称
            ViewTypes.Edit.DISPLAY_MODE, // 显示方式
            ViewTypes.Edit.WIDTH, // 组件宽度
            ViewTypes.Edit.ALIGNMENT, // 对齐方式

//            ViewTypes.Edit.DELETE, // 删除组件
        )
    }


    // 我的列表
    val myList by lazy {
        listOf(
            ViewTypes.My.APP_INFO, // 应用信息
            ViewTypes.My.CLEAR_CACHE, // 清理缓存
            ViewTypes.My.CLEAR_DATA, // 清除数据
            ViewTypes.My.APP_DETAIL, // 应用详情

            ViewTypes.My.SETTING, // 设置
            ViewTypes.My.BACKUP_AND_RESTORE, // 备份与恢复
            ViewTypes.My.LONG_PRESS_MENU, // 长按菜单
//            ViewTypes.My.SCHEME_LIST, // Scheme 列表
            ViewTypes.My.INSTRUCTION, // 使用说明
            ViewTypes.My.PROBLEM_FEEDBACK, // 问题反馈

            ViewTypes.My.UPDATE_LOG, // 更新日志
            ViewTypes.My.ABOUT_PROJECT, // 关于项目
            ViewTypes.My.SUPPORT_AUTHOR, // 支持作者

            ViewTypes.My.CHECK_UPDATE, // 检查更新
        )
    }


    // 支持作者
    val supportAuthorViewList by lazy {
        listOf(
            ViewTypes.SupportAuthor.WELCOME, // 欢迎
            ViewTypes.SupportAuthor.NOTICE, // 说明
            ViewTypes.SupportAuthor.QR_ALIPAY, // 支付宝收款码
            ViewTypes.SupportAuthor.OPERATE_ALIPAY, // 支付宝相关操作
            ViewTypes.SupportAuthor.QR_WECHAT, // 微信收款码
            ViewTypes.SupportAuthor.OPERATE_WECHAT, // 微信相关操作
        )
    }


    // 关于项目
    val aboutProjectViewList by lazy {
        listOf(
            ViewTypes.AboutProject.NOTICE_TITLE, // 说明的标题
            ViewTypes.AboutProject.NOTICE, // 说明
            ViewTypes.AboutProject.PROJECT_PATH_TITLE, // 项目的地址
            ViewTypes.AboutProject.PROJECT_PATH_GITEE, // Gitee 地址
            ViewTypes.AboutProject.PROJECT_PATH_GITHUB, // Github 地址
            ViewTypes.AboutProject.LICENSE_TITLE, // 开源许可的标题
            ViewTypes.AboutProject.LICENSE, // 开放源代码许可
        )
    }


    // 设置
    val settingViewList by lazy {
        listOf(
            ViewTypes.Setting.TITLE_DISPLAY, // 显示设置标题
            ViewTypes.Setting.SHOW_UNAVAILABLE_TOOLS, // 显示不可用工具
            ViewTypes.Setting.SHOW_LIKE_ICON, // 显示收藏标志
            ViewTypes.Setting.SEARCH_HISTORY_MAX_COUNT, // 搜索历史最大数量
            ViewTypes.Setting.SEARCH_SUGGESTION_MAX_COUNT, // 搜索建议最大数量

            ViewTypes.Setting.TITLE_LAUNCH, // 启动设置标题
            ViewTypes.Setting.EXIT_AFTER_LAUNCH, // 启动后退出
            ViewTypes.Setting.HOME_PAGE, // 启动的主页

            ViewTypes.Setting.TITLE_VIBRATION, // 振动设置的标题
            ViewTypes.Setting.VIBRATION_MODE, // 振动模式

            ViewTypes.Setting.TITLE_NETWORK, // 网络设置标题
            ViewTypes.Setting.NETWORK_ACCESS_MODE_MOBILE, // 移动网络
            ViewTypes.Setting.NETWORK_ACCESS_MODE_BLUETOOTH, // 蓝牙网络
            ViewTypes.Setting.NETWORK_ACCESS_MODE_WIFI, // WIFI网络
            ViewTypes.Setting.NETWORK_ACCESS_MODE_OTHER, // 其他网络

            ViewTypes.Setting.TITLE_CHECK_UPDATE, // 更新设置的标题
            ViewTypes.Setting.CHECK_UPDATE_IGNORE_NETWORK_RESTRICTIONS, // 检查更新时忽略网络限制
            ViewTypes.Setting.UPDATE_CHECK_FREQUENCY, // 更新检查频率

//            ViewTypes.Setting.TITLE_RESTORE, // 恢复设置的标题(只是要个分隔线)
//            ViewTypes.Setting.RESTORE_DEFAULT, // 恢复默认
//            ViewTypes.Setting.TITLE_RESTORE, // 恢复设置的标题(只是要个分隔线)
        )
    }


    val widgetDetailList by lazy {
        listOf(
            ViewTypes.WidgetDetail.DEFAULT_NAME, // 默认名称

            ViewTypes.WidgetDetail.SCHEME, // Scheme

            ViewTypes.WidgetDetail.DESCRIPTION, // 描述
            ViewTypes.WidgetDetail.WARNING_MESSAGE, // 警告信息

            ViewTypes.WidgetDetail.PACKAGE_NAME, // 包名
            ViewTypes.WidgetDetail.ACTIVITY_NAME, // 活动名

            ViewTypes.WidgetDetail.INTENT_ACTION,
            ViewTypes.WidgetDetail.INTENT_CATEGORY,
            ViewTypes.WidgetDetail.INTENT_FLAG,
            ViewTypes.WidgetDetail.INTENT_EXTRAS,
            ViewTypes.WidgetDetail.INTENT_URI,

            ViewTypes.WidgetDetail.CREATED_TIME, // 创建时间
            ViewTypes.WidgetDetail.LAST_MODIFIED_TIME, // 最后修改时间
            ViewTypes.WidgetDetail.LAST_USED_TIME, // 最后使用时间
            ViewTypes.WidgetDetail.USE_COUNT, // 使用次数
        )
    }


    val onlyInWidget by lazy {
        setOf(
            ViewTypes.WidgetDetail.CREATED_TIME, // 创建时间
            ViewTypes.WidgetDetail.LAST_MODIFIED_TIME, // 最后修改时间
            ViewTypes.WidgetDetail.LAST_USED_TIME, // 最后使用时间
            ViewTypes.WidgetDetail.USE_COUNT, // 使用次数
        )
    }


    val toolDetailList by lazy {
        widgetDetailList.filter {
            it !in onlyInWidget
        }
    }


    val backupAndRecoveryViewList by lazy {
        listOf(
            ViewTypes.BackupAndRecovery.COLLECTION_TITLE, // 收藏管理
            ViewTypes.BackupAndRecovery.COLLECTION_IMPORT, // 导入收藏
            ViewTypes.BackupAndRecovery.COLLECTION_EXPORT, // 导出收藏
            ViewTypes.BackupAndRecovery.COLLECTION_CLEAR, // 清空收藏

            ViewTypes.BackupAndRecovery.SETTING_TITLE, // 设置管理
            ViewTypes.BackupAndRecovery.SETTING_IMPORT, // 导入设置
            ViewTypes.BackupAndRecovery.SETTING_EXPORT, // 导出设置
            ViewTypes.BackupAndRecovery.SETTING_RECOMMEND, // 推荐设置
            ViewTypes.BackupAndRecovery.SETTING_RESTORE, // 恢复设置
        )
    }

}