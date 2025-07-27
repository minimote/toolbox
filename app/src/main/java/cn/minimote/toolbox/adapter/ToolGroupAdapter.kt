/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.constant.MenuList
import cn.minimote.toolbox.dataClass.Tool
import cn.minimote.toolbox.fragment.ToolListFragment
import cn.minimote.toolbox.helper.BottomSheetDialogHelper
import cn.minimote.toolbox.helper.ClipboardHelper
import cn.minimote.toolbox.helper.LaunchHelper
import cn.minimote.toolbox.helper.SchemeHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.MyViewModel
import com.google.android.flexbox.FlexboxLayoutManager


class ToolGroupAdapter(
    private var toolList: List<Tool>,
    private val myActivity: MainActivity,
    private val viewModel: MyViewModel,
    private val fragment: ToolListFragment,
    private val fragmentManager: FragmentManager,
    val viewPager: ViewPager2,
    val isSchemeList: Boolean = false,
) : RecyclerView.Adapter<ToolGroupAdapter.ToolViewHolder>() {

    inner class ToolViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_tool, parent, false)
        return ToolViewHolder(view)
    }


    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        val tool = toolList[position]

        val icon: ImageView = holder.itemView.findViewById(R.id.imageView_app_icon)
        val name: TextView = holder.itemView.findViewById(R.id.textView_app_name)
        val description: TextView = holder.itemView.findViewById(R.id.textView_activity_name)

        // 设置 FlexItem 属性，让子项自动填充剩余空间
        val layoutParams = holder.itemView.layoutParams as FlexboxLayoutManager.LayoutParams
        // 设置 flexGrow 为 1，使子项填充剩余空间
        layoutParams.flexGrow = 1f

        icon.setImageDrawable(viewModel.iconCacheHelper.getCircularDrawable(tool))
        name.text = tool.name
        if(isSchemeList) {
            description.text = SchemeHelper.getSchemeFromId(tool.id)
        } else {
            val descriptionText = tool.description?.trim()
            if(!descriptionText.isNullOrBlank()) {
                description.text = descriptionText
            } else {
                description.visibility = View.GONE
            }

        }

        holder.itemView.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            if(isSchemeList) {
                Toast.makeText(
                    myActivity,
                    myActivity.getString(R.string.long_press_can_copy_scheme),
                    Toast.LENGTH_SHORT,
                ).show()
            } else {
                LaunchHelper.launch(myActivity, viewModel, tool)
            }
        }

        // 取消长按震动
        holder.itemView.isHapticFeedbackEnabled = false
        holder.itemView.setOnLongClickListener { _ ->
            VibrationHelper.vibrateOnLongPress(viewModel)
            if(isSchemeList) {

                ClipboardHelper.copyToClipboard(
                    context = myActivity,
                    text = SchemeHelper.getSchemeFromId(tool.id),
                    toastString = tool.name + myActivity.getString(R.string.scheme),
                )
            } else {
                BottomSheetDialogHelper.setAndShowBottomSheetDialog(
                    context = myActivity,
                    viewModel = viewModel,
                    tool = tool,
                    menuList = if(viewModel.isWatch) MenuList.tool_watch else MenuList.tool,
                    viewPager = viewPager,
                    fragmentManager = fragmentManager,
                    constraintLayoutOrigin = fragment.constraintLayoutOrigin
                )
            }
            true
        }

    }

    override fun getItemCount(): Int = toolList.size

}
