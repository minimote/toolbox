/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.MenuList
import cn.minimote.toolbox.constant.MenuTypes
import cn.minimote.toolbox.dataClass.ToolActivity
import cn.minimote.toolbox.helper.ActivityLaunchHelper
import cn.minimote.toolbox.helper.ShortcutHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.ToolboxViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog


class BottomSheetAdapter(
    private val context: Context,
    private val viewModel: ToolboxViewModel,
    private val toolActivity: ToolActivity,
    private val bottomSheetDialog: BottomSheetDialog,
) : RecyclerView.Adapter<BottomSheetAdapter.BottomSheetHolder>() {
    val dataList = MenuList.tool

    inner class BottomSheetHolder(itemView: View, val viewType: Int) :
        RecyclerView.ViewHolder(itemView) {
        val textViewMenuItem: TextView = itemView.findViewById(R.id.textView_menuItem)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BottomSheetHolder {
        val layoutId = R.layout.item_bottom_sheet_menu
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        return BottomSheetHolder(view, viewType)
    }

    override fun onBindViewHolder(
        holder: BottomSheetHolder,
        position: Int
    ) {
        when(holder.viewType) {
            MenuTypes.CREATE_SHORTCUT -> {
                holder.textViewMenuItem.text = context.getString(R.string.create_shortcut)
            }

            MenuTypes.ADD_TO_HOME -> {
                // 已经在主页：从主页移除
                if(viewModel.inModifiedSizeMap(toolActivity.id)) {
                    holder.textViewMenuItem.text = context.getString(R.string.remove_from_home)
                } else {
                    // 不在主页：添加到主页
                    holder.textViewMenuItem.text = context.getString(R.string.add_to_home)
                }
            }
        }
        holder.itemView.setOnClickListener {
            VibrationHelper.vibrateOnClick(context, viewModel)
            bottomSheetDialog.dismiss()
            when(holder.viewType) {
                MenuTypes.CREATE_SHORTCUT -> {
                    createShortcut(context, toolActivity)
                }

                MenuTypes.ADD_TO_HOME -> {
                    addToHome(context, toolActivity)
                }
            }
        }
    }

    override fun getItemCount(): Int = dataList.size


    override fun getItemViewType(position: Int): Int {
        return dataList[position]
    }


    // 创建桌面快捷方式
    private fun createShortcut(context: Context, tool: ToolActivity) {
        val shortcutIntent = ActivityLaunchHelper.getIntent(context, tool)
        val icon = viewModel.iconCacheHelper.getIconIcon(tool)

        shortcutIntent?.let { nonNullIntent ->
            ShortcutHelper.createShortcut(
                context = context,
                shortcutIntent = nonNullIntent,
                shortLabel = tool.name,
                longLabel = if(tool.description.isNotBlank()) tool.description else tool.name,
                icon = icon,
            )
        }
    }


    // 添加到主页
    private fun addToHome(context: Context, tool: ToolActivity) {
        // 已经在主页：显示从主页移除
        if(viewModel.inModifiedSizeMap(tool.id)) {
            viewModel.removeFromModifiedMap(tool)
            Toast.makeText(
                context,
                context.getString(R.string.already_remove_from_home),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            // 不在主页：添加到主页
            viewModel.addToModifiedMap(tool)
            Toast.makeText(
                context,
                context.getString(R.string.add_success),
                Toast.LENGTH_SHORT,
            ).show()
        }
        viewModel.widgetListSizeWasModified()
        viewModel.saveWidgetList()
    }
}