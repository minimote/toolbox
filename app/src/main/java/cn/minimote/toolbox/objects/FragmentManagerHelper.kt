/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.objects

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import cn.minimote.toolbox.R
import cn.minimote.toolbox.view_model.ActivityViewModel


object FragmentManagerHelper {
    // 替换 Fragment
    fun replaceFragment(
        fragmentManager: FragmentManager,
        containerId: Int = R.id.constraintLayout_origin,
        fragment: Fragment,
        addToBackStack: Boolean = true,
        tag: String? = fragment::class.java.simpleName,
        viewModel: ActivityViewModel,
    ) {
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(containerId, fragment, tag)
        if(addToBackStack) {
            transaction.addToBackStack(tag)
            viewModel.updateFragmentName(tag)
        }
        // 使用 commitAllowingStateLoss 避免状态丢失问题
        transaction.commitAllowingStateLoss()
        Log.i("Fragment 栈", "进栈: $tag")
        printBackStackEntries(fragmentManager)
    }


    // 弹出顶部的 Fragment
    fun popFragment(
        fragmentManager: FragmentManager,
        viewModel: ActivityViewModel,
        activity: AppCompatActivity,
    ) {
        // 栈底是 WidgetListFragment，不用再弹出了
        if(fragmentManager.backStackEntryCount > 1) {
            // 更新为顶部第 2 个 Fragment 名称
            val tag = getNameAtTopN(
                fragmentManager = fragmentManager,
                n = 2,
            )
            viewModel.updateFragmentName(tag)
            fragmentManager.popBackStack()
            Log.i("Fragment 栈", "出栈: $tag")
            printBackStackEntries(fragmentManager)
        } else {
            activity.finish()
        }
    }


    // 获取返回栈顶部的第 n 个 Fragment 名称
    private fun getNameAtTopN(
        fragmentManager: FragmentManager,
        n: Int,
    ): String? {
        val backStackEntryCount = fragmentManager.backStackEntryCount
        if(backStackEntryCount >= n) {
            val topEntry = fragmentManager.getBackStackEntryAt(backStackEntryCount - n)
            return topEntry.name
        }
        return null
    }


    // 打印返回栈中的所有条目
    private fun printBackStackEntries(fragmentManager: FragmentManager) {
        for(i in 0 until fragmentManager.backStackEntryCount) {
            val entry = fragmentManager.getBackStackEntryAt(i)
            Log.i("栈内元素", "$i: ${entry.name}")
        }
    }
}