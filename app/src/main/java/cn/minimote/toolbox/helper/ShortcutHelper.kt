/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.widget.Toast
import cn.minimote.toolbox.R
import cn.minimote.toolbox.dataClass.Tool
import cn.minimote.toolbox.viewModel.MyViewModel
import java.util.UUID

object ShortcutHelper {

    fun createShortcut(
        context: Context,
        viewModel: MyViewModel,
        tool: Tool,
        shortLabel: String,
        longLabel: String,
    ) {
        val intent = LaunchHelper.getShortcutIntent(context, tool)
        if(intent == null) {
            // 创建快捷方式失败
            Toast.makeText(
                context,
                R.string.shortcut_create_failed,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // 获取高清图标
        val icon = viewModel.iconCacheHelper.getHighResIcon(tool)
        // 生成唯一 ID
        val uniqueId = UUID.randomUUID().toString()
        val shortcutInfo = ShortcutInfo.Builder(context, uniqueId)
            .setShortLabel(shortLabel)
            .setLongLabel(longLabel)
            .setIcon(icon)
            .setIntent(intent)
            .build()

        val shortcutManager = context.getSystemService(ShortcutManager::class.java)

        if(shortcutManager != null) {
            if(shortcutManager.isRequestPinShortcutSupported) {
                // 创建一个 PendingIntent 用于请求固定快捷方式
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    Intent(),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                shortcutManager.requestPinShortcut(
                    shortcutInfo,
                    pendingIntent.intentSender,
                )

            } else {
                // 设备不支持固定快捷方式
                Toast.makeText(
                    context,
                    R.string.toast_shortcut_not_supported,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}
