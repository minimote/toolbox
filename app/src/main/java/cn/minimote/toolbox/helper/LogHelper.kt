/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.util.Log
import cn.minimote.toolbox.BuildConfig


object LogHelper {
    // debug 模式下才启用日志
    val LOG_ENABLE = BuildConfig.DEBUG


    fun e(tag: String?, msg: String?, tr: Throwable? =  null) {
        if (LOG_ENABLE) {
            Log.e(tag, msg, tr)
        }
    }

    fun d(tag: String?, msg: String) {
        if (LOG_ENABLE) {
            Log.d(tag, msg)
        }
    }
}