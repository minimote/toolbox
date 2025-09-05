/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

import cn.minimote.toolbox.R

object ToolConstants {

    const val STORED_FILE_NAME = "stored_tools.json"

    const val MIN_WIDGET_WIDTH = 1
    const val MAX_WIDGET_WIDTH = 12

    object DisplayMode {

        object String {
            const val ONLY_ICON = "only_icon"
            const val ONLY_NAME = "only_name"
            const val ICON_AND_NAME = "icon_and_name"
        }

        val idToStringIdMap by lazy {
            linkedMapOf(
                String.ONLY_ICON to R.string.only_icon,
                String.ONLY_NAME to R.string.only_name,
                String.ICON_AND_NAME to R.string.icon_and_name,
            )
        }
    }


    // 对齐方式
    object Alignment {
        const val LEFT = "left"
        const val CENTER = "center"
        const val RIGHT = "right"

        val idToStringIdMap by lazy {
            linkedMapOf(
                LEFT to R.string.align_left,
                CENTER to R.string.align_center,
                RIGHT to R.string.align_right,
            )
        }
    }

}