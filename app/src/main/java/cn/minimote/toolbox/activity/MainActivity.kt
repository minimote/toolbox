/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import cn.minimote.toolbox.R
import cn.minimote.toolbox.adapter.ToolboxFragmentStateAdapter
import cn.minimote.toolbox.constant.FragmentName
import cn.minimote.toolbox.constant.ViewPaper
import cn.minimote.toolbox.helper.CheckUpdateHelper
import cn.minimote.toolbox.helper.ConfigHelper
import cn.minimote.toolbox.helper.DialogHelper
import cn.minimote.toolbox.helper.FragmentHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.pageTransformer.ViewPager2Transformer
import cn.minimote.toolbox.viewModel.MyViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MyViewModel by viewModels()

//    private val fragmentPositions = ToolboxViewModel.Constants.FragmentPositions

    // 滑动返回的比例阈值
//    private val thresholdExit = 0.2
//
//    private var initialX = 0f
//    private var screenWidth: Int = 0

    private lateinit var buttonTime: Button
    private lateinit var buttonExit: Button
    private lateinit var buttonAdd: Button

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    private lateinit var constraintLayoutOrigin: ConstraintLayout

    // 用于更新时间
    private val handler = Handler(Looper.getMainLooper())

    private val runnable = object : Runnable {
        override fun run() {
            // 1 秒更新一次
            handler.postDelayed(this, 1000)
            // 编辑模式下显示的是排序，所以不更新时间
            if(viewModel.multiselectMode.value == true && viewModel.getFragmentName() == FragmentName.WIDGET_LIST_FRAGMENT) {
                return
            }
            updateTime()
        }
    }

    // 观察者
    private lateinit var widgetListOrderWasModifiedObserver: Observer<Boolean>
    private lateinit var widgetListSizeWasModifiedObserver: Observer<Boolean>
    private lateinit var widgetWasModifiedObserver: Observer<Boolean>

    private lateinit var multiSelectModeObserver: Observer<Boolean>
    private lateinit var sortModeObserver: Observer<Boolean>
    private lateinit var fragmentNameObserver: Observer<String>

    private lateinit var settingWasModifiedObserver: Observer<Boolean>

    private lateinit var selectedIdsObserver: Observer<MutableSet<String>>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Log.d("MainActivity", "onCreate")
//        Log.d("MainActivity", "ViewModel initialized: ${System.identityHashCode(viewModel)}")

        // 默认暗色模式(使用后会出现亮色模式无法打开的问题)
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        enableEdgeToEdge()
        setContentView(R.layout.layout_main)

        // 加载配置文件
        ConfigHelper.loadAllConfig(viewModel)

        constraintLayoutOrigin = findViewById(R.id.constraintLayout_origin)

        // 适配系统返回手势和按钮
        setupBackPressedCallback()

        // 设置观察者
        setupObservers()

        // 设置按钮
        setupButtons()

        // 设置 ViewPager
        setupViewPager()

        // 检查更新
        CheckUpdateHelper.autoCheckUpdate(
            context = this, viewModel = viewModel,
        )

    }


    override fun onDestroy() {
        super.onDestroy()
//        Log.i("MainActivity", "onDestroy")
        // 移除更新时间的任务
        handler.removeCallbacks(runnable)
//        backgroundThread.quitSafely()
        // 移除观察者
        removeObservers()
    }


