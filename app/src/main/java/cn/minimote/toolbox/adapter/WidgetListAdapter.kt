/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.data_class.StoredActivity
import cn.minimote.toolbox.fragment.EditListFragment
import cn.minimote.toolbox.objects.FragmentManagerHelper
import cn.minimote.toolbox.objects.VibrationHelper
import cn.minimote.toolbox.view_model.ToolboxViewModel


class WidgetListAdapter(
    private val context: Context,
    private val viewModel: ToolboxViewModel,
    private val fragmentManager: FragmentManager,
) : RecyclerView.Adapter<WidgetListAdapter.WidgetViewHolder>() {

    private lateinit var itemTouchHelper: ItemTouchHelper
    //    var activityList: MutableList<StoredActivity> =
//        viewModel.storedActivityList.value ?: mutableListOf()
    var activityList: MutableList<StoredActivity> = loadActivityList()


//    private var buttonSave: Button = viewModel.buttonSave
//    private var isEditMode: MutableLiveData<Boolean> = viewModel.isEditMode
//    private val editBackground: ImageView = viewModel.editBackground


    class WidgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIcon: ImageView = itemView.findViewById(R.id.widget_icon)
        val widgetName: TextView? = itemView.findViewById(R.id.widget_name)
    }


    // 显示名称的视图类型为 1
    override fun getItemViewType(position: Int): Int {
        return if(activityList[position].showName) 1 else 0
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetViewHolder {
//        Log.i("WidgetListAdapter", "viewModel:${System.identityHashCode(viewModel)}")
        val layoutId = when(viewType) {
            1 -> R.layout.item_widget_icon_and_name
            else -> R.layout.item_widget_only_icon
        }
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        return WidgetViewHolder(view)
    }


    override fun onBindViewHolder(holder: WidgetViewHolder, position: Int) {
        val appInfo = activityList[position]

        if(appInfo.showName) {
            holder.widgetName?.text = appInfo.nickName
        }
        holder.appIcon.setImageDrawable(viewModel.getIcon(appInfo.iconName))

        holder.itemView.setOnClickListener {
//            toggleBackgroundColor(holder.itemView)
            VibrationHelper.vibrateOnClick(context)
            if(viewModel.editMode.value == true) {
//                Log.i("WidgetListAdapter", "编辑小组件<${appInfo.appName}>")
                val fragment = EditListFragment(viewModel)
                FragmentManagerHelper.replaceFragment(
                    fragmentManager = fragmentManager,
                    fragment = fragment,
                    viewModel = viewModel,
                )

                viewModel.originWidget.value = appInfo
                viewModel.modifiedWidget.value = appInfo.copy()
            } else {
                // 启动新活动并结束当前活动
                startActivityAndFinishCurrent(appInfo)
            }
        }

        holder.itemView.setOnLongClickListener {
            VibrationHelper.vibrateOnClick(context)
            if(viewModel.editMode.value != true) {
                viewModel.editMode.value = true
//                Log.i("WidgetListAdapter", "进入编辑模式")
                Toast.makeText(
                    context,
                    context.getString(R.string.enter_edit_mode),
                    Toast.LENGTH_SHORT,
                ).show()
            } else {
//                Toast.makeText(
//                    context,
//                    context.getString(R.string.already_enter_edit_mode),
//                    Toast.LENGTH_SHORT,
//                ).show()
                itemTouchHelper.startDrag(holder)
            }
            true // 返回 true 以表示事件已处理，不再继续传递
        }
    }


    // 启动新活动并结束当前活动
    private fun startActivityAndFinishCurrent(appInfo: StoredActivity) {
        val intentList = listOf(
            // 按包名和活动名启动
            Intent().apply {
                component = ComponentName(appInfo.packageName, appInfo.activityName)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            },
            // 使用 parseUri 启动
            Intent.parseUri(appInfo.activityName, Intent.URI_INTENT_SCHEME),
            // 使用 putExtra 启动
            getIntentPutExtra(appInfo),
        )
        for(intent in intentList) {
            try {
                context.startActivity(intent)
                (context as? Activity)?.finishAffinity()
                return
            } catch(e: Exception) {
                // 如果启动失败，继续尝试下一个 Intent
            }
        }
        // 启动失败，显示错误信息
        Toast.makeText(
            context,
            context.getString(R.string.start_fail, appInfo.activityName),
            Toast.LENGTH_LONG,
        ).show()
    }


    private fun getIntentPutExtra(appInfo: StoredActivity): Intent? {
        val activityName = appInfo.activityName
        val parts = activityName.split(context.getString(R.string.split_char))
        if(parts.size != 3) {
            if(parts.size == 1) {
                return context.packageManager.getLaunchIntentForPackage(appInfo.packageName)
                    ?.apply {
                        putExtra(activityName, true)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
            }
            return null
        }
        val action = parts[0]
        val extraKey = parts[1]
        val extraValue = parts[2]

        return Intent(action).apply {
            putExtra(extraKey, extraValue)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }


    override fun getItemCount(): Int = activityList.size


    @SuppressLint("NotifyDataSetChanged")
    fun submitList() {
        activityList = loadActivityList()
        notifyDataSetChanged()
    }


    private fun loadActivityList(): MutableList<StoredActivity> {
        return viewModel.storedActivityList.value ?: mutableListOf()
    }


    fun setItemTouchHelper(itemTouchHelper: ItemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper
    }

}
