/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

// import com.heytap.wearable.support.recycler.widget.RecyclerView
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.data_class.InstalledActivity
import cn.minimote.toolbox.fragment.ActivityListFragment
import cn.minimote.toolbox.objects.VibrationHelper
import cn.minimote.toolbox.view_model.ToolboxViewModel
import com.google.android.material.switchmaterial.SwitchMaterial


class ActivityListAdapter(
    private val context: Context,
    private val fragment: ActivityListFragment,
    private val viewModel: ToolboxViewModel,
) : RecyclerView.Adapter<ActivityListAdapter.AppViewHolder>() {

    companion object {
        private const val VIEW_TYPE_NORMAL = 0
        private const val VIEW_TYPE_TITLE = 1
    }

    private var installedAppList: List<InstalledActivity> =
        viewModel.installedActivityList.value ?: getEmptyList()


    class AppViewHolder(itemView: View, val viewType: Int) : RecyclerView.ViewHolder(itemView) {
        val appName: TextView = itemView.findViewById(R.id.app_name)

        lateinit var appIcon: ImageView
        lateinit var activityName: TextView
        lateinit var switch: SwitchMaterial

        init {
            when(viewType) {
                VIEW_TYPE_NORMAL -> {
                    appIcon = itemView.findViewById(R.id.widget_icon)
                    activityName = itemView.findViewById(R.id.activity_name)
                    switch = itemView.findViewById(R.id.switch_whether_show_in_home)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val layoutId = when(viewType) {
            VIEW_TYPE_NORMAL -> R.layout.item_activity
            VIEW_TYPE_TITLE -> R.layout.item_activity_title
            else -> -1
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return AppViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val installedActivity = installedAppList[position]

        // 项目被点到时切换开关状态
        val toggleSwitch = {
            val searchBoxText = fragment.searchBox.text.toString().trim()
            if(viewModel.searchMode.value == true && searchBoxText.isEmpty()
            ) {
                fragment.buttonCancel.performClick()
            } else {
                if(holder.viewType == VIEW_TYPE_NORMAL) {
                    holder.switch.isChecked = !holder.switch.isChecked

                    // 触发设备振动
                    VibrationHelper.vibrateOnClick(context)

                    // 根据开关状态更新字典
                    viewModel.toggleSwitch(holder.switch.isChecked, installedActivity)
                } else {
                }
            }
        }

        holder.itemView.setOnClickListener() {
            toggleSwitch()
        }

        holder.appName.text = installedActivity.appName
        if(holder.viewType == VIEW_TYPE_TITLE) {
            // 无结果提示为灰色正常字体
            if(installedActivity.appName == context.getString(R.string.no_result)) {
                // 设置字体样式为正常
                holder.appName.setTypeface(null, Typeface.NORMAL)
                // 设置字体颜色为灰色
                holder.appName.setTextColor(context.getColor(R.color.mid_gray))
            } else {
                // 设置字体样式为粗体
                holder.appName.setTypeface(null, Typeface.BOLD)
                // 设置字体颜色为白色
                holder.appName.setTextColor(context.getColor(R.color.white))
            }
            return
        }

        holder.appIcon.setImageDrawable(viewModel.getIcon(installedActivity))
        // 活动名仅显示最后一个点后面的部分
        holder.activityName.text = installedActivity.activityName.substringAfterLast('.')
        holder.switch.isChecked = viewModel.isStoredActivity(installedActivity.activityName)
//        Log.i(
//            "ActivityListAdapter",
//            " ${installedActivity.appName} ${holder.switch.isChecked}"
//        )
    }


    override fun getItemCount(): Int = installedAppList.size


    override fun getItemViewType(position: Int): Int {
        return if(installedAppList[position].packageName.isEmpty()) {
            VIEW_TYPE_TITLE
        } else {
            VIEW_TYPE_NORMAL
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newInstalledAppList: List<InstalledActivity> = loadActivityList()) {
        installedAppList = newInstalledAppList.ifEmpty {
            getEmptyList()
        }
        notifyDataSetChanged()
    }


    private fun getEmptyList(): List<InstalledActivity> {
        return mutableListOf(
            InstalledActivity(
                appName = context.getString(R.string.no_result),
                packageName = "",
                activityName = "",
            )
        )
    }

    private fun loadActivityList(): List<InstalledActivity> {
        return viewModel.installedActivityList.value ?: mutableListOf()
    }
}
