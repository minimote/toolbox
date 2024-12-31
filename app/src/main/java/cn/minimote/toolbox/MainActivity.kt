/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var gestureDetector: GestureDetector
    private var initialX = 0f

    // 滑动返回的比例阈值
    private val thresholdExit = 0.2

    // 主视图小组件的列数(6 是 2 和 3 的倍数)
    private val spanCount = 6

    private var widgetList: MutableList<WidgetInfo> = mutableListOf()
    private lateinit var widgetListRecyclerView: RecyclerView
    private lateinit var editBackground: ImageView
    private lateinit var widgetListAdapter: WidgetListAdapter
    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var iconCacheManager: IconCacheManager
    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    // 可传递的编辑模式标志
    private var isEditMode: MutableLiveData<Boolean> = MutableLiveData(false)

    private fun isAddButton(): Boolean {
        return buttonAdd.text == getString(R.string.add_button)
    }

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
        setTheme(R.style.Theme_toolbox)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.layout_main)

        iconCacheManager = IconCacheManager(this@MainActivity)

        widgetListRecyclerView = findViewById(R.id.recyclerView_widget_list)
        editBackground = findViewById(R.id.edit_background)


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
            if (isAddButton()) {
                navigateToAddAppListFragment()
            } else {
                if (isEditMode.value == true) {
                    exitEditMode()
                } else {
                    saveChanges()
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun navigateToAddAppListFragment() {
        // 清空内容
        widgetList.clear()
        widgetListAdapter.notifyDataSetChanged()

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout_recyclerView, AppListFragment(buttonAdd))
        transaction.addToBackStack(null)
        transaction.commit()
//        Log.i("MainActivity", "点击加号")

        buttonExit.text = getString(R.string.return_button)
        buttonAdd.text = getString(R.string.save_button)
        buttonAdd.visibility = View.GONE
        buttonAdd.isEnabled = false
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
//        setupRecyclerView()
//        widgetListAdapter.notifyDataSetChanged()
    }

    private fun saveChanges() {
        val fragment =
            supportFragmentManager.findFragmentById(R.id.frameLayout_recyclerView) as? AppListFragment
        fragment?.saveChanges()
        buttonAdd.visibility = View.GONE
        buttonAdd.isEnabled = false
    }

    private fun exitEditMode() {
        isEditMode.value = false
        buttonAdd.text = getString(R.string.add_button)
        buttonAdd.visibility = View.VISIBLE
        buttonAdd.isEnabled = true
        editBackground.visibility = View.GONE
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
                    findViewById<ConstraintLayout>(R.id.layout_main).animate()
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
//        Log.i("MainActivity", "初始化RecyclerView")
        uiScope.launch {
            widgetList = AppInfoStorage.loadAppSet(this@MainActivity).map { storageAppInfo ->
                WidgetInfo(
                    appIcon = iconCacheManager.getAppIcon(storageAppInfo.packageName),
                    appName = storageAppInfo.appName,
                    packageName = storageAppInfo.packageName,
                    activityName = storageAppInfo.activityName,
                    nickName = storageAppInfo.nickName,
//                    widgetType = Random.nextInt(1, 4),
                    widgetType = storageAppInfo.widgetType,
                    position = storageAppInfo.position
                )
            }.toMutableList()
            gridLayoutManager = GridLayoutManager(this@MainActivity, spanCount)
            widgetListAdapter = WidgetListAdapter(
                this@MainActivity,
                widgetList,
                buttonAdd,
                isEditMode,
                ::onEditWidget, // 传递编辑点击回调
                editBackground,
            )
            widgetListRecyclerView.adapter = widgetListAdapter

            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (widgetListAdapter.getItemViewType(position)) {
                        WidgetListAdapter.VIEW_TYPE_FULL -> spanCount / 1 // 占一整行
                        WidgetListAdapter.VIEW_TYPE_HALF -> spanCount / 2 // 占半行
                        WidgetListAdapter.VIEW_TYPE_THIRD -> spanCount / 3 // 占1/3行
                        else -> 1
                    }
                }
            }
            widgetListRecyclerView.layoutManager = gridLayoutManager

            // 添加自定义分隔线装饰
//            val dividerColor = resources.getColor(R.color.deep_gray) // 替换为你的颜色资源
//            val dividerWidth = resources.getDimensionPixelSize(R.dimen.divider_height) // 替换为你的高度资源
//            val paddingStart = resources.getDimensionPixelSize(R.dimen.padding_start) // 替换为你的起始间距资源
//            val paddingEnd = resources.getDimensionPixelSize(R.dimen.padding_end) // 替换为你的结束间距资源

            widgetListRecyclerView.addItemDecoration(
                CustomDividerItemDecoration(
                    this@MainActivity,
//                    color = dividerColor,
//                    width = dividerWidth,
//                    paddingStart = paddingStart,
//                    paddingEnd = paddingEnd
                )
            )

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onEditWidget(widget: WidgetInfo) {
//        // 清空内容
//        widgetList.clear()
//        widgetListAdapter.notifyDataSetChanged()
//
//        val transaction = supportFragmentManager.beginTransaction()
//        transaction.replace(
//            R.id.frameLayout_recyclerView,
//            EditWidgetFragment(widget, ::saveChanges)
//        )
//        transaction.addToBackStack(null)
//        transaction.commit()
////        Log.i("MainActivity", "点击加号")
//
//        buttonExit.text = getString(R.string.return_button)
//        buttonAdd.text = getString(R.string.save_button)
//        buttonAdd.visibility = View.GONE
//        buttonAdd.isEnabled = false
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun returnOrExit() {
        VibrationUtil.vibrateOnClick(this)
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            buttonExit.text = getString(R.string.exit_button)
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
        findViewById<ConstraintLayout>(R.id.layout_main).animate().x(0f).alpha(1f).setDuration(300)
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
