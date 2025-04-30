/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity


class TimeService : Service() {
    //    private var mTimer: Timer? = null
    private val channelId = "TimeServiceChannel"
    private val channelName = "时间小组件更新服务"
    private val notificationId = 1
    private lateinit var handler: Handler

    override fun onCreate() {
        super.onCreate()

        // 创建通知通道
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)

        // 生成通知并绑定点击事件
        val mainIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(this, channelId)
            .setContentTitle(channelName)
            .setSmallIcon(R.drawable.ic_toolbox)
            .setContentIntent(pendingIntent) // 绑定点击跳转
            .build()

        startForeground(notificationId, notification)

        // 初始化定时器
//        mTimer = Timer()
//        mTimer!!.schedule(MyTimerTask(this), 0, 1000)

        handler = Handler(Looper.getMainLooper())
        val timeRunnable = TimeRunnable(this, handler)
        timeRunnable.run()
    }


    override fun onDestroy() {
        super.onDestroy()
        // 关键步骤：移除通知并停止定时器
        stopForeground(STOP_FOREGROUND_REMOVE)
//        mTimer?.cancel()     // 取消定时器
//        mTimer = null
        handler.removeCallbacksAndMessages(null)
    }


    override fun onBind(intent: Intent?): IBinder? = null
}
