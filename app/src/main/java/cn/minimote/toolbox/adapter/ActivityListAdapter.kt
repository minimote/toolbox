/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.data_class.InstalledActivity
import cn.minimote.toolbox.objects.VibrationHelper
import cn.minimote.toolbox.view_model.ToolboxViewModel
import com.google.android.material.switchmaterial.SwitchMaterial


class ActivityListAdapter(
    private val context: Context,
    private val viewModel: ToolboxViewModel,
) : RecyclerView.Adapter<ActivityListAdapter.AppViewHolder>() {

    private var installedAppList: List<InstalledActivity> =
        viewModel.installedActivityList.value ?: listOf()


    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val appIcon: ImageView = itemView.findViewById(R.id.widget_icon)
        val appName: TextView = itemView.findViewById(R.id.app_name)
        val activityName: TextView = itemView.findViewById(R.id.activity_name)
        val switch: SwitchMaterial = itemView.findViewById(R.id.switch_whether_show_in_home)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val installedActivity = installedAppList[position]

        holder.appIcon.setImageDrawable(viewModel.getIcon(installedActivity.iconName))
        holder.appName.text = installedActivity.appName
        // 活动名仅显示最后一个点后面的部分
        holder.activityName.text = installedActivity.activityName.substringAfterLast('.')
        holder.switch.isChecked = viewModel.isStoredActivity(installedActivity.activityName)
//        Log.i(
//            "ActivityListAdapter",
//            " ${installedActivity.appName} ${holder.switch.isChecked}"
//        )

        // 项目被点到时切换开关状态
        val toggleSwitch = {
            holder.switch.isChecked = !holder.switch.isChecked

            // 触发设备振动
            VibrationHelper.vibrateOnClick(context)

            // 根据开关状态更新字典
            viewModel.toggleSwitch(holder.switch.isChecked, installedActivity)
        }

        holder.itemView.setOnClickListener()
        {
            toggleSwitch()
        }
    }

    override fun getItemCount(): Int = installedAppList.size
}
