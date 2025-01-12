/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
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
import cn.minimote.toolbox.fragment.EditWidgetFragment
import cn.minimote.toolbox.objects.FragmentManagerHelper
import cn.minimote.toolbox.objects.VibrationHelper
import cn.minimote.toolbox.view_model.ActivityViewModel


class WidgetListAdapter(
    private val context: Context,
    private val viewModel: ActivityViewModel,
    private val fragmentManager: FragmentManager,
) : RecyclerView.Adapter<WidgetListAdapter.WidgetViewHolder>() {

    private lateinit var itemTouchHelper: ItemTouchHelper
    private var activityList: MutableList<StoredActivity> =
        viewModel.storedActivityList.value ?: mutableListOf()
//    private var buttonSave: Button = viewModel.buttonSave
//    private var isEditMode: MutableLiveData<Boolean> = viewModel.isEditMode
//    private val editBackground: ImageView = viewModel.editBackground


    class WidgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val appIcon: ImageView = itemView.findViewById(R.id.app_icon)
        val widgetName: TextView? = itemView.findViewById(R.id.widget_name)
    }


    // 显示名称的视图类型为 1
    override fun getItemViewType(position: Int): Int {
        return if(activityList[position].showName) 1 else 0
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            WidgetViewHolder {
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
        holder.appIcon.setImageDrawable(viewModel.getIcon(appInfo.packageName))

        holder.itemView.setOnClickListener {
//            toggleBackgroundColor(holder.itemView)
            VibrationHelper.vibrateOnClick(context)
            if(viewModel.isEditMode.value == true) {
                Log.i("WidgetListAdapter", "编辑小组件<${appInfo.appName}>")
                val fragment = EditWidgetFragment(viewModel)
                FragmentManagerHelper.replaceFragment(
                    fragmentManager = fragmentManager,
                    fragment = fragment,
                    viewModel = viewModel,
                )

                viewModel.originWidget.value = appInfo
                viewModel.modifiedWidget.value = appInfo.copy()
            } else {
                Log.i("WidgetListAdapter", "启动 ${appInfo.appName}")
                // 启动相应活动并结束当前应用
                val intent = Intent().apply {
                    // 设置目标应用的包名和活动名
                    component = ComponentName(appInfo.packageName, appInfo.activityName)
                    // 添加标志以清除当前任务栈
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(intent)
                    // 结束当前应用
                    (context as? Activity)?.finishAffinity()
                } catch(e: ActivityNotFoundException) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.start_fail, appInfo.activityName),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        holder.itemView.setOnLongClickListener {
            VibrationHelper.vibrateOnClick(context)
            if(viewModel.isEditMode.value != true) {
                viewModel.isEditMode.value = true
                Log.i("WidgetListAdapter", "进入编辑模式")
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

//    private fun toggleBackgroundColor(view: View) {
//        val currentColor = (view.background as ColorDrawable).color
//        val newColor = if(currentColor == ContextCompat.getColor(view.context, R.color.black)) {
//            ContextCompat.getColor(view.context, R.color.deep_gray)
//        } else {
//            ContextCompat.getColor(view.context, R.color.light_gray)
//        }
//        view.setBackgroundColor(newColor)
//
//        // 50 毫秒（0.05 秒）后切换回原来的颜色
//        Handler(Looper.getMainLooper()).postDelayed({
//            view.setBackgroundColor(currentColor)
//        }, 50)
//    }

    override fun getItemCount(): Int = activityList.size


    @SuppressLint("NotifyDataSetChanged")
    fun submitList() {
        activityList = viewModel.storedActivityList.value ?: mutableListOf()
        notifyDataSetChanged()
    }


    fun setItemTouchHelper(itemTouchHelper: ItemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper
    }


//    private fun editWidget(widget: WidgetInfo) {
////        Log.i("WidgetListAdapter", "编辑小组件<${widget.appName}>")
//
//        onEditClick(widget) // 调用编辑点击回调
//    }
}
