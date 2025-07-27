/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.UI
import cn.minimote.toolbox.constant.ViewTypes
import cn.minimote.toolbox.dataClass.InstalledApp
import cn.minimote.toolbox.fragment.InstalledAppListFragment
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.MyViewModel
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.Locale


class InstalledAppListAdapter(
    private val context: Context,
    private val fragment: InstalledAppListFragment,
    private val viewModel: MyViewModel,
) : RecyclerView.Adapter<InstalledAppListAdapter.AppViewHolder>() {


    private var installedAppList: List<InstalledApp> =
        viewModel.installedAppList.value ?: getNoResultList()


    inner class AppViewHolder(itemView: View, val viewType: Int) :
        RecyclerView.ViewHolder(itemView) {
        lateinit var appName: TextView
        lateinit var appIcon: ImageView
        lateinit var activityName: TextView
        lateinit var switch: SwitchMaterial


        init {
            when(viewType) {
                ViewTypes.InstalledAppList.NORMAL -> {
                    appName = itemView.findViewById(R.id.textView_app_name)
                    appIcon = itemView.findViewById(R.id.imageView_app_icon)
                    activityName = itemView.findViewById(R.id.textView_activity_name)
                    switch = itemView.findViewById(R.id.switch_whether_show_in_home)
                }

                ViewTypes.InstalledAppList.TITLE -> {
                    appName = itemView.findViewById(R.id.title_name)
                }
            }
        }


        fun highlightAppNameAndActivityName(activity: InstalledApp) {
            val query = viewModel.searchQuery.value.orEmpty()
            if(query.isEmpty()) {
                appName.text = activity.name
                activityName.text = activity.activityName
                return
            }
            appName.text = highlightSearchTerm(activity.name, query)
            activityName.text = highlightSearchTerm(activity.activityName, query)
        }
    }


    // 高亮显示搜索结果中的匹配项
    private fun highlightSearchTerm(text: String, query: String): SpannableString {
        val spannableString = SpannableString(text)
        val lowerCaseText = text.lowercase(Locale.getDefault())
        val lowerCaseQuery = query.lowercase(Locale.getDefault())

        var index = lowerCaseText.indexOf(lowerCaseQuery)
        while(index >= 0) {
            spannableString.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.light_blue)),
                index,
                index + query.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            index = lowerCaseText.indexOf(lowerCaseQuery, index + query.length)
        }

        return spannableString
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val layoutId = when(viewType) {
            ViewTypes.InstalledAppList.NORMAL -> R.layout.item_installed_app
            ViewTypes.InstalledAppList.TITLE -> R.layout.item_setting_title
            else -> -1
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return AppViewHolder(view, viewType)
    }


    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val installedApp = installedAppList[position]

        if(holder.viewType == ViewTypes.InstalledAppList.TITLE) {
            holder.appName.text = installedApp.name
            // 标题都不可点击
//            holder.itemView.isClickable = false
            // 需要手动设置线条颜色，不然可能会受到无名称项的影响而变成空
//            setLineBackground(holder)
//            Log.e("title", installedActivity.appName)

            when(installedApp.name) {
                // 无名称则不显示
                "" -> {
//                    Log.e("title", "无内容")
//                    setLineBackground(holder, null)
                }

                // 无结果提示为灰色正常字体
                context.getString(R.string.no_result) -> {
                    // 设置字体样式为正常
                    holder.appName.setTypeface(null, Typeface.NORMAL)
                    // 设置字体颜色为灰色
                    holder.appName.setTextColor(context.getColor(R.color.mid_gray))
                }

                else -> {
                    // 设置字体样式为粗体
                    holder.appName.setTypeface(null, Typeface.BOLD)
                    // 设置字体颜色为白色
                    holder.appName.setTextColor(context.getColor(R.color.white))
                }

            }
            return
        }

        holder.appIcon.setImageDrawable(
            viewModel.iconCacheHelper.getCircularDrawable(installedApp)
        )

        if(viewModel.isWatch) {
            holder.appName.text = installedApp.name
            // 手表活动名仅显示最后一个点后面的部分
            holder.activityName.text = installedApp.activityName.substringAfterLast('.')
        } else {
            // 高亮显示搜索结果
            holder.highlightAppNameAndActivityName(installedApp)
        }

//        holder.switch.isChecked = viewModel.isStoredActivity(installedActivity.activityName)
        holder.switch.isChecked = viewModel.inSizeChangeMap(installedApp.activityName)
        // 设置暗淡效果的滤镜
        setDimmingEffect(holder, !holder.switch.isChecked)

//        Log.i(
//            "ActivityListAdapter",
//            " ${installedActivity.appName} ${holder.switch.isChecked}"
//        )

        // 项目被点到时切换开关状态
        fun toggleSwitch() {
            val searchBoxText = fragment.searchBox.text.toString().trim()
            if(viewModel.searchMode.value == true && searchBoxText.isEmpty()) {
                // 直接退出模式，而不是通过取消按钮退出，避免振动
                fragment.exitSearchMode()
            } else {
                if(holder.viewType == ViewTypes.InstalledAppList.NORMAL) {
                    holder.switch.isChecked = !holder.switch.isChecked

                    // 触发设备振动
                    VibrationHelper.vibrateOnClick(viewModel)

                    // 设置暗淡效果的滤镜
                    setDimmingEffect(holder, !holder.switch.isChecked)

                    // 根据开关状态更新字典
                    if(holder.switch.isChecked) {
                        viewModel.addToSizeChangeMap(installedApp)
                    } else {
                        viewModel.removeFromSizeChangeMap(installedApp.id)
                    }
                    // 如果修改后的关键字集合与原来的一样，复制映射
                    viewModel.syncSizeChangeMapIfSameKeys()
                }
            }
        }

        holder.itemView.setOnClickListener {
            toggleSwitch()
        }
    }


    // 设置透明度
    private fun setDimmingEffect(holder: AppViewHolder, shouldDim: Boolean) {
        // 调整亮度为原来的比例(透明度)
        val alpha = UI.ALPHA_7
        val originAlpha = UI.ALPHA_10

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


    override fun getItemCount(): Int = installedAppList.size


    override fun getItemViewType(position: Int): Int {
        return if(installedAppList[position].packageName.isEmpty()) {
            ViewTypes.InstalledAppList.TITLE
        } else {
            ViewTypes.InstalledAppList.NORMAL
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newInstalledAppList: List<InstalledApp> = loadActivityList()) {
        installedAppList = newInstalledAppList.ifEmpty {
            getNoResultList()
        }
        notifyDataSetChanged()
    }


    private fun getNoResultList(): List<InstalledApp> {
        return mutableListOf(
            InstalledApp(
                name = context.getString(R.string.no_result),
                packageName = "",
                activityName = "",
            )
        )
    }

    private fun loadActivityList(): List<InstalledApp> {
        return viewModel.installedAppList.value ?: mutableListOf()
    }
}
