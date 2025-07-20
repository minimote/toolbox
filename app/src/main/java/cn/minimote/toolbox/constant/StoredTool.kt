/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

import cn.minimote.toolbox.R

object StoredTool {

    const val STORED_FILE_NAME = "stored_tools.json"

    object DisplayMode {
        const val ICON_AND_NAME = 0
        const val ONLY_ICON = 1
        const val ONLY_NAME = 2

        val idToStringIdMap by lazy {
            linkedMapOf(
                ONLY_ICON to R.string.only_icon,
                ONLY_NAME to R.string.only_name,
                ICON_AND_NAME to R.string.icon_and_name,
            )
        }
    }

}