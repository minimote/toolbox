/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import cn.minimote.toolbox.fragment.ActivityListFragment
import cn.minimote.toolbox.fragment.WidgetListFragment
import cn.minimote.toolbox.objects.FragmentManagerHelper
import cn.minimote.toolbox.objects.VibrationHelper
import cn.minimote.toolbox.view_model.ActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: ActivityViewModel by viewModels()


    // 滑动返回的比例阈值
    private val thresholdExit = 0.2

    private var initialX = 0f
    private var screenWidth: Int = 0

    private lateinit var timeTextView: TextView
    private lateinit var buttonExit: Button
    private lateinit var buttonAdd: Button

    private lateinit var gestureDetector: GestureDetector

    // 用于更新时间
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 10000) // 10秒更新一次
        }
    }

    // 存储观察者引用
    private lateinit var isModifiedObserver: Observer<Boolean>
    private lateinit var isEditModeObserver: Observer<Boolean>
    private lateinit var fragmentNameObserver: Observer<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("MainActivity", "onCreate")
        // 默认暗色模式(使用后会出现亮色模式无法打开的问题)
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        enableEdgeToEdge()
        setContentView(R.layout.layout_main)
//        printBackStackEntries()

        // 获取屏幕宽度
        screenWidth = resources.displayMetrics.widthPixels

        // 适配系统返回手势和按钮
        setupBackPressedCallback()
        // 设置按钮
        setupButtons()
        // 设置时间
        setupTimeTextView()
        // 设置手势监听器
        setupGestureDetector()

        // 设置观察者
        setupObservers()

        // 在配置更改时，清空返回栈并重新加载 Fragment
        if(savedInstanceState != null) {
            Log.i("MainActivity", "配置更改了")
            supportFragmentManager.popBackStackImmediate(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
        showWidgetList()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("MainActivity", "onDestroy")
        // 移除更新时间的任务
        handler.removeCallbacks(runnable)
        Log.i("MainActivity", "onDestroy 移除更新时间的任务")
        // 移除观察者
        viewModel.isModified.removeObserver(isModifiedObserver)
        viewModel.isEditMode.removeObserver(isEditModeObserver)
        viewModel.fragmentName.removeObserver(fragmentNameObserver)
        Log.i("MainActivity", "onDestroy 移除观察者")
        // 清空返回栈
//        supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        Log.i("MainActivity", "onDestroy 结束")
//        printBackStackEntries()
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // 恢复保存的状态
        Log.i("MainActivity", "onRestoreInstanceState")
//        printBackStackEntries()
    }


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
                "WidgetListFragment" -> {
                    if(viewModel.isEditMode.value == true) {
                        viewModel.isEditMode.value = false
                        Toast.makeText(
                            this,
                            getString(R.string.save_success),
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        changeToActivityListFragment()
                    }
                }

                "EditWidgetFragment" -> {
                    viewModel.updateStorageWidgetList()
                    viewModel.saveStorageActivities()
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.save_success),
                        Toast.LENGTH_SHORT
                    ).show()
                    buttonAdd.visibility = View.GONE
                }

                "ActivityListFragment" -> {
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
    private fun setupTimeTextView() {
        timeTextView = findViewById(R.id.textView_time)
        updateTime()
        handler.post(runnable)
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
        isModifiedObserver = Observer { isModified ->
            Log.i(
                "isModifiedObserver",
                "fragmentName: ${viewModel.getFragmentName()}, isModified: $isModified"
            )
            when(viewModel.getFragmentName()) {
                "WidgetListFragment" -> {

                }

                "EditWidgetFragment" -> {
                    if(isModified) {
                        buttonAdd.visibility = View.VISIBLE
                        buttonAdd.text = getString(R.string.save_button)
                    } else {
                        buttonAdd.visibility = View.GONE
                    }
                }

                "ActivityListFragment" -> {
                    if(isModified) {
                        buttonAdd.visibility = View.VISIBLE
                        buttonAdd.text = getString(R.string.save_button)
                    } else {
                        buttonAdd.visibility = View.GONE
                    }
                }
            }
        }
        viewModel.isModified.observe(this, isModifiedObserver)

        isEditModeObserver = Observer { isEditMode ->
            Log.i(
                "isEditModeObserver",
                "fragmentName: ${viewModel.getFragmentName()}, isEditMode: $isEditMode"
            )
            if(isEditMode) {
//                buttonAdd.text = getString(R.string.save_button)
                buttonAdd.visibility = View.GONE
                buttonExit.text = getString(R.string.return_button)
            } else {
                buttonAdd.text = getString(R.string.add_button)
                buttonAdd.visibility = View.VISIBLE
                buttonExit.text = getString(R.string.exit_button)
            }
        }
        viewModel.isEditMode.observe(this, isEditModeObserver)

        fragmentNameObserver = Observer {
            val fragmentName = viewModel.getFragmentName()
            Log.i(
                "fragmentNameObserver",
                "fragmentName: $fragmentName, isEditMode: ${viewModel.isEditMode.value}"
            )
            if(fragmentName == "WidgetListFragment" && viewModel.isEditMode.value == false) {
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
    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float
            ): Boolean {
                if(e1 != null) {
                    if(initialX == 0f) {
                        initialX = e1.x
                    }
                    val scrollDistance = e2.x - e1.x
                    val opacity = 1 - abs(scrollDistance / screenWidth)
                    findViewById<ConstraintLayout>(R.id.layout_main).animate()
                        .x(scrollDistance + initialX).alpha(opacity).setDuration(0).start()
                }
                return true
            }

            override fun onFling(
                e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
            ): Boolean {
                if(e1 != null) {
                    val scrollDistance = e2.x - e1.x
                    if(abs(scrollDistance) > screenWidth * thresholdExit) {
                        finish()
                        return true
                    } else {
                        resetLayout()
                    }
                }
                return false
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                resetLayout()
                return true
            }
        })
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun returnOrExit() {
        Log.i("returnOrExit", "fragrantName:${viewModel.getFragmentName()}")
        if(viewModel.getFragmentName() == "WidgetListFragment" && viewModel.isEditMode.value == true) {
            viewModel.isEditMode.value = false
            Toast.makeText(
                this,
                getString(R.string.exit_edit_mode),
                Toast.LENGTH_SHORT,
            ).show()
            return
        }
        viewModel.isModified.value = false
        FragmentManagerHelper.popFragment(
            fragmentManager = supportFragmentManager,
            viewModel = viewModel,
            activity = this,
        )
    }


    private fun updateTime() {
        timeTextView.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }


    private fun resetLayout() {
        findViewById<ConstraintLayout>(R.id.layout_main).animate().x(0f).alpha(1f).setDuration(300)
            .start()
        initialX = 0f
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if(event.action == MotionEvent.ACTION_UP) {
            resetLayout()
        }
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

}
