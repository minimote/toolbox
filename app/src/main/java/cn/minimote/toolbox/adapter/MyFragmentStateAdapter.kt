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
import cn.minimote.toolbox.constant.FragmentName
import cn.minimote.toolbox.constant.ViewPaper
import cn.minimote.toolbox.fragment.MyListFragment
import cn.minimote.toolbox.fragment.ToolListFragment
import cn.minimote.toolbox.fragment.WidgetListFragment
import cn.minimote.toolbox.viewModel.MyViewModel


class MyFragmentStateAdapter(
    fragmentActivity: FragmentActivity,
    val viewPager: ViewPager2,
    val constraintLayoutOrigin: ConstraintLayout,
    val viewModel: MyViewModel,
) : FragmentStateAdapter(fragmentActivity) {

    private val fragmentList = ViewPaper.FragmentList

//    val viewModel: ToolboxViewModel =
//        ViewModelProvider(fragmentActivity)[ToolboxViewModel::class.java]

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        return when(fragmentList[position]) {
            FragmentName.TOOL_LIST_FRAGMENT -> {
                ToolListFragment()
            }

            FragmentName.WIDGET_LIST_FRAGMENT -> {
                WidgetListFragment()
            }

            FragmentName.MY_LIST_FRAGMENT -> {
                MyListFragment()
            }

            else -> {
                throw IllegalArgumentException("非法的 fragment: ${fragmentList[position]}")
            }
        }
    }

}
