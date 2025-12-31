/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

object ViewPaper {
    // viewPaper 的 Fragment 列表
    val fragmentList = listOf(
        FragmentName.TOOL_LIST_FRAGMENT,
        FragmentName.WIDGET_LIST_FRAGMENT,
        FragmentName.MY_LIST_FRAGMENT,
    )

    // 获取不到配置时的默认值
    val START_FRAGMENT_POS = fragmentList.indexOf(FragmentName.WIDGET_LIST_FRAGMENT)
}