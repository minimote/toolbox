/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper


import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import cn.minimote.toolbox.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimerTask


internal class MyTimerTask(
    private val context: Context,
) : TimerTask() {


    override fun run() {
        // 获取Widgets管理器

        val widgetManager = AppWidgetManager.getInstance(context)
        // widgetManager所操作的Widget对应的远程视图即当前Widget的layout文件
        val remoteViews = RemoteViews(context.packageName, R.layout.time_widget_layout)

        // 新增日期格式化
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd E", Locale.getDefault())
        val currentDate = LocalDate.now(ZoneId.systemDefault()) // 获取当前日期

        // 时间格式化（保留原有逻辑）
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault())
        val currentTime = LocalTime.now(ZoneId.systemDefault())

        // 同时设置时间和日期
        remoteViews.setTextViewText(R.id.textView_time_widget, currentTime.format(timeFormatter))
        remoteViews.setTextViewText(
            R.id.textView_date_widget,
            currentDate.format(dateFormatter)
        ) // 完整参数


//        remoteViews.setTextViewText(
//            R.id.time_widget_text,
//            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
//        )

        // 当点击Widgets时触发的事件
        val componentName =
            ComponentName(context, TimeWidget::class.java)
        widgetManager.updateAppWidget(componentName, remoteViews)
    }
}