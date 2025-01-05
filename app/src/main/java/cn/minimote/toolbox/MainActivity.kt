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
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import cn.minimote.toolbox.fragment.ActivityListFragment
import cn.minimote.toolbox.fragment.WidgetListFragment
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("MainActivity", "onCreate")
        // 默认暗色模式
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
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
        // 检查 Fragment 是否已经存在
//        if(savedInstanceState == null) {
        showWidgetList()
//        }

        // 设置观察者
        setupObservers()
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
            VibrationUtil.vibrateOnClick(this)
            when(getTopBackStackEntryName()) {
                "WidgetListFragment" -> {
                    if(viewModel.isEditMode.value == true) {
                        viewModel.isEditMode.value = false
                        Toast.makeText(
                            this,
                            getString(R.string.exit_edit_mode),
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        returnOrExit()
                    }
                }

                else -> {
                    returnOrExit()
                }
            }
        }

        buttonAdd = findViewById(R.id.button_add)
        buttonAdd.setOnClickListener {
            VibrationUtil.vibrateOnClick(this)
            when(getTopBackStackEntryName()) {
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
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.constraintLayout_origin, ActivityListFragment(viewModel))
        transaction.addToBackStack("ActivityListFragment") // 添加到返回栈
        transaction.commitAllowingStateLoss() // 使用 commitAllowingStateLoss 避免状态丢失问题

        buttonExit.text = getString(R.string.return_button)
        buttonAdd.text = getString(R.string.save_button)
        buttonAdd.visibility = View.GONE
    }


    // 获取返回栈顶部的 Fragment 名称
    private fun getTopBackStackEntryName(): String? {
        val backStackEntryCount = supportFragmentManager.backStackEntryCount
        if(backStackEntryCount > 0) {
            val topEntry = supportFragmentManager.getBackStackEntryAt(backStackEntryCount - 1)
            return topEntry.name
        }
        return null
    }


    // 日志输出返回栈中的所有条目
    private fun printBackStackEntries() {
        for(i in 0 until supportFragmentManager.backStackEntryCount) {
            val entry = supportFragmentManager.getBackStackEntryAt(i)
            Log.i("返回栈", "$i: ${entry.name}")
        }
    }


    // 设置时间
    private fun setupTimeTextView() {
        timeTextView = findViewById(R.id.textView_time)
        updateTime()
        handler.post(runnable)
    }


    // 显示小组件的 Fragment
    private fun showWidgetList() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.constraintLayout_origin, WidgetListFragment(viewModel))
        transaction.addToBackStack("WidgetListFragment") // 添加到返回栈
        transaction.commitAllowingStateLoss() // 使用 commitAllowingStateLoss 避免状态丢失问题
    }


    // 设置观察者
    private fun setupObservers() {
        isModifiedObserver = Observer { isModified ->
            when(getTopBackStackEntryName()) {
                "WidgetListFragment" -> {
                }

                "ActivityListFragment" -> {
                    buttonAdd.isEnabled = isModified
                    buttonAdd.visibility = if(isModified) View.VISIBLE else View.GONE
                    if(isModified) {
                        buttonAdd.text = getString(R.string.save_button)
                    } else {
                        buttonAdd.text = getString(R.string.add_button)
                    }
                }
            }
        }
        viewModel.isModified.observe(this, isModifiedObserver)
        isEditModeObserver = Observer { isEditMode ->
            if(isEditMode) {
                buttonAdd.text = getString(R.string.save_button)
                buttonExit.text = getString(R.string.return_button)
            } else {
                buttonAdd.text = getString(R.string.add_button)
                buttonExit.text = getString(R.string.exit_button)
            }
        }
        viewModel.isEditMode.observe(this, isEditModeObserver)
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
        VibrationUtil.vibrateOnClick(this)
        // 最后一级是空的，不需要返回
        if(supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
            buttonExit.text = getString(R.string.exit_button)
            buttonAdd.text = getString(R.string.add_button)
            buttonAdd.visibility = View.VISIBLE
            buttonAdd.isEnabled = true
            // 如果需要刷新 RecyclerView，可以在这里调用
            // widgetListAdapter.notifyDataSetChanged()
        } else {
            finish()
        }
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
