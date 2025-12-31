/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.adapter.MyFragmentStateAdapter
import cn.minimote.toolbox.constant.ViewPaper
import cn.minimote.toolbox.helper.DimensionHelper.getLayoutSize
import cn.minimote.toolbox.helper.FragmentHelper
import cn.minimote.toolbox.helper.LogHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.pageTransformer.ViewPager2Transformer
import cn.minimote.toolbox.viewModel.MyViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : Fragment() {

    private val viewModel: MyViewModel by activityViewModels()

    private val myActivity get() = requireActivity() as MainActivity

    lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    // 观察者
    private lateinit var multiSelectModeObserver: Observer<Boolean>
    private lateinit var sortModeObserver: Observer<Boolean>


    override fun onCreate(savedInstanceState: Bundle?) {
        LogHelper.e("函数onCreate", "")
        super.onCreate(savedInstanceState)
        setupObservers()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        LogHelper.e("函数onCreateView", "")
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        setupViewPager(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogHelper.e("函数onViewCreated", "")
        super.onViewCreated(view, savedInstanceState)
    }


    override fun onDestroy() {
        LogHelper.e("函数onDestroy", "")
        super.onDestroy()
        // 移除观察者
        removeObservers()
    }


    override fun onResume() {
        LogHelper.e("函数onResume", "")
        super.onResume()
        // 使用 post 方法将 setFirstPage 推迟到下一个消息循环执行
        viewPager.post {
            setFirstPage()
        }
    }


    // 设置 ViewPager
    private fun setupViewPager(view: View) {

        viewPager = view.findViewById(R.id.viewPager_origin)
        tabLayout = view.findViewById(R.id.tabLayout)

        myActivity.viewPager = viewPager

        val adapter = MyFragmentStateAdapter(
            fragmentActivity = myActivity,
        )
        viewPager.adapter = adapter

//        // 设置缓存页面数量
//        viewPager.offscreenPageLimit = fragmentList.size


        // 关联 ViewPager2 和 TabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // 设置每个 Tab 的标题或图标
            tab.text = FragmentHelper.getFragmentNameString(
                context = myActivity,
                fragmentName = ViewPaper.fragmentList[position],
            )

            tab.customView = layoutInflater.inflate(
                if(viewModel.isWatch) {
                    R.layout.layout_tab_watch
                } else {
                    R.layout.layout_tab_phone
                },
                tabLayout,
                false,
            )

            setTab(
                tab = tab,
                selected = false,
            )

            tab.view.isClickable = false
            tab.view.isLongClickable = false
            tab.view.isHapticFeedbackEnabled = false


            // 设置点击事件
            tab.customView?.setOnClickListener {
                val currentPosition = tabLayout.selectedTabPosition

                VibrationHelper.vibrateOnClick(viewModel)
                // 编辑模式下不允许切换页面
                if(viewModel.multiselectMode.value == true) {
                    Toast.makeText(
                        myActivity,
                        getString(R.string.multiselect_mode_dont_allow_page_switch),
                        Toast.LENGTH_SHORT,
                    ).show()
                } else if(viewModel.sortMode.value == true) {
                    Toast.makeText(
                        myActivity,
                        getString(R.string.sort_mode_dont_allow_page_switch),
                        Toast.LENGTH_SHORT,
                    ).show()
                } else {

                    // 检查是否需要关闭搜索模式
                    tryToExitSearchMode(position)

                    // 更新上一次选中的位置
                    viewModel.updateLastSelectedPosition(position)

//                    if(viewModel.isWatch) {
//                        Toast.makeText(
//                            myActivity,
//                            tab.text,
//                            Toast.LENGTH_SHORT,
//                        ).show()
//                    }

                    // 手动切换页面，不使用切换动画
                    if(currentPosition != position) {
                        viewPager.setCurrentItem(position, false)
                    }
                }
            }


            tab.customView?.setOnLongClickListener {
                // 长按 tab 的振动
                VibrationHelper.vibrateOnLongPress(viewModel)
                Toast.makeText(
                    myActivity,
                    tab.text,
                    Toast.LENGTH_SHORT,
                ).show()
                true
            }

        }.attach()


        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            // 选中时
            override fun onTabSelected(tab: TabLayout.Tab) {
                LogHelper.e("Tab选中：${tab.text}", "onTabSelected")
                setTab(
                    tab = tab,
                    selected = true,
                )
            }

            // 未选中时
            override fun onTabUnselected(tab: TabLayout.Tab) {
                LogHelper.e("Tab取消选中：${tab.text}", "onTabUnselected")
                setTab(
                    tab = tab,
                    selected = false,
                )
            }

            // 再次选中时
            override fun onTabReselected(tab: TabLayout.Tab) {
                LogHelper.e("Tab再次选中：${tab.text}", "onTabReselected")
                setTab(
                    tab = tab,
                    selected = true,
                )
            }
        })


        // 设置手表
        if(viewModel.isWatch) {
            // 手表使用小圆点，并且居中

            tabLayout.tabGravity = TabLayout.GRAVITY_CENTER
            // 设置 TabLayout 的高度
            tabLayout.layoutParams.height = getLayoutSize(
                myActivity, R.dimen.layout_size_1_small
            )
            tabLayout.layoutParams.width = getLayoutSize(
                myActivity, R.dimen.layout_size_2_5_large
            )

        } else { // 非手表设备

            // 设置页面切换动画
            viewPager.setPageTransformer(ViewPager2Transformer())

        }


        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                LogHelper.e(
                    "选中页面：${
                        FragmentHelper.getFragmentNameString(
                            myActivity,
                            ViewPaper.fragmentList[position]
                        )
                    }", "onPageSelected"
                )
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                when(state) {
                    // 用户开始拖拽滚动时
                    ViewPager2.SCROLL_STATE_DRAGGING -> {
                    }

                    // 页面正在自动滚动到最终位置
                    ViewPager2.SCROLL_STATE_SETTLING -> {
//                            Toast.makeText(myActivity,"上次项${viewModel.getLastSelectedPosition()},当前项${viewPager.currentItem}", Toast.LENGTH_SHORT).show()
//                            if(viewPager.currentItem != viewModel.getLastSelectedPosition()) {
                        VibrationHelper.vibrateOnViewPagerScroll(viewModel)
//                            }
                    }

                    // 滚动结束时
                    ViewPager2.SCROLL_STATE_IDLE -> {

                        tryToExitSearchMode(viewPager.currentItem)

                        // 更新上一次选中的位置
                        viewModel.updateLastSelectedPosition(viewPager.currentItem)
                    }
                }
            }
        })

        setFirstPage()

    }


    // 设置首页
    private fun setFirstPage() {
        val startFragmentPos = viewModel.getLastSelectedPosition()
        LogHelper.e(
            "设置起始页：${
                FragmentHelper.getFragmentNameString(
                    myActivity,
                    ViewPaper.fragmentList[startFragmentPos],
                )
            }",
            startFragmentPos.toString(),
        )

