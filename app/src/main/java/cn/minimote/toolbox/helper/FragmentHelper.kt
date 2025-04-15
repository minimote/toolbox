/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getString
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2
import cn.minimote.toolbox.R
import cn.minimote.toolbox.fragment.AboutProjectFragment
import cn.minimote.toolbox.fragment.ActivityListFragment
import cn.minimote.toolbox.fragment.EditListFragment
import cn.minimote.toolbox.fragment.SettingFragment
import cn.minimote.toolbox.fragment.SupportAuthorFragment
import cn.minimote.toolbox.fragment.WebViewFragment
import cn.minimote.toolbox.viewModel.ToolboxViewModel
import cn.minimote.toolbox.viewModel.ToolboxViewModel.Companion.FragmentNames


object FragmentHelper {

    // 不需要新建的 Fragment
    private val nonCreatableFragments = setOf(
        FragmentNames.WIDGET_LIST_FRAGMENT,
    )

    fun switchFragment(
        fragmentName: String,
        fragmentManager: FragmentManager,
        viewModel: ToolboxViewModel,
        viewPager: ViewPager2? = null,
        constraintLayoutOrigin: ConstraintLayout? = null,
        containerId: Int = R.id.constraintLayout_origin,
    ) {
        if(fragmentName !in nonCreatableFragments) {
            val transaction = fragmentManager.beginTransaction()
            val fragment = when(fragmentName) {
                FragmentNames.ACTIVITY_LIST_FRAGMENT -> {
                    ActivityListFragment()
                }

                FragmentNames.EDIT_LIST_FRAGMENT -> {
                    EditListFragment()
                }

                FragmentNames.SUPPORT_AUTHOR_FRAGMENT -> {
                    SupportAuthorFragment()
                }

                FragmentNames.ABOUT_PROJECT_FRAGMENT -> {
                    AboutProjectFragment()
                }

                FragmentNames.SETTING_FRAGMENT -> {
                    SettingFragment()
                }

                FragmentNames.WEB_VIEW_FRAGMENT -> {
                    WebViewFragment()
                }

                else -> {
                    throw IllegalArgumentException("非法的 Fragment 名称: $fragmentName")
                }
            }

            transaction.replace(containerId, fragment, fragmentName)
            transaction.addToBackStack(fragmentName)
            // 使用 commitAllowingStateLoss 避免状态丢失问题
            transaction.commitAllowingStateLoss()

            showViewPager(
                viewPager = viewPager!!,
                constraintLayoutOrigin = constraintLayoutOrigin!!,
                showViewPager = false,
            )

            viewModel.updateFragmentName(fragmentName)
        }
    }

    // 返回 Fragment
    fun returnFragment(
        fragmentManager: FragmentManager,
        viewModel: ToolboxViewModel,
        activity: AppCompatActivity,
        viewPager: ViewPager2? = null,
        constraintLayoutOrigin: ConstraintLayout? = null,
    ) {
        val fragmentName = viewModel.getFragmentName()
//        Log.e("returnFragment", fragmentName)
        when(fragmentName) {
            FragmentNames.WIDGET_LIST_FRAGMENT -> {
                // 退出编辑模式
                if(viewModel.editMode.value == true) {
                    viewModel.editMode.value = false
                    Toast.makeText(
                        activity,
                        getString(activity, R.string.exit_edit_mode),
                        Toast.LENGTH_SHORT,
                    ).show()
                    return
                }
            }

            FragmentNames.ACTIVITY_LIST_FRAGMENT -> {
                // 如果处于搜索模式，则仅退出搜索模式
                if(viewModel.searchMode.value == true) {
                    viewModel.searchMode.value = false
                    return
                }
            }

            FragmentNames.EDIT_LIST_FRAGMENT -> {
                // 从编辑界面返回时剩余组件为空，则退出编辑模式
                if(viewModel.storedActivityList.value?.size == 0) {
                    viewModel.editMode.value = false
                    Toast.makeText(
                        activity,
                        getString(activity, R.string.toast_no_widget_return_home),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }


//        viewModel.widgetListSizeWasModified.value = false
        if(fragmentManager.backStackEntryCount > 0) {
//            // 更新为顶部第 2 个 Fragment 名称
//            val tag = getNameAtTopN(
//                viewModel = viewModel,
//                fragmentManager = fragmentManager,
//            )
//            viewModel.updateFragmentName(tag)
            fragmentManager.popBackStack()

            showViewPager(
                viewPager = viewPager!!,
                constraintLayoutOrigin = constraintLayoutOrigin!!,
                showViewPager = true,
            )
//            Log.e("FragmentHelper", fragmentName)
            // 返回组件列表
            viewModel.updateFragmentName(FragmentNames.WIDGET_LIST_FRAGMENT)
        } else {
            activity.finish()
        }
    }


    // 显示 ViewPager
    private fun showViewPager(
        viewPager: ViewPager2,
        constraintLayoutOrigin: ConstraintLayout,
        showViewPager: Boolean,
    ) {
        if(showViewPager) {
            viewPager.visibility = View.VISIBLE
            constraintLayoutOrigin.visibility = View.INVISIBLE
//            val adapter = viewPager.adapter as ToolboxFragmentStateAdapter
//            adapter.updateFragmentList()
//            Log.e("showViewPager", "重新加载")
        } else {
            viewPager.visibility = View.INVISIBLE
            constraintLayoutOrigin.visibility = View.VISIBLE
        }
    }


//    // 获取返回栈顶部的第 n 个 Fragment 名称
//    private fun getNameAtTopN(
//        fragmentManager: FragmentManager,
//        n: Int = 2,
//    ): String? {
//        val backStackEntryCount = fragmentManager.backStackEntryCount
//        if(backStackEntryCount >= n) {
//            val topEntry = fragmentManager.getBackStackEntryAt(backStackEntryCount - n)
//            return topEntry.name
//        }
//        return fragmentNames.NO_FRAGMENT
//    }
}