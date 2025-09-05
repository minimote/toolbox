/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.UI
import cn.minimote.toolbox.constant.ViewTypes
import cn.minimote.toolbox.dataClass.InstalledApp
import cn.minimote.toolbox.fragment.InstalledAppListFragment
import cn.minimote.toolbox.helper.DialogHelper
import cn.minimote.toolbox.helper.SearchHelper.highlightSearchTermForDevice
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.MyViewModel
import com.google.android.material.switchmaterial.SwitchMaterial


class InstalledAppListAdapter(
    private val context: Context,
    private val fragment: InstalledAppListFragment,
    private val viewModel: MyViewModel,
) : RecyclerView.Adapter<ViewHolder>() {


    private var dataList: List<Any> =
        viewModel.installedAppList.value ?: emptyList()


    inner class AppViewHolder(itemView: View, val viewType: Int) : ViewHolder(itemView) {
        var appName: TextView = itemView.findViewById(R.id.textView_app_name)
        var appIcon: ImageView = itemView.findViewById(R.id.imageView_app_icon)
        var activityName: TextView = itemView.findViewById(R.id.textView_activity_name)
        var switch: SwitchMaterial = itemView.findViewById(R.id.switch_whether_show_in_home)


        fun highlightAppNameAndActivityName(activity: InstalledApp) {
            if(fragment.searchQuery.isNotEmpty()) {
                appName.text = highlightSearchTermForDevice(
                    viewModel = viewModel,
                    text = activity.name,
                    query = fragment.searchQuery,
                )
                activityName.text = highlightSearchTermForDevice(
                    viewModel = viewModel,
                    text = activity.activityName,
                    query = fragment.searchQuery,
                )
            } else {
                appName.text = activity.name
                activityName.text = activity.activityName
            }
        }

        fun bind(activity: InstalledApp) {

            appIcon.setImageDrawable(
                viewModel.iconCacheHelper.getCircularDrawable(activity)
            )

            // 高亮显示搜索结果
            highlightAppNameAndActivityName(activity)

            switch.isChecked = viewModel.inSizeChangeMap(activity.activityName)
            // 设置暗淡效果的滤镜
            setDimmingEffect(this, !switch.isChecked)


            itemView.setOnClickListener {
                switch.isChecked = !switch.isChecked

                // 触发设备振动
                VibrationHelper.vibrateOnClick(viewModel)

                // 设置暗淡效果的滤镜
                setDimmingEffect(this, !switch.isChecked)

                // 根据开关状态更新字典
                if(switch.isChecked) {
                    viewModel.addToSizeChangeMap(activity)
                } else {
                    viewModel.removeFromSizeChangeMap(activity.id)
                }
                // 如果修改后的关键字集合与原来的一样，复制映射
                viewModel.syncSizeChangeMapIfSameKeys()
                if(viewModel.searchMode.value == true) {
                    fragment.updateSearchHistoryAndSuggestion(activity.name.trim())
                }
            }
        }
    }


    inner class NoResultViewHolder(itemView: View) : ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.textView_name)

        fun bind(text: String) {
            name.text = text
            name.setTypeface(null, Typeface.NORMAL)
            // 设置字体颜色为灰色
            name.setTextColor(context.getColor(R.color.mid_gray))

            itemView.setOnClickListener {
                fragment.hideKeyboardAndClearFocus()
            }
        }
    }


    inner class HistoryOrSuggestionViewHolder(itemView: View) : ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.textView_name)

        fun bind(text: String) {
            textView.text = text
            itemView.setOnClickListener {
                VibrationHelper.vibrateOnClick(viewModel)
                fragment.setSearchBoxText(text)
                fragment.updateSearchHistoryAndSuggestion(text)
            }
        }
    }


    inner class SearchTitleViewHolder(itemView: View) : ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.textView_name)
        private val imageButtonClear: ImageButton = itemView.findViewById(R.id.imageButton_clear)

        fun bind(text: String) {
            textView.text = text
            itemView.setOnClickListener {
                fragment.hideKeyboardAndClearFocus()
            }

            imageButtonClear.setOnClickListener {
                VibrationHelper.vibrateOnClick(viewModel)
                DialogHelper.setAndShowDefaultDialog(
                    context = context,
                    viewModel = viewModel,
                    messageText = context.getString(
                        R.string.confirm_clear_something,
                        text,
                    ),
                    positiveAction = {
                        fragment.clearSearchHistoryOrSuggestion(text)
                    },
                )
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            ViewTypes.InstalledAppList.APP -> AppViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_installed_app, parent, false),
                viewType,
            )

            ViewTypes.InstalledAppList.NO_RESULT -> NoResultViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_setting_title, parent, false),
            )

            ViewTypes.InstalledAppList.HISTORY_OR_SUGGESTION -> HistoryOrSuggestionViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_search_history_or_suggestion, parent, false),
            )

            ViewTypes.InstalledAppList.SEARCH_TITLE -> SearchTitleViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_search_title, parent, false),
            )

            else -> throw IllegalArgumentException("非法视图类型：$viewType")
        }
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = dataList[position]

        when(holder) {
            is AppViewHolder -> {
                holder.bind(data as InstalledApp)
            }

            is NoResultViewHolder -> {
                holder.bind(data as String)
            }

            is HistoryOrSuggestionViewHolder -> {
                holder.bind(data as String)
            }

            is SearchTitleViewHolder -> {
                holder.bind(data as String)
            }
        }

    }


    // 设置透明度
    private fun setDimmingEffect(holder: AppViewHolder, shouldDim: Boolean) {
        // 调整亮度为原来的比例(透明度)
        val alpha = UI.Alpha.ALPHA_7
        val originAlpha = UI.Alpha.ALPHA_10

        if(shouldDim) {
            holder.itemView.alpha = alpha
        } else {
            holder.itemView.alpha = originAlpha
        }
    }


//    // 设置线的背景
//    private fun setLineBackground(
//        holder: AppViewHolder,
//        color: Drawable? = ContextCompat.getDrawable(context, R.color.mid_gray)
//    ) {
//        holder.viewLeft.background = color
//        holder.viewRight.background = color
//    }


    override fun getItemCount(): Int = dataList.size


    override fun getItemViewType(position: Int): Int {
        val data = dataList[position]
//        LogHelper.e("数据类型", "position: $position, data: $data, type: ${data::class.java}")
        return when(data) {
            is InstalledApp -> {
                ViewTypes.InstalledAppList.APP
            }

            is String -> {
                when(data) {
                    context.getString(R.string.no_result) -> {
                        ViewTypes.InstalledAppList.NO_RESULT
                    }

                    context.getString(R.string.search_history),
                    context.getString(R.string.search_suggestion),
                        -> {
                        ViewTypes.InstalledAppList.SEARCH_TITLE
                    }

                    else -> {
                        ViewTypes.InstalledAppList.HISTORY_OR_SUGGESTION
                    }
                }
            }

            else -> {
                -1
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newInstalledAppList: List<Any> = loadActivityList()) {
        dataList = newInstalledAppList
        notifyDataSetChanged()
    }


    private fun loadActivityList(): List<InstalledApp> {
        return viewModel.installedAppList.value ?: mutableListOf()
    }
}
