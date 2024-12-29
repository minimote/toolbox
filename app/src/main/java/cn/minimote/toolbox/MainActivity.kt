/*
 * Copyright (c) 2024 minimote(微尘). All rights reserved.
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
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var gestureDetector: GestureDetector
    private var initialX = 0f
    private val thresholdExit = 0.2

    private var widgetList: MutableList<WidgetInfo> = mutableListOf()
    private lateinit var widgetListRecyclerView: RecyclerView
    private lateinit var widgetListAdapter: WidgetListAdapter
    private lateinit var iconCacheManager: IconCacheManager
    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    private var isAddButton = true

    private lateinit var timeTextView: TextView
    private lateinit var buttonExit: Button
    private lateinit var buttonAdd: Button
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 10000) // 10秒更新一次
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_layout)

        iconCacheManager = IconCacheManager(this@MainActivity)
        widgetListRecyclerView = findViewById(R.id.recyclerView_widget_list)
        widgetListRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        setupBackPressedCallback()
        setupButtons()
        setupTimeTextView()
        setupGestureDetector()
        setupRecyclerView()
    }

    private fun setupBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                returnOrExit()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun setupButtons() {
        buttonAdd = findViewById(R.id.button_add)
        buttonExit = findViewById(R.id.button_exit)

        buttonExit.setOnClickListener {
            returnOrExit()
        }

        buttonAdd.setOnClickListener {
            VibrationUtil.vibrateOnClick(this)
            if (isAddButton) {
                navigateToAddAppListFragment()
            } else {
                saveChanges()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun navigateToAddAppListFragment() {
        widgetList.clear()
        widgetListAdapter.notifyDataSetChanged()
        // 清空 RecyclerView 所在的 FrameLayout
//        val frameLayout: FrameLayout = findViewById(R.id.frameLayout_recyclerView)
//        frameLayout.removeAllViews()

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout_recyclerView, AppListFragment(buttonAdd))
        transaction.addToBackStack(null)
        transaction.commit()
        Log.i("MainActivity", "点击加号")

        buttonExit.text = getString(R.string.return_button)
        buttonAdd.text = getString(R.string.save_button)
        buttonAdd.visibility = View.GONE
        buttonAdd.isEnabled = false

        isAddButton = false
    }

    override fun onResume() {
        super.onResume()
        setupRecyclerView()
    }

    private fun saveChanges() {
        val fragment =
            supportFragmentManager.findFragmentById(R.id.frameLayout_recyclerView) as? AppListFragment
        fragment?.saveChanges()
        buttonAdd.visibility = View.GONE
        buttonAdd.isEnabled = false
    }

    private fun setupTimeTextView() {
        timeTextView = findViewById(R.id.textView_time)
        updateTime()
        handler.post(runnable)
    }

    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float
            ): Boolean {
                if (e1 != null) {
                    if (initialX == 0f) {
                        initialX = e1.x
                    }
                    val scrollDistance = e2.x - e1.x
                    val opacity = 1 - abs(scrollDistance / resources.displayMetrics.widthPixels)
                    findViewById<ConstraintLayout>(R.id.main_layout).animate()
                        .x(scrollDistance + initialX).alpha(opacity).setDuration(0).start()
                }
                return true
            }

            override fun onFling(
                e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
            ): Boolean {
                if (e1 != null) {
                    val scrollDistance = e2.x - e1.x
                    if (abs(scrollDistance) > resources.displayMetrics.widthPixels * thresholdExit) {
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

    private fun setupRecyclerView() {
        Log.i("MainActivity", "初始化RecyclerView")
        uiScope.launch {
            widgetList = AppInfoStorage.loadAppSet(this@MainActivity).map { storageAppInfo ->
                WidgetInfo(
                    appIcon = iconCacheManager.getAppIcon(storageAppInfo.packageName),
                    appName = storageAppInfo.appName,
                    packageName = storageAppInfo.packageName,
                    activityName = storageAppInfo.activityName,
                    nickName = storageAppInfo.nickName,
                    widgetSize = storageAppInfo.widgetSize,
                    position = storageAppInfo.position
                )
            }.toMutableList()
            Log.i("MainActivity", "一共保存了${widgetList.size}个应用")
            widgetListAdapter = WidgetListAdapter(this@MainActivity, widgetList)
            widgetListRecyclerView.adapter = widgetListAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun returnOrExit() {
        VibrationUtil.vibrateOnClick(this)
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            buttonExit.text = getString(R.string.exit_button)
            isAddButton = true
            buttonAdd.text = getString(R.string.add_button)
            buttonAdd.visibility = View.VISIBLE
            buttonAdd.isEnabled = true
            setupRecyclerView()
            widgetListAdapter.notifyDataSetChanged()
        } else {
            finish()
        }
    }

    private fun updateTime() {
        timeTextView.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    private fun resetLayout() {
        findViewById<ConstraintLayout>(R.id.main_layout).animate().x(0f).alpha(1f).setDuration(300)
            .start()
        initialX = 0f
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            resetLayout()
        }
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }
}
