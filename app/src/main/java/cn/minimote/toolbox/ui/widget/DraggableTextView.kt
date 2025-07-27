/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.ui.widget

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import cn.minimote.toolbox.constant.Config
import cn.minimote.toolbox.constant.UI
import cn.minimote.toolbox.helper.ConfigHelper.getConfigValue
import cn.minimote.toolbox.helper.ConfigHelper.hasUserConfigKey
import cn.minimote.toolbox.helper.ConfigHelper.saveButtonPosition
import cn.minimote.toolbox.viewModel.MyViewModel
import kotlin.math.abs


class DraggableTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {

    private var initialX: Float = 0f
    private var initialY: Float = 0f
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f

    lateinit var viewModel: MyViewModel

    // 判断是否为横屏
//    private fun isLandscape(): Boolean {
//        return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
//    }

    // 获取父布局的宽度
    private fun getParentWidth(): Int {
        val parent = parent as View
//        return if(isLandscape()) {
//            parent.height
//        } else {
        return parent.width
//        }
    }

    // 获取父布局的高度
    private fun getParentHeight(): Int {
        val parent = parent as View
//        return if(isLandscape()) {
//            parent.width
//        } else {
        return parent.height
//        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 记录初始位置和触摸点
                initialX = this.x
                initialY = this.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                // 更新 TextView 的位置
                val dx = event.rawX - initialTouchX
                val dy = event.rawY - initialTouchY
                this.x = initialX + dx
                this.y = initialY + dy
            }

            MotionEvent.ACTION_UP -> {

                // 边界限制
                val targetPos = getNearestPosition()

                // 使用 ObjectAnimator 实现平滑移动
                val animatorX = ObjectAnimator.ofFloat(
                    this, "x", targetPos[0],
                )
                val animatorY = ObjectAnimator.ofFloat(
                    this, "y", targetPos[1],
                )
                // 设置动画时长
                animatorX.duration = UI.ANIMATION_DURATION
                animatorY.duration = UI.ANIMATION_DURATION
                animatorX.start()
                animatorY.start()


                // 判断是否是点击操作（移动距离小于阈值）
                val dx = abs(event.rawX - initialTouchX)
                val dy = abs(event.rawY - initialTouchY)
                if(dx < 1 && dy < 1) {
                    performClick()
                } else {
                    // 保存按钮位置
                    viewModel.saveButtonPosition(targetPos)
                }

            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        // 触发点击事件（用于无障碍服务）
        super.performClick()
        return true
    }


    // 获取最近位置
    fun getNearestPosition(pos: List<Float> = listOf(this.x, this.y)): List<Float> {
        val x = pos[0]
        val y = pos[1]
        // 调整位置到最近的边缘
        val parentWidth = getParentWidth()
        val parentHeight = getParentHeight()

        // 获取状态栏高度
        val statusBarHeight = getStatusBarHeight()

        // 判断距离哪边更近
        val leftDistance = abs(0 - x)
        val rightDistance = abs(parentWidth - (x + this.width))

        val topDistance = abs(0 - y)
        val bottomDistance = abs(parentHeight - (y + this.height))

        // 找出最小的距离
        val minDistance = listOf(
            leftDistance, rightDistance, topDistance, bottomDistance,
        ).min()

        // 目标坐标
        var targetX = this.x
        var targetY = this.y

        // 根据最小距离调整位置
        when(minDistance) {
            leftDistance -> targetX = 0f
            rightDistance -> targetX = parentWidth - this.width.toFloat()
            topDistance -> targetY = statusBarHeight
            bottomDistance -> targetY = parentHeight - this.height.toFloat()
        }

        targetX = maxOf(
            0f,
            minOf(targetX, parentWidth - this.width.toFloat()),
        )
        // 横屏时上下边界限制
        targetY = maxOf(
            0f,
            minOf(targetY, parentHeight - this.height.toFloat()),
        )

        return listOf(targetX, targetY)
    }


    // 获取状态栏高度
    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    private fun getStatusBarHeight(): Float {
        val resourceId = resources.getIdentifier(
            "status_bar_height", "dimen", "android",
        )

        val statusBarHeight = if(resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            UI.STATUS_BAR_HEIGHT * resources.displayMetrics.density
        }

        return statusBarHeight.toFloat()
    }


    // 设置初始位置
    fun setInitialPosition() {
        if(viewModel.hasUserConfigKey(Config.ConfigKeys.SAVE_BUTTON_POSITION)) {
            val pos =
                viewModel.getConfigValue(Config.ConfigKeys.SAVE_BUTTON_POSITION).toFloatList()
            val targetPos = getNearestPosition(pos)
            setPosition(targetPos)
            viewModel.saveButtonPosition(targetPos)
        } else {
            val parentHeight = getParentHeight()

            setPosition(listOf(0f, (parentHeight - this.height) / 2f))
        }
    }


    fun Any?.toFloatList(): List<Float> {
        return when(this) {
            is List<*> -> filterIsInstance<Float>()

            is org.json.JSONArray -> {
                val list = mutableListOf<Float>()
                for(i in 0 until length()) {
                    try {
                        list.add(get(i).toString().toFloat())
                    } catch(_: Exception) {
                        list.add(0f)
                    }
                }
                list
            }

            else -> emptyList()
        }
    }


    // 设置位置
    fun setPosition(pos: List<Float>) {
        this.x = pos[0]
        this.y = pos[1]
    }

}
