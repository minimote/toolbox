/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets


object Config {

    // 编码
    val ENCODING: Charset = StandardCharsets.UTF_8

    // 配置的文件名
    object ConfigFileName {
        const val DEFAULT_CONFIG = "default_config.jsonc"
        const val USER_CONFIG = "user_config.jsonc"
    }

    // 配置的键
    object ConfigKeys {
        // 上次检查更新时间
        const val LAST_CHECK_UPDATE_TIME = "last_check_update_time"
        // 振动模式
        const val VIBRATION_MODE = "vibration_mode"
        // 更新检查频率
        const val CHECK_UPDATE_FREQUENCY = "check_update_frequency"
        // 网络访问模式
        object NetworkAccessModeKeys {
            const val MOBILE = "network_access_mode_mobile"
            const val BLUETOOTH = "network_access_mode_bluetooth"
            const val WIFI = "network_access_mode_wifi"
            const val OTHER = "network_access_mode_other"
        }
    }


    // 配置的值
    object ConfigValues {
        // 振动模式：自动、启用、关闭
        object VibrationMode {
            const val AUTO = "auto"
            const val ON = "on"
            const val OFF = "off"
        }

        // 更新检查频率：每天、每周、每月、从不
        object CheckUpdateFrequency {
            const val DAILY = "daily"
            const val WEEKLY = "weekly"
            const val MONTHLY = "monthly"
            const val NEVER = "never"
        }

        // 网络访问模式
        object NetworkAccessModeValues {
            const val ALLOW = "allow"  // 允许
            const val DENY = "deny"    // 拒绝
            const val ALERT = "alert"  // 提示
        }
    }
}