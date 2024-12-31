/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial

class AppListAdapter(
    private val appList: MutableList<DisplayAppInfo>,
    private val context: Context,
    private var originalAppSet: MutableSet<StorageAppInfo>,
    private var originalMapActivityNameToStorageAppInfo: MutableMap<String, StorageAppInfo>,
    private var modifiedMapActivityNameToStorageAppInfo: MutableMap<String, StorageAppInfo>,
    private var buttonSave: Button,
) :
    RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val appIcon: ImageView = itemView.findViewById(R.id.app_icon)
        val appName: TextView = itemView.findViewById(R.id.app_name)
        val activityName: TextView = itemView.findViewById(R.id.activity_name)
        val switch: SwitchMaterial = itemView.findViewById(R.id.switchButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val appInfo = appList[position]
        holder.appIcon.setImageDrawable(appInfo.appIcon)
        holder.appName.text = appInfo.appName
        holder.activityName.text = appInfo.activityName.substringAfterLast('.')
        holder.switch.isChecked = appInfo.isSwitchOn

        val toggleSwitch = {
            appInfo.isSwitchOn = !appInfo.isSwitchOn
            holder.switch.isChecked = appInfo.isSwitchOn

            // 触发设备振动
            VibrationUtil.vibrateOnClick(context)

            // 根据开关状态更新字典
            if (appInfo.isSwitchOn) {
                // 如果之前就有，直接映射
                originalMapActivityNameToStorageAppInfo[appInfo.activityName]?.let { storageAppInfo ->
                    modifiedMapActivityNameToStorageAppInfo[appInfo.activityName] = storageAppInfo
                } ?: run {
                    // 如果之前没有，创建新的实例并映射
                    val newStorageAppInfo =
                        displayAppInfoToStorageAppInfo(appInfo, position = originalAppSet.size)
                    modifiedMapActivityNameToStorageAppInfo[appInfo.activityName] =
                        newStorageAppInfo
                    // 将新的实例添加进集合
                    originalAppSet.add(newStorageAppInfo)
                }
            } else {
                // 从修改字典中移除键
                modifiedMapActivityNameToStorageAppInfo.remove(appInfo.activityName)
            }

            // 判断字典是否发生变化
            if (
                originalMapActivityNameToStorageAppInfo == modifiedMapActivityNameToStorageAppInfo
            ) {
                buttonSave.visibility = View.GONE
                buttonSave.isEnabled = false
            } else {
                buttonSave.visibility = View.VISIBLE
                buttonSave.isEnabled = true
            }
        }

        holder.itemView.setOnClickListener()
        {
            toggleSwitch()
        }

        holder.appIcon.setOnClickListener()
        {
            toggleSwitch()
        }

        holder.appName.setOnClickListener()
        {
            toggleSwitch()
        }

        holder.switch.setOnClickListener()
        {
            toggleSwitch()
        }
    }

    override fun getItemCount(): Int = appList.size
}
