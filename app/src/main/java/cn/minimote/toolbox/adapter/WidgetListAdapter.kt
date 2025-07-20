/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.MenuList
import cn.minimote.toolbox.constant.MenuType
import cn.minimote.toolbox.constant.StoredTool.DisplayMode
import cn.minimote.toolbox.dataClass.StoredTool
import cn.minimote.toolbox.fragment.WidgetListFragment
import cn.minimote.toolbox.helper.ActivityLaunchHelper
import cn.minimote.toolbox.helper.BottomSheetDialogHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.MyViewModel


class WidgetListAdapter(
    private val context: Context,
    private val viewModel: MyViewModel,
    private val fragment: WidgetListFragment,
    private val fragmentManager: FragmentManager,
    private val viewPager: ViewPager2,
) : RecyclerView.Adapter<WidgetListAdapter.WidgetViewHolder>() {

    private lateinit var itemTouchHelper: ItemTouchHelper
    //    var activityList: MutableList<StoredActivity> =
//        viewModel.storedActivityList.value ?: mutableListOf()
    var toolList: MutableList<StoredTool> = loadToolList()


    inner class WidgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIcon: ImageView? = itemView.findViewById(R.id.imageView_app_icon)
        val widgetName: TextView? = itemView.findViewById(R.id.textView_app_name)
        val viewBackground: View = itemView.findViewById(R.id.view_background)
    }


    override fun getItemViewType(position: Int): Int {
        return toolList[position].displayMode
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetViewHolder {
//        Log.i("WidgetListAdapter", "viewModel:${System.identityHashCode(viewModel)}")
        val layoutId = when(viewType) {
            DisplayMode.ONLY_ICON -> R.layout.item_widget_only_icon
            DisplayMode.ONLY_NAME -> R.layout.item_widget_only_name
            else -> R.layout.item_widget_icon_and_name
        }
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)

        return WidgetViewHolder(view)
    }


    override fun onBindViewHolder(holder: WidgetViewHolder, position: Int) {

        val toolActivity = toolList[position]

        when(toolActivity.displayMode) {
            DisplayMode.ONLY_ICON -> {
                holder.appIcon?.setImageDrawable(
                    viewModel.iconCacheHelper.getCircularDrawable(toolActivity)
                )
                holder.appIcon?.visibility = View.VISIBLE
                holder.widgetName?.visibility = View.GONE
            }

            DisplayMode.ONLY_NAME -> {
                holder.appIcon?.visibility = View.GONE
                holder.widgetName?.text = toolActivity.nickname
                holder.widgetName?.visibility = View.VISIBLE
            }

            else -> {
                holder.appIcon?.setImageDrawable(
                    viewModel.iconCacheHelper.getCircularDrawable(toolActivity)
                )
                holder.appIcon?.visibility = View.VISIBLE
                holder.widgetName?.text = toolActivity.nickname
                holder.widgetName?.visibility = View.VISIBLE
            }
        }

        // 根据 selectedIds 设置背景状态
        val isSelected = viewModel.isSelected(toolActivity.id)
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
                if(viewModel.isSelected(toolActivity.id)) {
                    viewModel.deselectItem(toolActivity.id)
                    holder.viewBackground.visibility = View.INVISIBLE
                } else {
                    viewModel.selectedItem(toolActivity.id)
                    holder.viewBackground.visibility = View.VISIBLE
                }
            } else if(viewModel.sortMode.value != true) {
                VibrationHelper.vibrateOnClick(viewModel)
                // 启动新活动并结束当前活动
                startActivityAndFinishCurrent(toolActivity)
            }
        }

        // 禁用振动反馈
        holder.itemView.isHapticFeedbackEnabled = false
        holder.itemView.setOnLongClickListener {
            if(viewModel.multiselectMode.value != true && viewModel.sortMode.value != true) {
                VibrationHelper.vibrateOnLongPress(viewModel)
                BottomSheetDialogHelper.setAndShowBottomSheetDialog(
                    context = context,
                    viewModel = viewModel,
                    tool = toolActivity,
                    menuList = MenuList.widget,
                    viewPager = viewPager,
                    fragmentManager = fragmentManager,
                    constraintLayoutOrigin = fragment.constraintLayoutOrigin,
                    onMenuItemClick = { menuItemId ->
                        when(menuItemId) {
                            MenuType.MULTI_SELECT -> {
                                holder.viewBackground.visibility = View.VISIBLE
                                viewModel.clearSelectedIds()
                                viewModel.selectedItem(toolActivity.id)
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
        val flag = ActivityLaunchHelper.launch(
            context = context,
            viewModel = viewModel,
            tool = appInfo,
        )
        if(flag) {
            (context as? Activity)?.finishAffinity()
        }
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
