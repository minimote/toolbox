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
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.constant.FragmentName
import cn.minimote.toolbox.dataClass.ExpandableGroup
import cn.minimote.toolbox.fragment.ToolListFragment
import cn.minimote.toolbox.helper.FragmentHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.MyViewModel
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlin.math.min


class ToolListAdapter(
    private val myActivity: MainActivity,
    private var groupList: List<ExpandableGroup>,
    private val viewModel: MyViewModel,
    private val fragment: ToolListFragment,
    private val fragmentManager: FragmentManager,
    val viewPager: ViewPager2,
    val isSchemeList: Boolean = fragment.isSchemeList,
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
                myActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.item_tool_title, parent, false)

            viewHolder.titleTextView = convertView!!.findViewById(R.id.title_name)
            viewHolder.groupIndicator = convertView.findViewById(R.id.group_indicator)
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }

        val group = getGroup(groupPosition) as? ExpandableGroup
        group?.let {
            viewHolder.titleTextView?.text = myActivity.getString(it.titleId)
            if(it.titleId == R.string.add_local_app) {
                // 如果是添加本机软件，将图标设置为加号
                viewHolder.groupIndicator?.setImageResource(R.drawable.ic_add)

                // 点击后跳转到软件列表
                convertView.setOnClickListener {
                    VibrationHelper.vibrateOnClick(viewModel)
                    FragmentHelper.switchFragment(
                        fragmentName = FragmentName.INSTALLED_APP_LIST_FRAGMENT,
                        fragmentManager = fragmentManager,
                        viewModel = viewModel,
                        viewPager = fragment.viewPager,
                        constraintLayoutOrigin = fragment.constraintLayoutOrigin,
                    )
                }
            }
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
        val context = parent?.context
        val inflater = LayoutInflater.from(context)

        val view = convertView ?: inflater.inflate(R.layout.fragment_my_list, parent, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        val toolList = groupList[groupPosition].viewTypeList
//        // 每行显示的数量
//        val spanCount = viewModel.getConfigValue(
//            if(isSchemeList) {
//                Config.ConfigKeys.SCHEME_LIST_COLUMN_COUNT
//            } else {
//                Config.ConfigKeys.TOOL_LIST_COLUMN_COUNT
//            }
//        )

//        val layoutManager = GridLayoutManager(context, spanCount as Int)
        // 使用 FlexboxLayoutManager 实现自适应列数
        val layoutManager = FlexboxLayoutManager(context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.CENTER
        layoutManager.alignItems = AlignItems.CENTER
        layoutManager.flexWrap = FlexWrap.WRAP  // 确保可以换行


        val adapter = ToolGroupAdapter(
            toolList = toolList,
            myActivity = myActivity,
            viewModel = viewModel,
            fragment = fragment,
            fragmentManager = fragmentManager,
            viewPager = viewPager,
            isSchemeList = isSchemeList,
        )

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.isNestedScrollingEnabled = false


        return view
    }


    // ViewHolder 模式用于缓存视图组件
    private class ViewHolder {
        var titleTextView: TextView? = null
        //        var imageViewAppIcon: ImageView? = null
//        var textViewAppName: TextView? = null
//        var textViewDescription: TextView? = null
        var groupIndicator: ImageView? = null
    }


    override fun getChild(groupPosition: Int, childPosition: Int): Any? {
        return groupList[groupPosition].viewTypeList
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return min(1, groupList[groupPosition].viewTypeList.size)
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

