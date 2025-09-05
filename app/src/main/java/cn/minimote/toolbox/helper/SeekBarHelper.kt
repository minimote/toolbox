/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import cn.minimote.toolbox.viewModel.MyViewModel
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs


object SeekBarHelper {

    // 添加一个方法来获取当前值，这个方法需要在具体的适配器中实现
    interface SeekBarCallback {
        fun updateConfigValue(value: String)
        fun setupTextView(value: String)
    }

    // 长按连续触发相关常量
    private const val LONG_PRESS_DELAY_MS: Long = 600  // 初始延迟时间
    private const val REPEAT_INTERVAL_MS: Long = 100   // 重复触发间隔

    // 用于处理长按连续触发的Handler
    private val handler = Handler(Looper.getMainLooper())

    // 当前正在运行的Runnable，用于停止连续触发
    private var currentRunnable: Runnable? = null

    @SuppressLint("ClickableViewAccessibility")
    fun setSeekBar(
        seekBar: SeekBar,
        valueList: List<String>,
        initPosition: Int,
        viewModel: MyViewModel,
        callback: SeekBarCallback,
        textViewIncrease: TextView? = null,
        textViewDecrease: TextView? = null,
    ) {
        val lastPosition = AtomicInteger(-1)

        callback.setupTextView(valueList[initPosition])
        // 设置初始进度
        seekBar.progress = initPosition

        // 可停留的位置数量
        val positionNumber = valueList.size
        seekBar.max = positionNumber - 1
        val positions = IntArray(positionNumber)
        for(i in 0 until positionNumber) {
            positions[i] = i // 直接使用整数值 [0, 1, 2, 3, 4, 5]
        }

        // 设置减少按钮的点击事件
        textViewDecrease?.setOnClickListener {
            val currentPosition = seekBar.progress
            if(currentPosition > 0) {
                val newPosition = currentPosition - 1
                seekBar.progress = newPosition
                if(newPosition != lastPosition.get()) {
                    lastPosition.set(newPosition)
                    val selectedValue = valueList[newPosition]
                    callback.updateConfigValue(selectedValue)
                    callback.setupTextView(selectedValue)
                    VibrationHelper.vibrateOnClick(viewModel)
                }
            }
        }

        // 设置增加按钮的点击事件
        textViewIncrease?.setOnClickListener {
            val currentPosition = seekBar.progress
            if(currentPosition < seekBar.max) {
                val newPosition = currentPosition + 1
                seekBar.progress = newPosition
                if(newPosition != lastPosition.get()) {
                    lastPosition.set(newPosition)
                    val selectedValue = valueList[newPosition]
                    callback.updateConfigValue(selectedValue)
                    callback.setupTextView(selectedValue)
                    VibrationHelper.vibrateOnClick(viewModel)
                }
            }
        }

        // 设置减少按钮的长按事件
        textViewDecrease?.isHapticFeedbackEnabled = false
        textViewDecrease?.setOnLongClickListener {
            // 长按震动反馈
            VibrationHelper.vibrateOnLongPress(viewModel)
            startContinuousAdjustment(
                textView = it as TextView
            )
            true
        }

        // 设置增加按钮的长按事件
        textViewIncrease?.isHapticFeedbackEnabled = false
        textViewIncrease?.setOnLongClickListener {
            // 长按震动反馈
            VibrationHelper.vibrateOnLongPress(viewModel)
            startContinuousAdjustment(
                textView = it as TextView
            )
            true
        }

        // 设置触摸监听器以停止连续调整
        val touchListener = View.OnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    stopContinuousAdjustment()
                }
            }
            false
        }

        textViewDecrease?.setOnTouchListener(touchListener)
        textViewIncrease?.setOnTouchListener(touchListener)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    val closestPosition = positions.minByOrNull { abs(it - progress) }
                    seekBar.progress = closestPosition ?: 0
                    if(seekBar.progress != lastPosition.get()) {
                        lastPosition.set(seekBar.progress)
                        val selectedValue = valueList[seekBar.progress]
                        callback.updateConfigValue(selectedValue)
                        callback.setupTextView(selectedValue)
                        VibrationHelper.vibrateOnClick(viewModel)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    /**
     * 开始连续调整进度
     */
    private fun startContinuousAdjustment(
        textView: TextView
    ) {
        stopContinuousAdjustment()  // 停止任何正在进行的调整

        val runnable = object : Runnable {
            override fun run() {
                textView.performClick()
                // 继续重复执行
                handler.postDelayed(this, REPEAT_INTERVAL_MS)
            }
        }

        currentRunnable = runnable
        // 延迟开始连续触发
        handler.postDelayed(runnable, LONG_PRESS_DELAY_MS)
    }

    /**
     * 停止连续调整
     */
    private fun stopContinuousAdjustment() {
        currentRunnable?.let {
            handler.removeCallbacks(it)
            currentRunnable = null
        }
    }
}
