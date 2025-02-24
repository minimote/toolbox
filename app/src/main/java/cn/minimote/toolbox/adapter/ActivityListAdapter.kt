/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

// import com.heytap.wearable.support.recycler.widget.RecyclerView
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
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
import cn.minimote.toolbox.dataClass.InstalledActivity
import cn.minimote.toolbox.fragment.ActivityListFragment
import cn.minimote.toolbox.objects.VibrationHelper
import cn.minimote.toolbox.viewModel.ToolboxViewModel
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.Locale


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
        viewModel.installedActivityList.value ?: getNoResultList()


    inner class AppViewHolder(itemView: View, val viewType: Int) :
        RecyclerView.ViewHolder(itemView) {
        val textViewAppName: TextView = itemView.findViewById(R.id.app_name)

        lateinit var appIcon: ImageView
        lateinit var activityName: TextView
        lateinit var switch: SwitchMaterial

        // 左右的横线
        lateinit var viewLeft: View
        lateinit var viewRight: View

        init {
            when(viewType) {
                VIEW_TYPE_NORMAL -> {
                    appIcon = itemView.findViewById(R.id.imageView_app_icon)
                    activityName = itemView.findViewById(R.id.activity_name)
                    switch = itemView.findViewById(R.id.switch_whether_show_in_home)
                }

                VIEW_TYPE_TITLE -> {
                    viewLeft = itemView.findViewById(R.id.view_left)
                    viewRight = itemView.findViewById(R.id.view_right)
                }
            }
        }


        fun bind(activity: InstalledActivity) {
            val appName = activity.appName
            val query = viewModel.searchQuery.value.orEmpty()
            // 手表不高亮显示
            if(viewModel.isWatch() || query.isEmpty()) {
                textViewAppName.text = appName
                return
            }
            textViewAppName.text = highlightSearchTerm(appName, query)
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

        holder.itemView.setOnClickListener {
            toggleSwitch()
        }

        if(holder.viewType == VIEW_TYPE_TITLE) {
            holder.textViewAppName.text = installedActivity.appName
            // 标题都不可点击
//            holder.itemView.isClickable = false
            // 需要手动设置线条颜色，不然可能会受到无名称项的影响而变成空
            setLineBackground(holder)
//            Log.e("title", installedActivity.appName)

            when(installedActivity.appName) {
                // 无名称则不显示
                "" -> {
//                    Log.e("title", "无内容")
                    setLineBackground(holder, null)
                }

                // 无结果提示为灰色正常字体
                context.getString(R.string.no_result) -> {
                    // 设置字体样式为正常
                    holder.textViewAppName.setTypeface(null, Typeface.NORMAL)
                    // 设置字体颜色为灰色
                    holder.textViewAppName.setTextColor(context.getColor(R.color.mid_gray))
                }

                else -> {
                    // 设置字体样式为粗体
                    holder.textViewAppName.setTypeface(null, Typeface.BOLD)
                    // 设置字体颜色为白色
                    holder.textViewAppName.setTextColor(context.getColor(R.color.white))
                }

            }
            return
        }

        // 高亮显示搜索结果
        holder.bind(installedActivity)

        holder.appIcon.setImageDrawable(viewModel.getIcon(installedActivity))
        // 活动名仅显示最后一个点后面的部分
        holder.activityName.text = installedActivity.activityName.substringAfterLast('.')
        holder.switch.isChecked = viewModel.isStoredActivity(installedActivity.activityName)
//        Log.i(
//            "ActivityListAdapter",
//            " ${installedActivity.appName} ${holder.switch.isChecked}"
//        )
    }


    // 设置线的背景
    private fun setLineBackground(
        holder: AppViewHolder,
        color: Drawable? = ContextCompat.getDrawable(context, R.color.mid_gray)
    ) {
        holder.viewLeft.background = color
        holder.viewRight.background = color
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
        installedAppList = if(newInstalledAppList.isNotEmpty()) {
            // 搜索结果非空，添加一个空的活动来占位
            newInstalledAppList//.plus(viewModel.getEmptyInstalledActivity())
        } else {
            getNoResultList()
        }
        notifyDataSetChanged()
    }


    private fun getNoResultList(): List<InstalledActivity> {
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
