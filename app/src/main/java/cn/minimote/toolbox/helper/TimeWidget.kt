/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import cn.minimote.toolbox.R


class TimeWidget : AppWidgetProvider() {
    private var serviceIntent: Intent? = null  // 新增成员变量

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

        for(appWidgetId in appWidgetIds) {
//            serviceIntent = Intent(context, TimeService::class.java)
//            context.startForegroundService(serviceIntent)
//            serviceIntent.putExtra("appWidgetId", appWidgetId)
//            context.startService(serviceIntent)

//            Log.e("onUpdate", "更新 $appWidgetId")

            val remoteViews = RemoteViews(context.packageName, R.layout.time_widget_layout)

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }


    override fun onEnabled(context: Context) {
//        Log.d("TimeWidget", "Starting service...")
        serviceIntent = Intent(context, TimeService::class.java)  // 保存Intent实例
        context.startForegroundService(serviceIntent)
    }

    override fun onDisabled(context: Context) {
//        Log.d("TimeWidget", "Stop service...")
        serviceIntent?.let {  // 使用保存的Intent实例
            context.stopService(it)
            serviceIntent = null  // 避免内存泄漏
        }
    }
}
