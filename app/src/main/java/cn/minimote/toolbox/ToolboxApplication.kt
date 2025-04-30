/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class ToolboxApplication : Application() {
    override fun onCreate() {
        super.onCreate()
//        createNotificationChannel()
    }

//    private fun createNotificationChannel() {
//        val channelId = "time_channel"
//        val channelName = "时间更新"
//        val channel = NotificationChannel(
//            channelId,
//            channelName,
//            NotificationManager.IMPORTANCE_LOW
//        ).apply {
//            description = "用于时间小组件后台更新的通知渠道"
//            lockscreenVisibility = Notification.VISIBILITY_SECRET // 可选：控制锁屏显示
//        }
//
//        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//        // 先检查渠道是否存在
//        if(manager.getNotificationChannel(channelId) == null) {
//            manager.createNotificationChannel(channel)
//        }
//    }
}