/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

// 视图类型
object ViewTypes {
    // 安装应用列表
    object InstalledAppList {
        const val APP = 0
        const val NO_RESULT = 1
        const val HISTORY_OR_SUGGESTION = 2
        const val SEARCH_TITLE = 3
    }

    // 编辑视图类型
    object Edit {
        const val PREVIEW = 0
        const val NICKNAME = 1
        const val DISPLAY_MODE = 2
        const val WIDTH = 3
        const val DELETE = 4
        const val ALIGNMENT = 5
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
        const val SCHEME_LIST = 10
        const val APP_DETAIL = 11
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


        const val TITLE_RESTORE = 9
        const val RESTORE_DEFAULT = 10


        const val TITLE_LAUNCH = 11
        const val EXIT_AFTER_LAUNCH = 12
        const val HOME_PAGE = 13


        const val TITLE_DISPLAY = 14
        const val SHOW_LIKE_ICON = 15
        const val SHOW_UNAVAILABLE_TOOLS = 16


        const val SEARCH_HISTORY_MAX_COUNT = 17
        const val SEARCH_SUGGESTION_MAX_COUNT = 18


        val titleViewSet by lazy {
            setOf(
                TITLE_CHECK_UPDATE,
                TITLE_VIBRATION,
                TITLE_NETWORK,
                TITLE_RESTORE,
                TITLE_LAUNCH,
                TITLE_DISPLAY,
            )
        }

        val switchViewSet by lazy {
            setOf(
                EXIT_AFTER_LAUNCH,
                SHOW_LIKE_ICON,
                SHOW_UNAVAILABLE_TOOLS,
            )
        }

        val radioViewSet by lazy {
            setOf(
                NETWORK_ACCESS_MODE_MOBILE,
                NETWORK_ACCESS_MODE_BLUETOOTH,
                NETWORK_ACCESS_MODE_WIFI,
                NETWORK_ACCESS_MODE_OTHER,
                VIBRATION_MODE,
                HOME_PAGE,
            )
        }

        val seekBarSet by lazy {
            setOf(
                SEARCH_HISTORY_MAX_COUNT,
                SEARCH_SUGGESTION_MAX_COUNT,
            )
        }
    }


    // 组件详情
    object WidgetDetail {

        const val SCHEME = 0

        const val DEFAULT_NAME = 1
        const val DESCRIPTION = 2
        const val WARNING_MESSAGE = 3

        const val PACKAGE_NAME = 4
        const val ACTIVITY_NAME = 5

        const val INTENT_ACTION = 6
        const val INTENT_CATEGORY = 7
        const val INTENT_FLAG = 8
        const val INTENT_EXTRAS = 9
        const val INTENT_URI = 10

        const val CREATED_TIME = 11
        const val LAST_MODIFIED_TIME = 12
        const val LAST_USED_TIME = 13
        const val USE_COUNT = 14

    }


    object ToolList {
        const val GROUP = 0
        const val CHILD = 1
        const val SEPARATOR = 2
        const val NO_RESULT = 3
        const val HISTORY_OR_SUGGESTION = 4
        const val SEARCH_TITLE = 5
    }

}
