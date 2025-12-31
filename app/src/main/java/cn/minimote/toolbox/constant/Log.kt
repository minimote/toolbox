/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

import cn.minimote.toolbox.BuildConfig


object Log {
    // debug 模式下才启用日志
    val LOG_ENABLE = BuildConfig.DEBUG

    // 日志文件名
    const val LOG_FILE_NAME = "log/app.log"
}