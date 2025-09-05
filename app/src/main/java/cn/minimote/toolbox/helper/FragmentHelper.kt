/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.getString
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.viewpager2.widget.ViewPager2
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.constant.Config
import cn.minimote.toolbox.constant.Config.ConfigValues.NetworkAccessModeValues
import cn.minimote.toolbox.constant.FragmentName
import cn.minimote.toolbox.constant.NetworkType
import cn.minimote.toolbox.constant.ViewPaper.FragmentList
import cn.minimote.toolbox.constant.ViewPaper.START_VIEW_POS
import cn.minimote.toolbox.fragment.AboutProjectFragment
import cn.minimote.toolbox.fragment.DetailListFragment
import cn.minimote.toolbox.fragment.EasterEggFragment
import cn.minimote.toolbox.fragment.EditListFragment
import cn.minimote.toolbox.fragment.InstalledAppListFragment
import cn.minimote.toolbox.fragment.SchemeListFragment
import cn.minimote.toolbox.fragment.SettingFragment
import cn.minimote.toolbox.fragment.SupportAuthorFragment
import cn.minimote.toolbox.fragment.WebViewFragment
import cn.minimote.toolbox.fragment.WoodenFishFragment
import cn.minimote.toolbox.helper.ConfigHelper.getConfigValue
import cn.minimote.toolbox.helper.NetworkHelper.getNetworkAccessMode
import cn.minimote.toolbox.viewModel.MyViewModel


object FragmentHelper {

    // 不需要新建的 Fragment
    private val nonCreatableFragments = setOf(
        FragmentName.WIDGET_LIST_FRAGMENT,
    )

    fun switchFragment(
        fragmentName: String,
        viewModel: MyViewModel,
        activity: MainActivity,
    ) {
        val context = activity
        if(fragmentName !in nonCreatableFragments) {

            val fragment = when(fragmentName) {
                FragmentName.INSTALLED_APP_LIST_FRAGMENT -> {
                    InstalledAppListFragment()
                }

                FragmentName.EDIT_LIST_FRAGMENT -> {
                    EditListFragment()
                }

                FragmentName.SUPPORT_AUTHOR_FRAGMENT -> {
                    SupportAuthorFragment()
                }

                FragmentName.ABOUT_PROJECT_FRAGMENT -> {
                    AboutProjectFragment()
                }

                FragmentName.SETTING_FRAGMENT -> {
                    SettingFragment()
                }

                FragmentName.WEB_VIEW_FRAGMENT -> {
                    // 获取网络类型
                    val networkType = NetworkHelper.getNetworkType(viewModel.myContext)
                    // 网络未连接
                    if(networkType == NetworkType.DISCONNECTED) {
                        Toast.makeText(
                            viewModel.myContext,
                            getString(viewModel.myContext, R.string.toast_no_network),
                            Toast.LENGTH_SHORT,
                        ).show()
                        return
                    }

                    val networkTypeString = NetworkHelper.getNetworkTypeString(
                        context = viewModel.myContext,
                        networkType = networkType,
                    )

                    when(viewModel.getNetworkAccessMode(networkType)) {
                        NetworkAccessModeValues.ALERT -> {
                            DialogHelper.setAndShowDefaultDialog(
                                context = context,
                                viewModel = viewModel,
                                messageText = context.getString(
                                    R.string.dialog_message_network,
                                    networkTypeString,
                                ),
                                positiveAction = {
                                    showFragment(
                                        fragment = WebViewFragment(),
                                        fragmentName = fragmentName,
                                        viewModel = viewModel,
                                        activity = activity,
                                    )
                                }
                            )

                            return
                        }

                        NetworkAccessModeValues.DENY -> {
                            Toast.makeText(
                                viewModel.myContext,
                                viewModel.myContext.getString(
                                    R.string.toast_network_access_denied,
                                    networkTypeString,
                                ),
                                Toast.LENGTH_SHORT,
                            ).show()
                            return
                        }

                        NetworkAccessModeValues.ALLOW -> {}
                    }
                    WebViewFragment()
                }


                FragmentName.SCHEME_LIST_FRAGMENT -> {
                    SchemeListFragment()
                }

                FragmentName.DETAIL_LIST_FRAGMENT -> {
                    DetailListFragment()
                }

                FragmentName.WOODEN_FISH_FRAGMENT -> {
                    WoodenFishFragment()
                }

                FragmentName.EASTER_EGG_FRAGMENT -> {
                    EasterEggFragment()
                }

                else -> {
                    throw IllegalArgumentException("非法的 Fragment: $fragmentName")
                }
            }

            showFragment(
                fragment = fragment,
                fragmentName = fragmentName,
                viewModel = viewModel,
                activity = activity,
            )
        }
    }


