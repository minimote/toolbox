/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

// 视图类型
object ViewType {
    // 编辑视图类型
    object Edit {
        const val PACKAGE_NAME = 0
        const val ACTIVITY_NAME = 1
        const val NICKNAME = 2
        const val SHOW_NAME = 3
        const val SIZE = 4
        const val DELETE = 5
    }

    // 我的视图类型
    object My {
        const val APP_INFO = 0
        const val CLEAR_CACHE = 1
        const val CLEAR_DATA = 2
        const val SUPPORT_AUTHOR = 3
        const val ABOUT_PROJECT = 4
        const val CHECK_UPDATE = 5
        const val SETTING = 6
        const val INSTRUCTION = 7
        const val UPDATE_LOG = 8
        const val PROBLEM_FEEDBACK = 9
    }

    // 支持作者视图类型
    object SupportAuthor {
        const val WELCOME = 0
        const val NOTICE = 1
        const val QR_ALIPAY = 2
        const val OPERATE_ALIPAY = 3
        const val QR_WECHAT = 4
        const val OPERATE_WECHAT = 5
    }

    // 关于项目视图类型
    object AboutProject {
        const val NOTICE = 0
        const val PROJECT_PATH_NAME = 1
        const val PROJECT_PATH_GITEE = 2
        const val PROJECT_PATH_GITHUB = 3
        const val LICENSE_TITLE = 4
        const val LICENSE = 5
    }

    // 设置视图类型
    object Setting {
        const val TITLE_CHECK_UPDATE = 0
        const val UPDATE_CHECK_FREQUENCY = 1
        const val TITLE_VIBRATION = 2
        const val VIBRATION_MODE = 3
        const val TITLE_NETWORK = 4
        const val NETWORK_ACCESS_MODE_MOBILE = 5
        const val NETWORK_ACCESS_MODE_WIFI = 6
        const val NETWORK_ACCESS_MODE_BLUETOOTH = 7
        const val NETWORK_ACCESS_MODE_OTHER = 8
        const val RESTORE_DEFAULT = 9
    }

}
