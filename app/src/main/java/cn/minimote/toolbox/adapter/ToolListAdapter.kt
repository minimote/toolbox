/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.dataClass.ExpandableGroup
import cn.minimote.toolbox.dataClass.ToolActivity
import cn.minimote.toolbox.helper.ActivityLaunchHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.ToolboxViewModel


class ToolListAdapter(
    private val context: Context,
    private var groupList: List<ExpandableGroup>,
    private val viewModel: ToolboxViewModel,
) : BaseExpandableListAdapter() {

    override fun getGroupView(
        groupPosition: Int, isExpanded: Boolean,
        convertView: View?, parent: ViewGroup?
    ): View {
        var convertView = convertView
        val viewHolder: ViewHolder

        // 复用已有 convertView 并使用 ViewHolder 缓存
        if(convertView == null) {
            viewHolder = ViewHolder()
            val layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.item_tool_title, parent, false)

            viewHolder.titleTextView = convertView!!.findViewById(R.id.title_name)
            viewHolder.groupIndicator = convertView.findViewById(R.id.group_indicator)
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }

        val group = getGroup(groupPosition) as? ExpandableGroup
        group?.let {
            viewHolder.titleTextView?.text = context.getString(it.titleId)
        }

        // 设置分组指示器状态
        viewHolder.groupIndicator?.apply {
            isSelected = isExpanded // 使用 isSelected 控制图标切换
        }

        return convertView
    }

    override fun getChildView(
        groupPosition: Int, childPosition: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup?
    ): View {
        var convertView = convertView
        val viewHolder: ViewHolder

        if(convertView == null) {
            viewHolder = ViewHolder()
            val layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.item_tool, parent, false)

            viewHolder.imageViewAppIcon = convertView!!.findViewById(R.id.imageView_app_icon)
            viewHolder.textViewAppName = convertView.findViewById(R.id.textView_app_name)
            viewHolder.textViewDescription = convertView.findViewById(R.id.textView_description)
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }

        val child = getChild(groupPosition, childPosition) as ToolActivity
        viewHolder.imageViewAppIcon?.setImageDrawable(viewModel.iconCacheHelper.getIcon(child))
        viewHolder.textViewAppName?.text = child.appName
        viewHolder.textViewDescription?.let { textView ->
            if(child.description.isBlank()) {
                textView.visibility = View.GONE
            } else {
                textView.text = child.description
            }
        }


        convertView.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel.myContext, viewModel)
            ActivityLaunchHelper.launch(viewModel.myContext, child)
        }


        return convertView
    }

    // ViewHolder 模式用于缓存视图组件
    private class ViewHolder {
        var titleTextView: TextView? = null
        var imageViewAppIcon: ImageView? = null
        var textViewAppName: TextView? = null
        var textViewDescription: TextView? = null
        var groupIndicator: ImageView? = null
    }


    override fun getChild(groupPosition: Int, childPosition: Int): Any? {
        return groupList[groupPosition].viewTypeList[childPosition]
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return groupList[groupPosition].viewTypeList.size
    }

    override fun getGroup(groupPosition: Int): Any? {
        return groupList[groupPosition]
    }

    override fun getGroupCount(): Int {
        return groupList.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }


    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}

