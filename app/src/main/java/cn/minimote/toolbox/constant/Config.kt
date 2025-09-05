/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.FragmentName.MY_LIST_FRAGMENT
import cn.minimote.toolbox.constant.FragmentName.TOOL_LIST_FRAGMENT
import cn.minimote.toolbox.constant.FragmentName.WIDGET_LIST_FRAGMENT
import org.json.JSONObject
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets


object Config {

    // 编码
    val ENCODING: Charset = StandardCharsets.UTF_8

    // 配置的文件名
    object ConfigFileName {
        //        const val DEFAULT_CONFIG = "default_config.json"
        const val USER_CONFIG = "user_config.json"
    }

    // 配置的键
    object ConfigKeys {
//        // 版本号
//        const val VERSION = "version"

        object CheckUpdate {
            // 上次检查更新时间
            const val LAST_CHECK_UPDATE_TIME = "last_check_update_time"
            // 更新检查频率
            const val CHECK_UPDATE_FREQUENCY = "check_update_frequency"
        }

        // 振动模式
        const val VIBRATION_MODE = "vibration_mode"

        // 网络访问模式
        object NetworkAccessModeKeys {
            const val MOBILE = "network_access_mode_mobile"
            const val BLUETOOTH = "network_access_mode_bluetooth"
            const val WIFI = "network_access_mode_wifi"
            const val OTHER = "network_access_mode_other"
        }

        // downloadId
        const val DOWNLOAD_ID = "download_id"


        // 保存按钮的位置
        const val SAVE_BUTTON_POSITION = "save_button_position"


        // 全部工具页面的列数
//        const val TOOL_LIST_COLUMN_COUNT = "tool_list_column_count"
//        // Scheme 页面的列数
//        const val SCHEME_LIST_COLUMN_COUNT = "scheme_list_column_count"


        object Launch {
            // 打开工具后退出本软件
            const val EXIT_AFTER_LAUNCH = "exit_after_launch"
            // 软件启动的首页
            const val HOME_PAGE = "home_page"
        }


        object Display {
            // 显示收藏标志
            const val SHOW_LIKE_ICON = "show_like_icon"
            // 显示不可用的工具
            const val SHOW_UNAVAILABLE_TOOLS = "show_unavailable_tools"
        }


        object CollapsedGroups {
            const val PREFIX = "collapsed_groups_"

            const val TOOL_LIST = PREFIX + "tool_list"
            const val SCHEME_LIST = PREFIX + "scheme_list"
        }


        object SearchHistory {
            const val PREFIX = "search_history_"
            const val MAX_COUNT = PREFIX + "max_count"
            const val TOOL_LIST = PREFIX + "tool_list"
            const val SCHEME_LIST = PREFIX + "scheme_list"
            const val INSTALLED_APP_LIST = PREFIX + "installed_app_list"
        }


        // 搜索建议
        object SearchSuggestion {
            const val PREFIX = "search_suggestion_"
            const val MAX_COUNT = PREFIX + "max_count"
            const val TOOL_LIST = PREFIX + "tool_list"
            const val SCHEME_LIST = PREFIX + "scheme_list"
            const val INSTALLED_APP_LIST = PREFIX + "installed_app_list"
        }
    }


    // 配置的值
    object ConfigValues {
        // 振动模式：自动、启用、关闭
        object VibrationMode {
            const val AUTO = "auto"
            const val ON = "on"
            const val OFF = "off"

            val idToStringIdMap by lazy {
                linkedMapOf(
                    ON to R.string.vibration_mode_on,
                    AUTO to R.string.vibration_mode_auto,
                    OFF to R.string.vibration_mode_off,
                )
            }
        }

        // 更新检查频率：每天、每周、每月、从不
        object CheckUpdateFrequency {
            const val DAILY = "daily"
            const val WEEKLY = "weekly"
            const val MONTHLY = "monthly"
            const val NEVER = "never"

            val idToStringIdMap by lazy {
                linkedMapOf(
                    DAILY to R.string.check_update_frequency_daily,
                    WEEKLY to R.string.check_update_frequency_weekly,
                    MONTHLY to R.string.check_update_frequency_monthly,
                    NEVER to R.string.check_update_frequency_never,
                )
            }
        }

