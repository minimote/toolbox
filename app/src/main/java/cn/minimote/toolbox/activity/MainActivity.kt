/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import cn.minimote.toolbox.helper.CheckUpdateHelper
import cn.minimote.toolbox.helper.ConfigHelper
import cn.minimote.toolbox.helper.FragmentHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.pageTransformer.ViewPager2Transformer
import cn.minimote.toolbox.viewModel.ToolboxViewModel
import cn.minimote.toolbox.viewModel.ToolboxViewModel.Companion.FragmentNames
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: ToolboxViewModel by viewModels()

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

//    private late init var layoutMain: ConstraintLayout
//    private late init var gestureDetector: GestureDetector

    // 用于更新时间
    private val handler = Handler(Looper.getMainLooper())
    // 创建一个 HandlerThread
//    private val backgroundThread = HandlerThread("BackgroundThread").apply {
//        start()
//    }
    // 创建一个与 HandlerThread 关联的 Handler
//    private val backgroundHandler = Handler(backgroundThread.looper)

    private val runnable = object : Runnable {
        override fun run() {
            // 1 秒更新一次
//            backgroundHandler.postDelayed(this, 1000)
            handler.postDelayed(this, 1000)
            // 编辑模式下显示的是排序，所以不更新时间
            if(viewModel.editMode.value == true && viewModel.getFragmentName() == FragmentNames.WIDGET_LIST_FRAGMENT) {
                return
            }
            updateTime()
        }
    }

    // 观察者
    private lateinit var widgetListOrderWasModifiedObserver: Observer<Boolean>
    private lateinit var widgetListSizeWasModifiedObserver: Observer<Boolean>
    private lateinit var widgetWasModifiedObserver: Observer<Boolean>

    private lateinit var isEditModeObserver: Observer<Boolean>
    private lateinit var fragmentNameObserver: Observer<String>

    private lateinit var settingWasModifiedObserver: Observer<Boolean>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Log.d("MainActivity", "onCreate")
//        Log.d("MainActivity", "ViewModel initialized: ${System.identityHashCode(viewModel)}")

        // 默认暗色模式(使用后会出现亮色模式无法打开的问题)
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        enableEdgeToEdge()
        setContentView(R.layout.layout_main)
//        layoutMain = findViewById(R.id.layout_main)
//        printBackStackEntries()

        constraintLayoutOrigin = findViewById(R.id.constraintLayout_origin)

//        // 获取屏幕尺寸
        viewModel.updateScreenSize(resources.displayMetrics)

        // 适配系统返回手势和按钮
        setupBackPressedCallback()

        // 设置观察者
        setupObservers()

        // 设置按钮
        setupButtons()

        // 设置 ViewPager
        setupViewPager()

        // 显示首页小组件
