/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox

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
import androidx.lifecycle.Observer
import cn.minimote.toolbox.fragment.ActivityListFragment
import cn.minimote.toolbox.fragment.WidgetListFragment
import cn.minimote.toolbox.objects.FragmentManagerHelper
import cn.minimote.toolbox.objects.VibrationHelper
import cn.minimote.toolbox.view_model.ToolboxViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: ToolboxViewModel by viewModels()

    private lateinit var fragmentNames: ToolboxViewModel.Constants.FragmentNames

    // 滑动返回的比例阈值
//    private val thresholdExit = 0.2
//
//    private var initialX = 0f
    private var screenWidth: Int = 0

    private lateinit var buttonTime: Button
    private lateinit var buttonExit: Button
    private lateinit var buttonAdd: Button

//    private late init var layoutMain: ConstraintLayout
//    private late init var gestureDetector: GestureDetector

    // 用于更新时间
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            handler.postDelayed(this, 1000) // 1秒更新一次
            // 编辑模式下显示的是排序，所以不更新时间
            if(viewModel.editMode.value == true) {
                return
            }
            updateTime()
        }
    }

    // 观察者
    private lateinit var wasModifiedObserver: Observer<Boolean>
    private lateinit var isEditModeObserver: Observer<Boolean>
    private lateinit var fragmentNameObserver: Observer<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Log.i("MainActivity", "onCreate")
        // 默认暗色模式(使用后会出现亮色模式无法打开的问题)
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        enableEdgeToEdge()
        setContentView(R.layout.layout_main)
//        layoutMain = findViewById(R.id.layout_main)
//        printBackStackEntries()

        fragmentNames = viewModel.fragmentNames
        // 获取屏幕宽度
        viewModel.screenWidth = resources.displayMetrics.widthPixels

        // 适配系统返回手势和按钮
        setupBackPressedCallback()
        // 设置按钮
        setupButtons()

//        // 设置手势监听器
//        setupGestureDetector()

        // 设置观察者
        setupObservers()

        // 显示首页小组件
        showWidgetList()
    }


    override fun onDestroy() {
        super.onDestroy()
//        Log.i("MainActivity", "onDestroy")
        // 移除更新时间的任务
        handler.removeCallbacks(runnable)
        // 移除观察者
        removeObservers()
    }


    private fun removeObservers() {
        viewModel.widgetListWasModified.removeObserver(wasModifiedObserver)
        viewModel.editMode.removeObserver(isEditModeObserver)
        viewModel.fragmentName.removeObserver(fragmentNameObserver)
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
                returnOrExit()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }


    // 设置按钮
    private fun setupButtons() {
        buttonExit = findViewById(R.id.button_exit)
        buttonExit.setOnClickListener {
            VibrationHelper.vibrateOnClick(this)
            returnOrExit()
        }

        buttonAdd = findViewById(R.id.button_add)
        buttonAdd.setOnClickListener {
            VibrationHelper.vibrateOnClick(this)
            when(viewModel.getFragmentName()) {
                fragmentNames.WIDGET_LIST_FRAGMENT -> {
                    if(viewModel.editMode.value == true) {
                        viewModel.saveStorageActivities()
                        Toast.makeText(
                            this,
                            getString(R.string.save_success),
                            Toast.LENGTH_SHORT,
                        ).show()
                        buttonAdd.visibility = View.GONE
                    } else {
                        changeToActivityListFragment()
                    }
                }

                fragmentNames.EDIT_LIST_FRAGMENT -> {
                    viewModel.updateWidget()
                    viewModel.updateStorageActivityList()
                    viewModel.saveStorageActivities()
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.save_success),
                        Toast.LENGTH_SHORT
                    ).show()
                    buttonAdd.visibility = View.GONE
                }

                fragmentNames.ACTIVITY_LIST_FRAGMENT -> {
                    viewModel.updateStorageActivityList()
                    viewModel.saveStorageActivities()
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.save_success),
                        Toast.LENGTH_SHORT
                    ).show()
                    buttonAdd.visibility = View.GONE
                }
            }
        }

        // 设置时间
        setupTime()
    }


    // 切换到 AppListFragment
    private fun changeToActivityListFragment() {
        val fragment = ActivityListFragment(viewModel)
        FragmentManagerHelper.replaceFragment(
            fragmentManager = supportFragmentManager,
            fragment = fragment,
            viewModel = viewModel,
        )

//        buttonExit.text = getString(R.string.return_button)
        buttonAdd.text = getString(R.string.save_button)
        buttonAdd.visibility = View.GONE
    }


    // 设置时间
    private fun setupTime() {
        buttonTime = findViewById(R.id.button_time)
        buttonTime.setOnClickListener {
            if(viewModel.editMode.value == true) {
                // 组件排序
                VibrationHelper.vibrateOnClick(this)
                viewModel.sortStoredActivityList()
            }
        }
        updateTime()
        handler.post(runnable)
    }


    private fun updateTime() {
        // 从资源文件中获取时间格式字符串
        val timeFormat = getString(R.string.time_format)
        // 使用 SimpleDateFormat 格式化当前时间
        buttonTime.text = SimpleDateFormat(timeFormat, Locale.getDefault()).format(Date())
    }


    // 显示小组件的 Fragment
    private fun showWidgetList() {
        val fragment = WidgetListFragment(
            viewModel = viewModel,
            fragmentManager = supportFragmentManager,
        )
        FragmentManagerHelper.replaceFragment(
            fragmentManager = supportFragmentManager,
            fragment = fragment,
            viewModel = viewModel,
        )
    }


    // 设置观察者
    private fun setupObservers() {
        wasModifiedObserver = Observer { wasModified ->
//            Log.i(
//                "isModifiedObserver",
//                "fragmentName: ${viewModel.getFragmentName()}, wasModified: $wasModified"
//            )
            when(viewModel.getFragmentName()) {
                fragmentNames.WIDGET_LIST_FRAGMENT -> {
                    if(wasModified) {
                        buttonAdd.visibility = View.VISIBLE
                        buttonAdd.text = getString(R.string.save_button)
                    } else {
                        buttonAdd.visibility = View.GONE
                    }
                }

                fragmentNames.EDIT_LIST_FRAGMENT -> {
                    if(wasModified) {
                        buttonAdd.visibility = View.VISIBLE
                        buttonAdd.text = getString(R.string.save_button)
                    } else {
                        buttonAdd.visibility = View.GONE
                    }
                }

                fragmentNames.ACTIVITY_LIST_FRAGMENT -> {
                    if(wasModified) {
                        buttonAdd.visibility = View.VISIBLE
                        buttonAdd.text = getString(R.string.save_button)
                    } else {
                        buttonAdd.visibility = View.GONE
                    }
                }
            }
        }
        viewModel.widgetListWasModified.observe(this, wasModifiedObserver)

        isEditModeObserver = Observer { isEditMode ->
            // Log.i(
            //     "isEditModeObserver",
            //     "fragmentName: ${viewModel.getFragmentName()}, isEditMode: $isEditMode"
            // )
            if(isEditMode) {
//                buttonAdd.text = getString(R.string.save_button)
                buttonAdd.visibility = View.GONE
                buttonExit.text = getString(R.string.return_button)
                buttonTime.text = getString(R.string.sort)
            } else {
                buttonAdd.text = getString(R.string.add_button)
                buttonAdd.visibility = View.VISIBLE
                buttonExit.text = getString(R.string.exit_button)
                updateTime()
            }
        }
        viewModel.editMode.observe(this, isEditModeObserver)

        fragmentNameObserver = Observer {
            val fragmentName = viewModel.getFragmentName()
            // Log.i(
            //     "fragmentNameObserver",
            //     "fragmentName: $fragmentName, isEditMode: ${viewModel.editMode.value}"
            // )
            if(fragmentName == fragmentNames.WIDGET_LIST_FRAGMENT && viewModel.editMode.value == false) {
                buttonExit.text = getString(R.string.exit_button)
                buttonAdd.visibility = View.VISIBLE
                buttonAdd.text = getString(R.string.add_button)
            } else {
                buttonExit.text = getString(R.string.return_button)
            }
        }
        viewModel.fragmentName.observe(this, fragmentNameObserver)
    }


    // 设置手势监听器