//    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
//        super.onRestoreInstanceState(savedInstanceState)
//        // 恢复保存的状态
//        Log.i("MainActivity", "onRestoreInstanceState")
////        printBackStackEntries()
//    }


    // 适配系统返回手势和按钮
    private fun setupBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                FragmentHelper.returnFragment(
                    fragmentManager = supportFragmentManager,
                    viewModel = viewModel,
                    activity = this@MainActivity,
                    viewPager = viewPager,
                    constraintLayoutOrigin = constraintLayoutOrigin,
                )
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }


    // 设置按钮
    private fun setupButtons() {
        if(viewModel.isWatch) {
            // 将手表顶部的顶部按钮间距设为 30dp
            val guideline = findViewById<Guideline>(R.id.guideline4)
            guideline.setGuidelineBegin(viewModel.dpToPx(30f))
        }

        buttonExit = findViewById(R.id.button_exit)
        buttonExit.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
//            returnOrExit()
            FragmentHelper.returnFragment(
                fragmentManager = supportFragmentManager,
                viewModel = viewModel,
                activity = this@MainActivity,
                viewPager = viewPager,
                constraintLayoutOrigin = constraintLayoutOrigin,
            )
        }


        buttonAdd = findViewById(R.id.button_add)
        buttonAdd.text = getString(R.string.save_button)

        buttonAdd.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            when(viewModel.getFragmentName()) {
                FragmentName.WIDGET_LIST_FRAGMENT -> {
                    if(viewModel.multiselectMode.value == true) {
                        DialogHelper.showConfirmDialog(
                            context = this,
                            viewModel = viewModel,
                            titleText = getString(
                                R.string.confirm_delete_widget,
                                viewModel.getSelectedSize(),
                            ),
                            positiveAction = {
                                viewModel.deleteSelectedItemAndSave()
                                buttonAdd.visibility = View.INVISIBLE
                            },
                        )
                        return@setOnClickListener
                    }
                    if(viewModel.sortMode.value == true) {
                        viewModel.saveWidgetList()
                        buttonAdd.visibility = View.INVISIBLE
                        return@setOnClickListener
                    }
                }

                FragmentName.SETTING_FRAGMENT -> {
                    ConfigHelper.saveUserConfig(viewModel)
                    return@setOnClickListener
                }

            }

            viewModel.editedTool.value?.lastModifiedTime = System.currentTimeMillis()
            viewModel.saveWidgetList()
            Toast.makeText(
                this@MainActivity,
                getString(R.string.save_success),
                Toast.LENGTH_SHORT,
            ).show()
            buttonAdd.visibility = View.INVISIBLE
        }


        // 设置时间
        setupTimeButton()
    }


    // 设置时间
    private fun setupTimeButton() {
        buttonTime = findViewById(R.id.button_time)
//        buttonTime.setOnClickListener {
////            Log.e(viewModel.getFragmentName(), viewModel.editMode.value.toString())
//            if(viewModel.editMode.value == true && viewModel.getFragmentName() == FragmentName.WIDGET_LIST_FRAGMENT) {
//                // 组件排序
//                VibrationHelper.vibrateOnClick(this, viewModel)
//                viewModel.sortStoredActivityList()
//            }
//        }
        // 第一次更新时间
        updateTime()
        handler.post(runnable)
        // 启动后台任务
//        backgroundHandler.post(runnable)
    }


    private fun updateTime() {
        // 从资源文件中获取时间格式字符串
        val timeFormat = getString(R.string.time_format)
        // 使用 SimpleDateFormat 格式化当前时间
        buttonTime.text = SimpleDateFormat(timeFormat, Locale.getDefault()).format(Date())
    }


    // 设置 ViewPager
    private fun setupViewPager() {
        val startViewPos = ViewPaper.START_VIEW_POS

        viewPager = findViewById(R.id.viewPager_origin)
        tabLayout = findViewById(R.id.tabLayout)

        val adapter = ToolboxFragmentStateAdapter(
            fragmentActivity = this,
            viewPager = viewPager,
            constraintLayoutOrigin = constraintLayoutOrigin,
            viewModel = viewModel,
        )
        viewPager.adapter = adapter

        // 设置 ViewPager 默认显示第二个页面
        viewPager.setCurrentItem(startViewPos, false)

        // 关联 ViewPager2 和 TabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // 设置每个 Tab 的标题或图标
            tab.text = FragmentHelper.getFragmentNameString(
                context = this,
                fragmentName = ViewPaper.FragmentList[position],
            )

            // 设置点击事件
            tab.view.setOnClickListener {
                val currentPosition = tabLayout.selectedTabPosition
                if(viewModel.isWatch) {
                    // 手表没有页面切换振动，所以点击都会振动
                    VibrationHelper.vibrateOnClick(viewModel)
                } else {
                    // 手机有页面切换振动，所以点击时只有点击当前页面才振动
                    if(currentPosition == position) {
                        // 点击当前页面 tab 的振动
                        VibrationHelper.vibrateOnClick(viewModel)
                    }
                }
            }

            // 拦截 Tab 的选择事件，阻止默认切换
            tab.view.setOnTouchListener { _, event ->
                when(event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        // 模拟按下状态，触发涟漪动画
                        tab.view.isPressed = true
                        tab.view.refreshDrawableState()
                        true
                    }

                    android.view.MotionEvent.ACTION_UP -> {
                        // 模拟抬起状态，触发涟漪动画
                        tab.view.isPressed = false
                        tab.view.refreshDrawableState()

                        // 编辑模式下不允许切换页面
                        if(viewModel.multiselectMode.value == true) {
                            VibrationHelper.vibrateOnClick(viewModel)
                            Toast.makeText(
                                this@MainActivity,
                                getString(R.string.multiselect_mode_dont_allow_page_switch),
                                Toast.LENGTH_SHORT,
                            ).show()
                        } else if(viewModel.sortMode.value == true) {
                            VibrationHelper.vibrateOnClick(viewModel)
                            Toast.makeText(
                                this@MainActivity,
                                getString(R.string.sort_mode_dont_allow_page_switch),
                                Toast.LENGTH_SHORT,
                            ).show()
                        } else {
                            tab.view.performClick()
                        }
                        true
                    }

                    android.view.MotionEvent.ACTION_CANCEL -> {
                        // 模拟取消状态，触发涟漪动画
                        tab.view.isPressed = false
                        tab.view.refreshDrawableState()
                        true
                    }

                    else -> false
                }
            }

            if(viewModel.isWatch) {
                tab.customView = if(position == startViewPos) {
                    layoutInflater.inflate(R.layout.tab_layout_selected_watch, tabLayout, false)
                } else {
                    layoutInflater.inflate(R.layout.tab_layout_unselected_watch, tabLayout, false)
                }
            } else {
                tab.customView = if(position == startViewPos) {
                    layoutInflater.inflate(R.layout.tab_layout_selected_phone, tabLayout, false)
                } else {
                    layoutInflater.inflate(R.layout.tab_layout_unselected_phone, tabLayout, false)
                }

                val textViewName = tab.customView?.findViewById<android.widget.TextView>(R.id.textView_name)
                textViewName?.text = tab.text
            }
        }.attach()

        // 设置手表
        if(viewModel.isWatch) {
            // 手表使用小圆点，并且居中
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                // 选中时
                override fun onTabSelected(tab: TabLayout.Tab) {
                    tab.customView = layoutInflater.inflate(
                        R.layout.tab_layout_selected_watch, tabLayout, false
                    )
                }

                // 未选中时
                override fun onTabUnselected(tab: TabLayout.Tab) {
                    tab.customView =
                        layoutInflater.inflate(R.layout.tab_layout_unselected_watch, tabLayout, false)
                }

                // 重新选中时
                override fun onTabReselected(tab: TabLayout.Tab) {
                    tab.customView = layoutInflater.inflate(
                        R.layout.tab_layout_selected_watch, tabLayout, false
                    )
                }
            })

            tabLayout.tabGravity = TabLayout.GRAVITY_CENTER
            // 设置 TabLayout 的高度
            tabLayout.layoutParams.height =
                resources.getDimensionPixelSize(R.dimen.layout_size_1_small)
        } else { // 非手表设备
            // 设置页面切换动画
            viewPager.setPageTransformer(ViewPager2Transformer())
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                // 选中时
                override fun onTabSelected(tab: TabLayout.Tab) {
                    // 页面切换的振动
                    VibrationHelper.vibrateOnClick(viewModel)
                    tab.customView = layoutInflater.inflate(
                        R.layout.tab_layout_selected_phone, tabLayout, false
                    )
                    val textViewName = tab.customView?.findViewById<android.widget.TextView>(R.id.textView_name)
                    textViewName?.text = tab.text
                }

                // 未选中时
                override fun onTabUnselected(tab: TabLayout.Tab) {
                    tab.customView =
                        layoutInflater.inflate(R.layout.tab_layout_unselected_phone, tabLayout, false)
                    val textViewName = tab.customView?.findViewById<android.widget.TextView>(R.id.textView_name)
                    textViewName?.text = tab.text
                }

                // 重新选中时
                override fun onTabReselected(tab: TabLayout.Tab) {
                    tab.customView = layoutInflater.inflate(
                        R.layout.tab_layout_selected_phone, tabLayout, false
                    )
                    val textViewName = tab.customView?.findViewById<android.widget.TextView>(R.id.textView_name)
                    textViewName?.text = tab.text
                }
            })
        }
    }


    // 更新 viewPager.isUserInputEnabled
    private fun updateViewPagerUserInputEnabled(enable: Boolean) {
        viewPager.isUserInputEnabled = enable
//        // 同步更新 TabLayout 的点击行为
//        for(i in 0 until tabLayout.tabCount) {
//            val tab = tabLayout.getTabAt(i)
//            tab?.view?.isClickable = enable
//        }
    }


    // 设置观察者
    private fun setupObservers() {
        // 列表顺序观察者
        widgetListOrderWasModifiedObserver = Observer { widgetListOrderWasModified ->
//            Log.e("顺序观察者", "$widgetListOrderWasModified")
            if(widgetListOrderWasModified) {
                buttonAdd.visibility = View.VISIBLE
            } else {
                buttonAdd.visibility = View.INVISIBLE
            }
        }
        viewModel.toolListOrderChanged.observe(this, widgetListOrderWasModifiedObserver)


        // 列表大小观察者
        widgetListSizeWasModifiedObserver = Observer { widgetListSizeWasModified ->
            // Log.e("大小观察者", "$widgetListSizeWasModified")
            when(viewModel.getFragmentName()) {
                FragmentName.EDIT_LIST_FRAGMENT -> {
                    // 编辑列表存在大小改变和组件改变，必须同时不变才取消保存按钮
                    if(widgetListSizeWasModified || viewModel.toolChanged.value == true) {
                        buttonAdd.visibility = View.VISIBLE
                    } else {
                        buttonAdd.visibility = View.INVISIBLE
                    }
                }

                else -> {
                    if(widgetListSizeWasModified) {
                        buttonAdd.visibility = View.VISIBLE
                    } else {
                        buttonAdd.visibility = View.INVISIBLE
                    }
                }
            }
        }
        viewModel.toolListSizeChanged.observe(this, widgetListSizeWasModifiedObserver)


        widgetWasModifiedObserver = Observer { widgetWasModified ->
            when(viewModel.getFragmentName()) {
                FragmentName.EDIT_LIST_FRAGMENT -> {
                    // 编辑列表存在大小改变和组件改变，必须同时不变才取消保存按钮
                    if(widgetWasModified || viewModel.toolListSizeChanged.value == true) {
                        buttonAdd.visibility = View.VISIBLE
                    } else {
                        buttonAdd.visibility = View.INVISIBLE
                    }
                }

                else -> {
                    if(widgetWasModified) {
                        buttonAdd.visibility = View.VISIBLE
                    } else {
                        buttonAdd.visibility = View.INVISIBLE
                    }
                }
            }
        }
        viewModel.toolChanged.observe(this, widgetWasModifiedObserver)


        multiSelectModeObserver = Observer { multiSelectMode ->
//             LogHelper.e(
//                 "isEditModeObserver",
//                 "fragmentName: ${viewModel.getFragmentName()}, isEditMode: $isEditMode"
//             )
            // 非多选模式才允许滑动
            updateViewPagerUserInputEnabled(!multiSelectMode)
            if(multiSelectMode) {
//                buttonAdd.text = getString(R.string.save_button)
                buttonExit.text = getString(R.string.return_button)
                buttonAdd.text = getString(R.string.delete_button)
                // 进入多选模式后，必然选中了一个条目
                buttonAdd.visibility = View.VISIBLE
//                buttonTime.text = getString(R.string.sort)
//                buttonTime.isClickable = true
            } else {
//                viewModel.widgetListOrderWasModified.value = false
////                buttonAdd.text = getString(R.string.add_button)
                buttonAdd.visibility = View.INVISIBLE
                buttonExit.text = getString(R.string.exit_button)
                buttonAdd.text = getString(R.string.save_button)
//                updateTime()
//                buttonTime.isClickable = false
            }
//            viewModel.selectedIds.value?.clear()
        }
        viewModel.multiselectMode.observe(this, multiSelectModeObserver)


        sortModeObserver = Observer { sortMode ->
            // 非排序模式才允许滑动
            updateViewPagerUserInputEnabled(!sortMode)
            if(sortMode) {
//                buttonAdd.visibility = View.INVISIBLE
                buttonExit.text = getString(R.string.return_button)
            } else {
//                viewModel.widgetListOrderWasModified.value = false
                buttonAdd.visibility = View.INVISIBLE
                buttonExit.text = getString(R.string.exit_button)
            }
        }
        viewModel.sortMode.observe(this, sortModeObserver)


        fragmentNameObserver = Observer { fragmentName ->
//             Log.e(
//                 "fragmentNameObserver",
//                 "fragmentName: $fragmentName, isEditMode: ${viewModel.editMode.value}"
//             )
            when(fragmentName) {
                FragmentName.WIDGET_LIST_FRAGMENT -> {
                    if(viewModel.multiselectMode.value == true) {
//                        buttonTime.text = getString(R.string.sort)
//                        buttonTime.isClickable = true
                        if(viewModel.toolListOrderChanged.value == true) {
                            buttonAdd.visibility = View.VISIBLE
                        } else {
                            buttonAdd.visibility = View.INVISIBLE
                        }
                    } else {
//                        buttonAdd.text = getString(R.string.add_button)
                        buttonAdd.visibility = View.INVISIBLE
                    }
                }

                FragmentName.EDIT_LIST_FRAGMENT -> {
                    updateTime()
//                    buttonTime.isClickable = false
                    buttonAdd.visibility = View.INVISIBLE
                }

                FragmentName.INSTALLED_APP_LIST_FRAGMENT -> {
                    buttonAdd.visibility = View.INVISIBLE
//                    buttonAdd.text = getString(R.string.save_button)
                }

                FragmentName.SUPPORT_AUTHOR_FRAGMENT -> {
                    buttonAdd.visibility = View.INVISIBLE
                }

                FragmentName.ABOUT_PROJECT_FRAGMENT -> {
                    buttonAdd.visibility = View.INVISIBLE
                }

                FragmentName.SETTING_FRAGMENT -> {
                    buttonAdd.visibility = View.INVISIBLE
//                    buttonAdd.text = getString(R.string.save_button)
                }

                FragmentName.WEB_VIEW_FRAGMENT -> {
                    buttonAdd.visibility = View.INVISIBLE
                }
            }
            if(fragmentName == FragmentName.WIDGET_LIST_FRAGMENT && viewModel.multiselectMode.value != true && viewModel.sortMode.value != true) {
                buttonExit.text = getString(R.string.exit_button)
            } else {
                buttonExit.text = getString(R.string.return_button)
            }

            if(fragmentName == FragmentName.WIDGET_LIST_FRAGMENT) {
                tabLayout.visibility = View.VISIBLE
            } else {
                tabLayout.visibility = View.INVISIBLE
            }

        }
        viewModel.fragmentName.observe(this, fragmentNameObserver)


        settingWasModifiedObserver = Observer { wasModified ->
            if(viewModel.getFragmentName() == FragmentName.SETTING_FRAGMENT) {
                if(wasModified) {
                    buttonAdd.visibility = View.VISIBLE
                } else {
                    buttonAdd.visibility = View.INVISIBLE
                }
            }
        }
        viewModel.configChanged.observe(this, settingWasModifiedObserver)


        selectedIdsObserver = Observer { selectedIds ->
            if(viewModel.getFragmentName() == FragmentName.WIDGET_LIST_FRAGMENT) {
                if(viewModel.multiselectMode.value == true) {
                    if(selectedIds.isNotEmpty()) {
                        buttonAdd.visibility = View.VISIBLE
                    } else {
                        buttonAdd.visibility = View.INVISIBLE
                    }
                }
            }
        }
        viewModel.selectedIds.observe(this, selectedIdsObserver)
    }

    // 移除观察者
    private fun removeObservers() {
        viewModel.toolListOrderChanged.removeObserver(widgetListOrderWasModifiedObserver)
        viewModel.toolListSizeChanged.removeObserver(widgetListSizeWasModifiedObserver)
        viewModel.toolChanged.removeObserver(widgetWasModifiedObserver)
        viewModel.multiselectMode.removeObserver(multiSelectModeObserver)
        viewModel.fragmentName.removeObserver(fragmentNameObserver)
        viewModel.configChanged.removeObserver(settingWasModifiedObserver)
        viewModel.sortMode.removeObserver(sortModeObserver)
        viewModel.selectedIds.removeObserver(selectedIdsObserver)
    }

}
