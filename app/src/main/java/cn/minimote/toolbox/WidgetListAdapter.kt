/*
 * Copyright (c) 2024 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class WidgetListAdapter(
    private val context: Context,
    private var widgetList: MutableList<WidgetInfo>
) :
    RecyclerView.Adapter<WidgetListAdapter.WidgetViewHolder>() {

    private lateinit var itemTouchHelper: ItemTouchHelper

    inner class WidgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIcon: ImageView = itemView.findViewById(R.id.app_icon)
        val widgetName: TextView = itemView.findViewById(R.id.widget_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_widget, parent, false)
        return WidgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: WidgetViewHolder, position: Int) {
        val widgetInfo = widgetList[position]
        holder.appIcon.setImageDrawable(widgetInfo.appIcon)
        holder.widgetName.text = widgetInfo.nickName

        holder.itemView.setOnClickListener {
            VibrationUtil.vibrateOnClick(context)
            Log.i("WidgetListAdapter", "启动 ${widgetInfo.appName}")
            // 启动相应活动并结束当前应用
            val intent = Intent().apply {
                // 设置目标应用的包名和活动名
                component = ComponentName(widgetInfo.packageName, widgetInfo.activityName)
                // 添加标志以清除当前任务栈
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            // 结束当前应用
            (context as? Activity)?.finishAffinity()
        }


        holder.itemView.setOnLongClickListener {
            // 进入编辑模式
            editWidget(widgetInfo)
            true
        }
    }

    override fun getItemCount(): Int = widgetList.size

    fun setItemTouchHelper(itemTouchHelper: ItemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper
    }

    private fun editWidget(widget: WidgetInfo) {
        // 实现编辑逻辑，例如弹出对话框修改名称和大小
        // 修改完成后更新 widgetList 并调用 notifyItemChanged
    }
}