//    private fun setupGestureDetector() {
//        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
//            override fun onScroll(
//                e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float
//            ): Boolean {
//                if(e1 != null) {
////                    if(initialX == 0f) {
////                        initialX = e1.x
////                    }
////                    val scrollDistance = e2.x - e1.x
//                    // 获取当前坐标
//                    val left = layoutMain.left
//                    Log.i("onScroll", "left: $left")
//
////                    val opacity = 1 - abs(scrollDistance / screenWidth)
//                    val opacity = left.toFloat() / screenWidth
//                    layoutMain.animate()
////                        .x(scrollDistance + initialX)
//                        .alpha(opacity).setDuration(0).start()
//                }
//                return true
//            }
//
////            // 快速滑动
////            override fun onFling(
////                e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
////            ): Boolean {
////                if(e1 != null) {
////                    val scrollDistance = e2.x - e1.x
////                    if(abs(scrollDistance) > screenWidth * thresholdExit) {
////                        finish()
////                        return true
////                    } else {
////                        resetLayout()
////                    }
////                }
////                return false
////            }
//
////            override fun onSingleTapUp(e: MotionEvent): Boolean {
////                resetLayout()
////                return true
////            }
//        })
//    }


    private fun returnOrExit() {
        val fragmentName = viewModel.getFragmentName()
        // Log.i("returnOrExit", fragmentName)
        if(fragmentName == fragmentNames.WIDGET_LIST_FRAGMENT && viewModel.editMode.value == true) {
            viewModel.editMode.value = false
            Toast.makeText(
                this,
                getString(R.string.exit_edit_mode),
                Toast.LENGTH_SHORT,
            ).show()
            return
        }
        // 从编辑界面返回时剩余组件为空，则返回主页
        if(fragmentName == fragmentNames.EDIT_LIST_FRAGMENT && viewModel.storedActivityList.value?.size == 0) {
            viewModel.editMode.value = false
            Toast.makeText(
                this,
                getString(R.string.toast_no_widget_return_home),
                Toast.LENGTH_SHORT,
            ).show()
        }
        viewModel.widgetListWasModified.value = false
        FragmentManagerHelper.popFragment(
            fragmentManager = supportFragmentManager,
            viewModel = viewModel,
            activity = this,
        )
    }


//    private fun resetLayout() {
//        layoutMain.animate().x(0f).alpha(1f).setDuration(300)
//            .start()
//        initialX = 0f
//    }


//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        if(event.action == MotionEvent.ACTION_UP) {
//            resetLayout()
//        }
//        gestureDetector.onTouchEvent(event)
//        return super.onTouchEvent(event)
//    }

}
