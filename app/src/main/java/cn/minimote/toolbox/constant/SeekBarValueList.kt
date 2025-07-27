/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

import cn.minimote.toolbox.constant.Config.ConfigValues

object SeekBarValueList {
    // 网络访问模式列表
    val networkAccessModeList = listOf(
        ConfigValues.NetworkAccessModeValues.ALLOW,
        ConfigValues.NetworkAccessModeValues.ALERT,
        ConfigValues.NetworkAccessModeValues.DENY,
    )

    // 更新检查频率列表
    val checkUpdateFrequencyList = listOf(
        ConfigValues.CheckUpdateFrequency.DAILY,
        ConfigValues.CheckUpdateFrequency.WEEKLY,
        ConfigValues.CheckUpdateFrequency.MONTHLY,
        ConfigValues.CheckUpdateFrequency.NEVER,
    )

    // 振动模式列表
    val vibrationModeList = listOf(
        ConfigValues.VibrationMode.ON,
        ConfigValues.VibrationMode.AUTO,
        ConfigValues.VibrationMode.OFF,
    )


    // 列数列表
    val columnCountList =
        (ToolConstants.MIN_WIDGET_SIZE..ToolConstants.MAX_WIDGET_SIZE).map { it.toString() }


}