    // 显示 Fragment
    fun showFragment(
        fragment: Fragment,
        fragmentName: String,
        viewModel: MyViewModel,
        activity: MainActivity,
    ) {
        val transaction = activity.supportFragmentManager.beginTransaction()
        transaction.replace(activity.containerId, fragment, fragmentName)
        transaction.addToBackStack(fragmentName)
        // 使用 commitAllowingStateLoss 避免状态丢失问题
        transaction.commitAllowingStateLoss()

        showViewPager(
            viewPager = activity.viewPager,
            constraintLayoutOrigin = activity.constraintLayoutOrigin,
            showViewPager = false,
        )

        viewModel.updateFragmentName(fragmentName)
    }


    // 返回 Fragment
    fun returnFragment(
        viewModel: MyViewModel,
        activity: MainActivity,
    ) {
        val fragmentName = viewModel.getFragmentName()
        val fragmentManager = activity.supportFragmentManager
//        Log.e("returnFragment", fragmentName)
        when(fragmentName) {
            FragmentName.WIDGET_LIST_FRAGMENT -> {
                // 退出多选模式
                if(viewModel.multiselectMode.value == true) {
                    viewModel.clearSelectedIds()
                    viewModel.multiselectMode.value = false
                    Toast.makeText(
                        activity,
                        getString(activity, R.string.exit_multiselect_mode),
                        Toast.LENGTH_SHORT,
                    ).show()
                    return
                } else if(viewModel.sortMode.value == true) {
                    viewModel.sortMode.value = false
                    Toast.makeText(
                        activity,
                        getString(activity, R.string.exit_sort_mode),
                        Toast.LENGTH_SHORT,
                    ).show()
                    return
                } else if(viewModel.searchMode.value == true) {
                    viewModel.searchMode.value = false
                    return
                }
            }

            FragmentName.INSTALLED_APP_LIST_FRAGMENT, FragmentName.SCHEME_LIST_FRAGMENT -> {
                // 如果处于搜索模式，则仅退出搜索模式
                if(viewModel.searchMode.value == true) {
                    viewModel.searchMode.value = false
                    return
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
                viewPager = activity.viewPager,
                constraintLayoutOrigin = activity.constraintLayoutOrigin,
                showViewPager = true,
            )
//            Log.e("FragmentHelper", fragmentName)
            // 返回组件列表
            viewModel.updateFragmentName(FragmentName.WIDGET_LIST_FRAGMENT)
        } else {
            activity.finish()
        }
    }


    // 显示 ViewPager
    private fun showViewPager(
        viewPager: ViewPager2,
        constraintLayoutOrigin: FragmentContainerView,
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


    // 获取 Fragment 名称
    fun getFragmentNameString(
        context: Context,
        fragmentName: String,
    ): String {
        val stringId = Config.ConfigValues.HomePage.idToStringIdMap[fragmentName]

        return when(stringId) {
            null -> {
                context.getString(R.string.unknown)
            }

            else -> {
                context.getString(stringId)
            }
        }
    }


    fun MyViewModel.getStartFragmentPos(): Int {
        val fragmentName = this.getConfigValue(Config.ConfigKeys.Launch.HOME_PAGE)
        val index = FragmentList.indexOf(fragmentName)
        return if(index == -1) START_VIEW_POS else index
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