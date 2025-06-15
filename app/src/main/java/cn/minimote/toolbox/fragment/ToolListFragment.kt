/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.fragment

import android.annotation.SuppressLint
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
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import cn.minimote.toolbox.R
import cn.minimote.toolbox.adapter.ToolListAdapter
import cn.minimote.toolbox.constant.FragmentNames
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

    private lateinit var fragmentNameObserver: Observer<String>


//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }


//    override fun onDestroy() {
//        super.onDestroy()
//
//    }


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

//        expandableListView.setOnChildClickListener(object : OnChildClickListener {
//            override fun onChildClick(
//                parent: ExpandableListView?,
//                v: View?,
//                groupPosition: Int,
//                childPosition: Int,
//                id: Long
//            ): Boolean {
//                VibrationHelper.vibrateOnClick(requireContext(), viewModel)
//                Toast.makeText(requireContext(), "点击了子项", Toast.LENGTH_SHORT).show()
//                return true
//            }
//        })

        // 设置观察者
        setObservers()

        return fragmentView
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // 移除观察者
        removeObservers()
    }


//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        // 刷新数据
////        refreshData()
//    }

    override fun onResume() {
        super.onResume()
        // 刷新数据
        refreshData()
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        removeObserver()
//    }

    // 设置观察者
    private fun setObservers() {
        fragmentNameObserver = Observer { fragmentName ->
            if(fragmentName == FragmentNames.TOOL_LIST_FRAGMENT) {
                // 切换回来的时候刷新数据，主要是从活动列表返回的时候
                refreshData()
            }
        }
        viewModel.fragmentName.observe(viewLifecycleOwner, fragmentNameObserver)
    }

    // 移除观察者
    private fun removeObservers() {
        viewModel.fragmentName.removeObserver(fragmentNameObserver)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshData() {
        // 通知适配器数据已更改
        adapter.notifyDataSetChanged()
    }
}