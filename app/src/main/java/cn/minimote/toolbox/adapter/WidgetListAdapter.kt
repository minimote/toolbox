/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.constant.MenuList
import cn.minimote.toolbox.constant.MenuType
import cn.minimote.toolbox.constant.ToolConstants.Alignment
import cn.minimote.toolbox.constant.ToolConstants.DisplayMode
import cn.minimote.toolbox.dataClass.StoredTool
import cn.minimote.toolbox.fragment.WidgetListFragment
import cn.minimote.toolbox.helper.BottomSheetDialogHelper
import cn.minimote.toolbox.helper.LaunchHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.MyViewModel


class WidgetListAdapter(
    private val myActivity: MainActivity,
    private val viewModel: MyViewModel,
    private val fragment: WidgetListFragment,
) : RecyclerView.Adapter<WidgetListAdapter.WidgetViewHolder>() {

    private lateinit var itemTouchHelper: ItemTouchHelper
    var toolList: MutableList<StoredTool> = loadToolList()


    inner class WidgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewBackground: View = itemView.findViewById(R.id.view_background)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetViewHolder {

        val layoutId = R.layout.item_widget_icon_and_name
        val view = LayoutInflater.from(myActivity).inflate(layoutId, parent, false)

        return WidgetViewHolder(view)
    }


    @SuppressLint("RtlHardcoded")
    override fun onBindViewHolder(holder: WidgetViewHolder, position: Int) {

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


        // 根据 selectedIds 设置背景状态
        val isSelected = viewModel.isSelected(tool.id)
        holder.viewBackground.visibility = if(
            viewModel.multiselectMode.value == true && isSelected
        ) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }


        holder.itemView.setOnClickListener {
            if(viewModel.multiselectMode.value == true) {
                VibrationHelper.vibrateOnClick(viewModel)
                if(viewModel.isSelected(tool.id)) {
                    viewModel.deselectItem(tool.id)
                    holder.viewBackground.visibility = View.INVISIBLE
                } else {
                    viewModel.selectedItem(tool.id)
                    holder.viewBackground.visibility = View.VISIBLE
                }
            } else if(viewModel.sortMode.value != true) {
                VibrationHelper.vibrateOnClick(viewModel)
                // 启动新活动并结束当前活动
                startActivityAndFinishCurrent(tool)
            }
        }

        // 禁用振动反馈
        holder.itemView.isHapticFeedbackEnabled = false
        holder.itemView.setOnLongClickListener {
            if(viewModel.multiselectMode.value != true && viewModel.sortMode.value != true) {
                VibrationHelper.vibrateOnLongPress(viewModel)
                BottomSheetDialogHelper.setAndShowBottomSheetDialog(
                    viewModel = viewModel,
                    activity = myActivity,
                    tool = tool,
                    menuList = if(viewModel.isWatch) MenuList.widget_watch else MenuList.widget,
                    onMenuItemClick = { menuItemId ->
                        when(menuItemId) {
                            MenuType.MULTI_SELECT -> {
                                holder.viewBackground.visibility = View.VISIBLE
                                viewModel.clearSelectedIds()
                                viewModel.selectedItem(tool.id)
                                viewModel.multiselectMode.value = true
                            }

                            MenuType.ADD_TO_HOME_OR_REMOVE_FROM_HOME -> {
                            }

                            MenuType.SORT -> {
//                                viewModel.sortMode.value = true
                                fragment.buttonSortMode.performClick()
                            }

                            else -> {}
                        }
                    }
                )
            }

            true // 返回 true 以表示事件已处理，不再继续传递
        }
    }


    // 启动新活动并结束当前活动
    private fun startActivityAndFinishCurrent(appInfo: StoredTool) {
        LaunchHelper.launch(
            myActivity = myActivity,
            viewModel = viewModel,
            tool = appInfo,
//            tool = Tool(
//                id = appInfo.id,
//                intentType = SCHEME,
//                packageName = appInfo.packageName,
//                name = "",
//                intentUri = "toolbox://",
//                description = "1",
//            )
        )
//        if(flag) {
//            (context as? Activity)?.finishAffinity()
//        }
    }


    override fun getItemCount(): Int = toolList.size


    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newActivityList: MutableList<StoredTool> = loadToolList()) {
//        LogHelper.e("WidgetListAdapter", "submitList:${newActivityList.size}")
        toolList = newActivityList
        notifyDataSetChanged()
    }


    private fun loadToolList(): MutableList<StoredTool> {
        return viewModel.storedToolList.value ?: mutableListOf()
    }


    fun setItemTouchHelper(itemTouchHelper: ItemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper
    }

}