        // 网络访问模式
        object NetworkAccessModeValues {
            const val ALLOW = "allow"  // 允许
            const val DENY = "deny"    // 拒绝
            const val ALERT = "alert"  // 提示

            val idToStringIdMap by lazy {
                linkedMapOf(
                    ALLOW to R.string.network_access_mode_allow,
                    ALERT to R.string.network_access_mode_alert,
                    DENY to R.string.network_access_mode_deny,
                )
            }
        }


        // 软件启动首页
        object HomePage {
            val idToStringIdMap by lazy {
                linkedMapOf(
                    TOOL_LIST_FRAGMENT to R.string.fragment_name_tool_list,
                    WIDGET_LIST_FRAGMENT to R.string.fragment_name_widget_list,
                    MY_LIST_FRAGMENT to R.string.fragment_name_my_list,
                )
            }
        }
    }


    // 默认配置
    val defaultConfig by lazy {
        mapOf(
//            // 版本
//            ConfigKeys.VERSION to Version.CONFIG,

            // 上次更新时间
            ConfigKeys.CheckUpdate.LAST_CHECK_UPDATE_TIME to 0,
            // 更新检查频率：daily, weekly, monthly, never
            ConfigKeys.CheckUpdate.CHECK_UPDATE_FREQUENCY to ConfigValues.CheckUpdateFrequency.DAILY,

            // 振动模式：auto, on, off
            ConfigKeys.VIBRATION_MODE to ConfigValues.VibrationMode.AUTO,

            // 网络访问模式-数据：allow, deny, alert
            ConfigKeys.NetworkAccessModeKeys.MOBILE to ConfigValues.NetworkAccessModeValues.ALERT,
            // 网络访问模式-蓝牙：allow, deny, alert
            ConfigKeys.NetworkAccessModeKeys.BLUETOOTH to ConfigValues.NetworkAccessModeValues.ALERT,
            // 网络访问模式-WIFI：allow, deny, alert
            ConfigKeys.NetworkAccessModeKeys.WIFI to ConfigValues.NetworkAccessModeValues.ALERT,
            // 网络访问模式-其他：allow, deny, alert
            ConfigKeys.NetworkAccessModeKeys.OTHER to ConfigValues.NetworkAccessModeValues.ALERT,

//            // downloadId
            ConfigKeys.DOWNLOAD_ID to -1,

            // 保存按钮的位置
            ConfigKeys.SAVE_BUTTON_POSITION to listOf(-1, -1),

//            // 全部工具页面的列数
//            ConfigKeys.TOOL_LIST_COLUMN_COUNT to 1,
//            // Scheme 页面的列数
//            ConfigKeys.SCHEME_LIST_COLUMN_COUNT to 1,

            // 打开工具后退出本软件
            ConfigKeys.Launch.EXIT_AFTER_LAUNCH to false,

            // 启动的主页
            ConfigKeys.Launch.HOME_PAGE to WIDGET_LIST_FRAGMENT,

            // 显示收藏标志
            ConfigKeys.Display.SHOW_LIKE_ICON to true,

            // 显示不可用的工具
            ConfigKeys.Display.SHOW_UNAVAILABLE_TOOLS to false,

            // 折叠的组(使用列表是因为 Json 没有 Set 类型)
            ConfigKeys.CollapsedGroups.TOOL_LIST to listOf<String>(),
            ConfigKeys.CollapsedGroups.SCHEME_LIST to listOf<String>(),


            // 搜索历史
            ConfigKeys.SearchHistory.MAX_COUNT to 10,
            ConfigKeys.SearchHistory.TOOL_LIST to listOf<String>(),
            ConfigKeys.SearchHistory.SCHEME_LIST to listOf<String>(),
            ConfigKeys.SearchHistory.INSTALLED_APP_LIST to listOf<String>(),


            // 搜索建议
            ConfigKeys.SearchSuggestion.MAX_COUNT to 10,
            ConfigKeys.SearchSuggestion.TOOL_LIST to JSONObject(),
            ConfigKeys.SearchSuggestion.SCHEME_LIST to JSONObject(),
            ConfigKeys.SearchSuggestion.INSTALLED_APP_LIST to JSONObject(),
        )
    }
}