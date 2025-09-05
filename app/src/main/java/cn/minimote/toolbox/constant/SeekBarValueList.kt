/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

import cn.minimote.toolbox.constant.Config.ConfigValues

object SeekBarValueList {
    // 网络访问模式列表
    val networkAccessMode = listOf(
        ConfigValues.NetworkAccessModeValues.ALLOW,
        ConfigValues.NetworkAccessModeValues.ALERT,
        ConfigValues.NetworkAccessModeValues.DENY,
    )

    // 更新检查频率列表
    val checkUpdateFrequency = listOf(
        ConfigValues.CheckUpdateFrequency.DAILY,
        ConfigValues.CheckUpdateFrequency.WEEKLY,
        ConfigValues.CheckUpdateFrequency.MONTHLY,
        ConfigValues.CheckUpdateFrequency.NEVER,
    )

    // 振动模式列表
    val vibrationMode = listOf(
        ConfigValues.VibrationMode.ON,
        ConfigValues.VibrationMode.AUTO,
        ConfigValues.VibrationMode.OFF,
    )

    val widgetWidth =
        (ToolConstants.MIN_WIDGET_WIDTH..ToolConstants.MAX_WIDGET_WIDTH).map { it.toString() }

    const val SEARCH_HISTORY_MAX_COUNT_LIMIT = 100
    const val SEARCH_SUGGESTION_MAX_COUNT_LIMIT = 100

    val searchHistoryMaxCount = (0..SEARCH_HISTORY_MAX_COUNT_LIMIT).map { it.toString() }
    val searchSuggestionMaxCount = (0..SEARCH_SUGGESTION_MAX_COUNT_LIMIT).map { it.toString() }

}