//        viewPager.alpha = 0f
        // 设置 ViewPager 默认显示页面(必须在 TabLayoutMediator.attach() 之后)
        viewPager.setCurrentItem(
            startFragmentPos, false,
        )
        tabLayout.selectTab(tabLayout.getTabAt(startFragmentPos))
//        viewPager.alpha = 1f
//        for(i in 0 until tabLayout.tabCount) {
//            tabLayout.getTabAt(i)?.let { tab ->
//                setTab(
//                    tab = tab,
//                    selected = i == startFragmentPos,
//                )
//            }
//        }
    }


    private fun setTab(
        tab: TabLayout.Tab,
        selected: Boolean,
    ) {
        LogHelper.e("设置Tab：${tab.text},选中：${selected}", "setTab")
        if(viewModel.isWatch) {
            val viewDot = tab.customView?.findViewById<View>(R.id.view_dot)
            viewDot?.background = ContextCompat.getDrawable(
                myActivity,
                if(selected) {
                    R.drawable.background_tab_layout_dot_selected
                } else {
                    R.drawable.background_tab_layout_dot_unselected
                },
            )
        } else {
            val textViewName = tab.customView?.findViewById<TextView>(R.id.textView_name)
            textViewName?.apply {
                text = tab.text
                setTextColor(
                    myActivity.getColor(
                        if(selected) {
                            R.color.text_white
                        } else {
                            R.color.mid_gray
                        },
                    )
                )
            }
        }
    }


    private fun tryToExitSearchMode(position: Int) {
//        Toast.makeText(
//            myActivity,
//            "搜索模式${viewModel.searchModeToolList.value},[当前${position},上次${viewModel.getLastSelectedPosition()}]",
//            Toast.LENGTH_SHORT,
//        ).show()
        if(viewModel.searchModeToolList.value == true &&
            position != viewModel.getLastSelectedPosition()
        ) {
            viewModel.searchModeToolList.value = false
        }
    }


    // 更新 viewPager.isUserInputEnabled
    private fun updateViewPagerUserInputEnabled(enable: Boolean) {
        viewPager.isUserInputEnabled = enable
    }


    // 设置观察者
    private fun setupObservers() {

        multiSelectModeObserver = Observer { multiSelectMode ->
            // 非多选模式才允许滑动
            updateViewPagerUserInputEnabled(!multiSelectMode)
        }
        viewModel.multiselectMode.observe(this, multiSelectModeObserver)


        sortModeObserver = Observer { sortMode ->
            // 非排序模式才允许滑动
            updateViewPagerUserInputEnabled(!sortMode)
        }
        viewModel.sortMode.observe(this, sortModeObserver)

    }


    // 移除观察者
    private fun removeObservers() {
        viewModel.multiselectMode.removeObserver(multiSelectModeObserver)
        viewModel.sortMode.removeObserver(sortModeObserver)
    }
}