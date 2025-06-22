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
import android.widget.ExpandableListView.OnGroupCollapseListener
import android.widget.ExpandableListView.OnGroupExpandListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import cn.minimote.toolbox.R
import cn.minimote.toolbox.adapter.ToolListAdapter
import cn.minimote.toolbox.constant.ToolList
import cn.minimote.toolbox.dataClass.ExpandableGroup
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.ToolboxViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ToolListFragment(
    val viewPager: ViewPager2,
    val constraintLayoutOrigin: ConstraintLayout,
) : Fragment() {

    private val viewModel: ToolboxViewModel by activityViewModels()
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

        adapter = ToolListAdapter(
            context = requireContext(),
            groupList = if(viewModel.isWatch) {
                ToolList.watch
            } else {
                ToolList.default
            },
            viewModel = viewModel,
            fragment = this,
            fragmentManager = requireActivity().supportFragmentManager,
            viewPager = viewPager,
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
        expandableListView.setOnGroupExpandListener(object : OnGroupExpandListener {
            override fun onGroupExpand(groupPosition: Int) {
                VibrationHelper.vibrateOnClick(requireContext(), viewModel)
            }
        })

        // 组的折叠
        expandableListView.setOnGroupCollapseListener(object : OnGroupCollapseListener {
            override fun onGroupCollapse(groupPosition: Int) {
                VibrationHelper.vibrateOnClick(requireContext(), viewModel)
            }
        })

        return fragmentView
    }

}