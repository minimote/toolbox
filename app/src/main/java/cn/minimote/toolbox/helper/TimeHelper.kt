/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.content.Context
import cn.minimote.toolbox.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeHelper {

    // 获取当前时间戳
    fun getCurrentTimeMillis(): Long = System.currentTimeMillis()


    // 将时间戳转换为格式化时间
    fun getFormatTimeString(
        context: Context,
        timestamp: Long,
    ): String {
        if(timestamp < 0) {
            return context.getString(R.string.none)
        }
        val dateFormat = SimpleDateFormat(
            context.getString(R.string.check_time_format),
            Locale.CHINA
        )
        return dateFormat.format(Date(timestamp))
    }
}