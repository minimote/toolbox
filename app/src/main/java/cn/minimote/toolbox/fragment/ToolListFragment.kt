/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.adapter.ToolListAdapter
import cn.minimote.toolbox.constant.DeviceType
import cn.minimote.toolbox.constant.ToolList
import cn.minimote.toolbox.dataClass.ExpandableGroup
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.MyViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ToolListFragment(
    val isSchemeList: Boolean = false,
) : Fragment() {

    private val viewModel: MyViewModel by activityViewModels()

    private val myActivity get() = requireActivity() as MainActivity

    val viewPager: ViewPager2
        get() = myActivity.viewPager
    val constraintLayoutOrigin: ConstraintLayout
        get() = myActivity.constraintLayoutOrigin
    private lateinit var expandableListView: ExpandableListView
    private lateinit var adapter: ToolListAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_tool_list, container, false)
        expandableListView = fragmentView.findViewById(R.id.expandableListView)

        var groupList = if(viewModel.isWatch) {
            ToolList.getToolListByDeviceType(myActivity, DeviceType.WATCH)
        } else {
            ToolList.getToolListByDeviceType(myActivity, DeviceType.PHONE)
        }
        // Scheme 页面去除掉顶部的添加本机软件
        if(isSchemeList) {
            groupList = groupList.drop(1)
        }

        adapter = ToolListAdapter(
            myActivity = myActivity,
            groupList = groupList,
            viewModel = viewModel,
            fragment = this,
            fragmentManager = myActivity.supportFragmentManager,
            viewPager = viewPager,
            isSchemeList = isSchemeList,
        )
        expandableListView.setAdapter(adapter)


        // 展开指定组
        for(groupPosition in 0 until adapter.groupCount) {
            val group = adapter.getGroup(groupPosition)
            if(group is ExpandableGroup && group.isExpanded) {
                expandableListView.expandGroup(groupPosition)
            }
        }

        //  组的展开
        expandableListView.setOnGroupExpandListener {
            VibrationHelper.vibrateOnClick(viewModel)
        }

        // 组的折叠
        expandableListView.setOnGroupCollapseListener {
            VibrationHelper.vibrateOnClick(viewModel)
        }

        return fragmentView
    }

}