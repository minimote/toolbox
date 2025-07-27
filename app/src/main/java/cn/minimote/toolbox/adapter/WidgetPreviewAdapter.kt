/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.ToolConstants.Alignment
import cn.minimote.toolbox.constant.ToolConstants.DisplayMode
import cn.minimote.toolbox.dataClass.StoredTool
import cn.minimote.toolbox.viewModel.MyViewModel


class WidgetPreviewAdapter(
    private val context: Context,
    private val viewModel: MyViewModel,
) : RecyclerView.Adapter<WidgetPreviewAdapter.WidgetViewHolder>() {

    var toolList: MutableList<StoredTool> = mutableListOf(viewModel.editedTool.value!!)

    inner class WidgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) //{
    //
    //}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetViewHolder {

        val layoutId = R.layout.item_widget_icon_and_name
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)

        return WidgetViewHolder(view)
    }


    override fun onBindViewHolder(holder: WidgetViewHolder, position: Int) {
        holder.itemView.isClickable = false

        val tool = toolList[position]

        val widgetIcon: ImageView = holder.itemView.findViewById(R.id.imageView_app_icon)
        val widgetName: TextView = holder.itemView.findViewById(R.id.textView_app_name)

        when(tool.displayMode) {
            DisplayMode.String.ONLY_ICON -> {
                widgetIcon.setImageDrawable(
                    viewModel.iconCacheHelper.getCircularDrawable(tool)
                )
                widgetIcon.visibility = View.VISIBLE

                widgetName.visibility = View.GONE
            }

            DisplayMode.String.ONLY_NAME -> {
                widgetIcon.visibility = View.GONE

                widgetName.text = tool.nickname
                widgetName.visibility = View.VISIBLE
            }

            else -> {
                widgetIcon.setImageDrawable(
                    viewModel.iconCacheHelper.getCircularDrawable(tool)
                )
                widgetIcon.visibility = View.VISIBLE

                widgetName.text = tool.nickname
                widgetName.visibility = View.VISIBLE
            }
        }


        val linearLayout = holder.itemView.findViewById<LinearLayout>(R.id.linearLayout)
        val layoutParams = linearLayout.layoutParams as ConstraintLayout.LayoutParams

        when(tool.alignment) {
            Alignment.LEFT -> {
                layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                layoutParams.endToEnd = ConstraintLayout.LayoutParams.UNSET
                widgetName.gravity = Gravity.START
            }

            Alignment.RIGHT -> {
                layoutParams.startToStart = ConstraintLayout.LayoutParams.UNSET
                layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                widgetName.gravity = Gravity.END
            }

            else -> {
                layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                widgetName.gravity = Gravity.CENTER
            }
        }
        // 应用修改后的约束
        linearLayout.layoutParams = layoutParams

        true // 返回 true 以表示事件已处理，不再继续传递
    }


    override fun getItemCount(): Int = toolList.size


    fun submit() {
        notifyItemChanged(0)
    }

}
