/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.widget.RemoteViews
import cn.minimote.toolbox.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


class TimeRunnable(
    private val context: Context,
    private val handler: Handler // 需传递主线程 Handler
) : Runnable {

    override fun run() {
        updateWidget() // 更新 Widget 的逻辑
        // 每秒重新调度（根据需求调整间隔）
        handler.postDelayed(this, 1000)
    }

    private fun updateWidget() {
        val widgetManager = AppWidgetManager.getInstance(context)
        val remoteViews = RemoteViews(context.packageName, R.layout.time_widget_layout)

        // 时间与日期更新逻辑（保持原有代码）
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault())
        val currentTime = LocalTime.now(ZoneId.systemDefault())
        remoteViews.setTextViewText(R.id.textView_time_widget, currentTime.format(timeFormatter))

        val dateFormatter = DateTimeFormatter.ofPattern("MM月dd日  E", Locale.getDefault())
        val currentDate = LocalDate.now(ZoneId.systemDefault())
        remoteViews.setTextViewText(R.id.textView_date_widget, currentDate.format(dateFormatter))

        val componentName = ComponentName(context, TimeWidget::class.java)
        widgetManager.updateAppWidget(componentName, remoteViews)
    }
}