//        showWidgetList()


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
        if(viewModel.isWatch()) {
            // 将手表顶部的顶部按钮间距设为 30dp
            val guideline = findViewById<Guideline>(R.id.guideline4)
            guideline.setGuidelineBegin(viewModel.dpToPx(30f))
        }

        buttonExit = findViewById(R.id.button_exit)
        buttonExit.setOnClickListener {
            VibrationHelper.vibrateOnClick(this, viewModel)
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
        buttonAdd.setOnClickListener {
            VibrationHelper.vibrateOnClick(this, viewModel)
            when(viewModel.getFragmentName()) {
                FragmentNames.WIDGET_LIST_FRAGMENT -> {
                    if(viewModel.editMode.value != true) {
                        changeToActivityListFragment()
                        return@setOnClickListener
                    }
                }

                FragmentNames.SETTING_FRAGMENT -> {
                    ConfigHelper.saveUserConfig(viewModel)
                    return@setOnClickListener
                }

            }

            viewModel.saveWidgetList()
            Toast.makeText(
                this@MainActivity,
                getString(R.string.save_success),
                Toast.LENGTH_SHORT,
            ).show()
            buttonAdd.visibility = View.GONE
        }


        // 设置时间
        setupTimeButton()
    }


    // 切换到 AppListFragment
    private fun changeToActivityListFragment() {
        FragmentHelper.switchFragment(
            fragmentName = FragmentNames.ACTIVITY_LIST_FRAGMENT,
            fragmentManager = supportFragmentManager,
            viewModel = viewModel,
            viewPager = viewPager,
            constraintLayoutOrigin = constraintLayoutOrigin,
        )
    }


    // 设置时间
    private fun setupTimeButton() {
        buttonTime = findViewById(R.id.button_time)
        buttonTime.setOnClickListener {
            Log.e(viewModel.getFragmentName(), viewModel.editMode.value.toString())
            if(viewModel.editMode.value == true && viewModel.getFragmentName() == FragmentNames.WIDGET_LIST_FRAGMENT) {
                // 组件排序
                VibrationHelper.vibrateOnClick(this, viewModel)
                viewModel.sortStoredActivityList()
            }
        }
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
        viewPager = findViewById(R.id.viewPager_origin)
        tabLayout = findViewById(R.id.tabLayout)

        val adapter = ToolboxFragmentStateAdapter(
            fragmentActivity = this,
            viewPager = viewPager,
            constraintLayoutOrigin = constraintLayoutOrigin,
            viewModel = viewModel,
        )
        viewPager.adapter = adapter

        // 关联 ViewPager2 和 TabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // 设置每个 Tab 的标题或图标
            tab.text = viewModel.viewPaperFragmentNameList[position]
            // 设置点击事件
            tab.view.setOnClickListener {
                VibrationHelper.vibrateOnClick(this@MainActivity, viewModel)
            }
            if(viewModel.isWatch()) {
                tab.customView = if(position == 0) {
                    layoutInflater.inflate(R.layout.tablayout_dot_selected, tabLayout, false)
                } else {
                    layoutInflater.inflate(R.layout.tablayout_dot, tabLayout, false)
                }
            }
        }.attach()

        // 设置手表
        if(viewModel.isWatch()) {
            // 手表使用小圆点，并且居中
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    // 设置选中状态的布局
                    tab.customView =
                        layoutInflater.inflate(R.layout.tablayout_dot_selected, tabLayout, false)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    // 设置未选中状态的布局
                    tab.customView =
                        layoutInflater.inflate(R.layout.tablayout_dot, tabLayout, false)
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                    // 重新选中时可以设置特定的布局
                    tab.customView =
                        layoutInflater.inflate(R.layout.tablayout_dot_selected, tabLayout, false)
                }
            })

            tabLayout.tabGravity = TabLayout.GRAVITY_CENTER
            // 设置 TabLayout 的高度
            tabLayout.layoutParams.height =
                resources.getDimensionPixelSize(R.dimen.layout_size_1_small)
        } else {
            // 设置页面切换动画
            viewPager.setPageTransformer(ViewPager2Transformer())
        }

    }


    // 更新 viewPager.isUserInputEnabled
    private fun updateViewPagerUserInputEnabled(enable: Boolean) {
        viewPager.isUserInputEnabled = enable
    }


    // 设置观察者
    private fun setupObservers() {
        widgetListOrderWasModifiedObserver = Observer { widgetListOrderWasModified ->
//            Log.e("顺序观察者", "$widgetListOrderWasModified")
            if(widgetListOrderWasModified) {
                buttonAdd.visibility = View.VISIBLE
            } else {
                buttonAdd.visibility = View.GONE
            }
        }
        viewModel.widgetListOrderWasModified.observe(this, widgetListOrderWasModifiedObserver)


        widgetListSizeWasModifiedObserver = Observer { widgetListSizeWasModified ->
            // Log.e("大小观察者", "$widgetListSizeWasModified")
            when(viewModel.getFragmentName()) {
                FragmentNames.EDIT_LIST_FRAGMENT -> {
                    // 编辑列表存在大小改变和组件改变，必须同时不变才取消保存按钮
                    if(widgetListSizeWasModified) {
                        buttonAdd.visibility = View.VISIBLE
                    } else if(viewModel.widgetWasModified.value == false) {
                        buttonAdd.visibility = View.GONE
                    }
                }

                else -> {
                    if(widgetListSizeWasModified) {
                        buttonAdd.visibility = View.VISIBLE
                    } else {
                        buttonAdd.visibility = View.GONE
                    }
                }
            }
        }
        viewModel.widgetListSizeWasModified.observe(this, widgetListSizeWasModifiedObserver)


        widgetWasModifiedObserver = Observer { widgetWasModified ->
            when(viewModel.getFragmentName()) {
                FragmentNames.EDIT_LIST_FRAGMENT -> {
                    // 编辑列表存在大小改变和组件改变，必须同时不变才取消保存按钮
                    if(widgetWasModified) {
                        buttonAdd.visibility = View.VISIBLE
                    } else if(viewModel.widgetListSizeWasModified.value == false) {
                        buttonAdd.visibility = View.GONE
                    }
                }

                else -> {
                    if(widgetWasModified) {
                        buttonAdd.visibility = View.VISIBLE
                    } else {
                        buttonAdd.visibility = View.GONE
                    }
                }
            }
        }
        viewModel.widgetWasModified.observe(this, widgetWasModifiedObserver)


        isEditModeObserver = Observer { isEditMode ->
            // Log.i(
            //     "isEditModeObserver",
            //     "fragmentName: ${viewModel.getFragmentName()}, isEditMode: $isEditMode"
            // )
            // 非编辑模式才允许滑动
            updateViewPagerUserInputEnabled(!isEditMode)
            if(isEditMode) {
                buttonAdd.text = getString(R.string.save_button)
                buttonAdd.visibility = View.GONE
                buttonExit.text = getString(R.string.return_button)
                buttonTime.text = getString(R.string.sort)
                buttonTime.isClickable = true
            } else {
                viewModel.widgetListOrderWasModified.value = false
                buttonAdd.text = getString(R.string.add_button)
                buttonAdd.visibility = View.VISIBLE
                buttonExit.text = getString(R.string.exit_button)
                updateTime()
                buttonTime.isClickable = false
            }
        }
        viewModel.editMode.observe(this, isEditModeObserver)


        fragmentNameObserver = Observer { fragmentName ->
//             Log.e(
//                 "fragmentNameObserver",
//                 "fragmentName: $fragmentName, isEditMode: ${viewModel.editMode.value}"
//             )
            when(fragmentName) {
                FragmentNames.WIDGET_LIST_FRAGMENT -> {
                    if(viewModel.editMode.value == true) {
                        buttonTime.text = getString(R.string.sort)
                        buttonTime.isClickable = true
                        if(viewModel.widgetListOrderWasModified.value == true) {
                            buttonAdd.visibility = View.VISIBLE
                        } else {
                            buttonAdd.visibility = View.GONE
                        }
                    } else {
                        buttonAdd.text = getString(R.string.add_button)
                        buttonAdd.visibility = View.VISIBLE
                    }
                }

                FragmentNames.EDIT_LIST_FRAGMENT -> {
                    updateTime()
                    buttonTime.isClickable = false
                    buttonAdd.visibility = View.GONE
                }

                FragmentNames.ACTIVITY_LIST_FRAGMENT -> {
                    buttonAdd.visibility = View.GONE
                    buttonAdd.text = getString(R.string.save_button)
                }

                FragmentNames.SUPPORT_AUTHOR_FRAGMENT -> {
                    buttonAdd.visibility = View.GONE
                }

                FragmentNames.ABOUT_PROJECT_FRAGMENT -> {
                    buttonAdd.visibility = View.GONE
                }

                FragmentNames.SETTING_FRAGMENT -> {
                    buttonAdd.visibility = View.GONE
                    buttonAdd.text = getString(R.string.save_button)
                }

                FragmentNames.WEB_VIEW_FRAGMENT -> {
                    buttonAdd.visibility = View.GONE
                }
            }
            if(fragmentName == FragmentNames.WIDGET_LIST_FRAGMENT && viewModel.editMode.value == false) {
                buttonExit.text = getString(R.string.exit_button)
            } else {
                buttonExit.text = getString(R.string.return_button)
            }

        }
        viewModel.fragmentName.observe(this, fragmentNameObserver)


        settingWasModifiedObserver = Observer { wasModified ->
            if(wasModified) {
                buttonAdd.visibility = View.VISIBLE
            } else {
                buttonAdd.visibility = View.GONE
            }
        }
        viewModel.settingWasModified.observe(this, settingWasModifiedObserver)
    }


    // 移除观察者
    private fun removeObservers() {
        viewModel.widgetListOrderWasModified.removeObserver(widgetListOrderWasModifiedObserver)
        viewModel.widgetListSizeWasModified.removeObserver(widgetListSizeWasModifiedObserver)
        viewModel.widgetWasModified.removeObserver(widgetWasModifiedObserver)
        viewModel.editMode.removeObserver(isEditModeObserver)
        viewModel.fragmentName.removeObserver(fragmentNameObserver)
        viewModel.settingWasModified.removeObserver(settingWasModifiedObserver)
    }

}
