/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import cn.minimote.toolbox.constant.FragmentNames
import cn.minimote.toolbox.constant.ViewPaper
import cn.minimote.toolbox.fragment.MyListFragment
import cn.minimote.toolbox.fragment.ToolListFragment
import cn.minimote.toolbox.fragment.WidgetListFragment
import cn.minimote.toolbox.viewModel.ToolboxViewModel


class ToolboxFragmentStateAdapter(
    fragmentActivity: FragmentActivity,
    val viewPager: ViewPager2,
    val constraintLayoutOrigin: ConstraintLayout,
    val viewModel: ToolboxViewModel,
) : FragmentStateAdapter(fragmentActivity) {

    private val fragmentList = ViewPaper.FragmentList

//    val viewModel: ToolboxViewModel =
//        ViewModelProvider(fragmentActivity)[ToolboxViewModel::class.java]

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        return when(fragmentList[position]) {
            FragmentNames.TOOL_LIST_FRAGMENT -> {
                ToolListFragment(
                    constraintLayoutOrigin = constraintLayoutOrigin,
                    viewPager = viewPager
                )
            }

            FragmentNames.WIDGET_LIST_FRAGMENT -> {
                WidgetListFragment(
                    viewPager = viewPager,
                    constraintLayoutOrigin = constraintLayoutOrigin,
                )
            }

            FragmentNames.MY_LIST_FRAGMENT -> {
                MyListFragment(
                    viewPager = viewPager,
                    constraintLayoutOrigin = constraintLayoutOrigin,
                )
            }

            else -> {
                throw IllegalArgumentException("非法的 fragment: $fragmentList[position]")
            }
        }
    }

}
