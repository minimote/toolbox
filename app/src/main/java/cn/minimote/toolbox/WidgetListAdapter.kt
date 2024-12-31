/*
 * Copyright (c) 2024 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


class WidgetListAdapter(
    private val context: Context,
    private var widgetList: MutableList<WidgetInfo>,
    private var buttonSave: Button,
    private var isEditMode: MutableLiveData<Boolean>, // 新增编辑模式标志
    private val onEditClick: (WidgetInfo) -> Unit, // 新增编辑点击回调
    private val editBackground: ImageView,
) : RecyclerView.Adapter<WidgetListAdapter.WidgetViewHolder>() {

    private lateinit var itemTouchHelper: ItemTouchHelper

    companion object {

        const val VIEW_TYPE_FULL = 1
        const val VIEW_TYPE_HALF = 2
        const val VIEW_TYPE_THIRD = 3
    }

    inner class WidgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val appIcon: ImageView = itemView.findViewById(R.id.app_icon)
        val widgetName: TextView? = itemView.findViewById(R.id.widget_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetViewHolder {
//        Log.i("WidgetListAdapter", "小组件类型为<${viewType}>")
        val layoutId = when (viewType) {
            VIEW_TYPE_FULL -> R.layout.item_widget_full
            VIEW_TYPE_HALF -> R.layout.item_widget_half
            VIEW_TYPE_THIRD -> R.layout.item_widget_third
            else -> throw IllegalArgumentException("未知的 view 类型")
        }
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        return WidgetViewHolder(view)
    }


    override fun onBindViewHolder(holder: WidgetViewHolder, position: Int) {
        val widgetInfo = widgetList[position]

        if (holder.itemViewType == VIEW_TYPE_FULL) {
            holder.widgetName?.text = widgetInfo.nickName
        }
        holder.appIcon.setImageDrawable(widgetInfo.appIcon)

        holder.itemView.setOnClickListener {
            VibrationUtil.vibrateOnClick(context)
            if (isEditMode.value == true) {
                Log.i("WidgetListAdapter", "编辑小组件<${widgetInfo.appName}>")
                editWidget(widgetInfo)
//            onEditClick(widgetInfo) // 调用编辑点击回调
            } else {
                Log.i("WidgetListAdapter", "启动 ${widgetInfo.appName}")
                // 启动相应活动并结束当前应用
                val intent = Intent().apply {
                    // 设置目标应用的包名和活动名
                    component = ComponentName(widgetInfo.packageName, widgetInfo.activityName)
                    // 添加标志以清除当前任务栈
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(intent)
                    // 结束当前应用
                    (context as? Activity)?.finishAffinity()
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.start_fail, widgetInfo.activityName),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }


        holder.itemView.setOnLongClickListener {
            if (isEditMode.value != true) {
                VibrationUtil.vibrateOnClick(context)
                // 进入编辑模式
                isEditMode.value = true
                editBackground.visibility = View.VISIBLE
                buttonSave.text = context.getString(R.string.save_button)
                Toast.makeText(
                    context,
                    context.getString(R.string.enter_edit_mode),
                    Toast.LENGTH_SHORT,
                ).show()
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.already_enter_edit_mode),
                    Toast.LENGTH_SHORT,
                ).show()
            }
            true // 返回 true 以表示事件已处理，不再继续传递
        }
    }

    override fun getItemCount(): Int = widgetList.size

    override fun getItemViewType(position: Int): Int {
        return widgetList[position].widgetType
    }

    fun setItemTouchHelper(itemTouchHelper: ItemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper
    }

    private fun editWidget(widget: WidgetInfo) {
//        Log.i("WidgetListAdapter", "编辑小组件<${widget.appName}>")

        onEditClick(widget) // 调用编辑点击回调
    }
}
