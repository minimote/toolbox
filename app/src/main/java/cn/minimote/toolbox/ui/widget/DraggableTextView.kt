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
import android.view.ViewConfiguration
import androidx.annotation.RequiresApi
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.Config
import cn.minimote.toolbox.constant.UI
import cn.minimote.toolbox.helper.DimensionHelper.getLayoutSize
import cn.minimote.toolbox.helper.OtherConfigHelper.getUiStateConfigValue
import cn.minimote.toolbox.helper.OtherConfigHelper.saveUiStateConfig
import cn.minimote.toolbox.helper.OtherConfigHelper.updateUiStateConfigValue
import cn.minimote.toolbox.helper.TypeConversionHelper.toFloatList
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

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private val gapSize = if(getStatusBarHeight() > 0) {
        getStatusBarHeight()
    } else {
        getLayoutSize(
            context,
            R.dimen.layout_size_save_button_gapSize,
        ).toFloat()
    }


    lateinit var viewModel: MyViewModel

    private val buttonPositionConfigKey = Config.ConfigKeys.SAVE_BUTTON_POSITION

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


    private fun getMinX(): Float {
        return gapSize
    }

    private fun getMaxX(): Float {
        return getParentWidth().toFloat() - gapSize - this.width.toFloat()
    }

    private fun getLegalX(x: Float): Float {
        val leftDistance = abs(getMinX() - x)
        val rightDistance = abs(getMaxX() - x)
        return when {
            getMinX() <= x && x <= getMaxX() -> x
            leftDistance <= rightDistance -> getMinX()
            rightDistance < leftDistance -> getMaxX()
            else -> getMaxX()
        }
    }

    private fun getMinY(): Float {
        return gapSize
    }

    private fun getMaxY(): Float {
        return getParentHeight().toFloat() - gapSize - this.height.toFloat()
    }

    private fun getLegalY(y: Float): Float {
        val topDistance = abs(getMinY() - y)
        val bottomDistance = abs(getMaxY() - y)
        return when {
            getMinY() <= y && y <= getMaxY() -> y
            topDistance <= bottomDistance -> getMinY()
            bottomDistance < topDistance -> getMaxY()
            else -> getMinY()
        }
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
                val newX = initialX + event.rawX - initialTouchX
                val newY = initialY + event.rawY - initialTouchY

                this.x = maxOf(
                    gapSize,
                    minOf(
                        newX,
                        getParentWidth().toFloat() - this.width - gapSize
                    ),
                )
                this.y = maxOf(
                    gapSize,
                    minOf(
                        newY,
                        getParentHeight().toFloat() - this.height - gapSize
                    ),
                )

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
                if(dx < touchSlop && dy < touchSlop) {
                    performClick()
                } else {
                    // 保存按钮位置
                    saveButtonPosition(targetPos)
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
    private fun getNearestPosition(pos: List<Float> = listOf(this.x, this.y)): List<Float> {
        var targetX = getLegalX(pos[0])
        var targetY = getLegalY(pos[1])
        // 调整位置到最近的边缘
//        val parentWidth = getParentWidth()
//        val parentHeight = getParentHeight()
//        Toast.makeText(context, "宽度${parentWidth},高度${parentHeight}", Toast.LENGTH_SHORT).show()

//        // 获取状态栏高度
//        val statusBarHeight = getStatusBarHeight()

        // 判断距离哪边更近
        val leftDistance = abs(getMinX() - targetX)
        val rightDistance = abs(getMaxX() - targetX)

        val topDistance = abs(getMinY() - targetY)
        val bottomDistance = abs(getMaxY() - targetY)

        // 找出最小的距离
        val minDistance = listOf(
            leftDistance, rightDistance,
            topDistance, bottomDistance,
        ).min()


        // 根据最小距离调整位置
        when(minDistance) {
            leftDistance -> targetX = getMinX()
            rightDistance -> targetX = getMaxX()
            topDistance -> targetY = getMinY()
            bottomDistance -> targetY = getMaxY()
        }

        return listOf(targetX, targetY)
    }


    // 获取状态栏高度
    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    private fun getStatusBarHeight(): Float {
        val resourceId = resources.getIdentifier(
            "status_bar_height", "dimen", "android",
        )

        val statusBarHeight = if(resourceId > 0) {
            getLayoutSize(
                context, resourceId,
            )
        } else {
            UI.STATUS_BAR_HEIGHT * resources.displayMetrics.density
        }

        return statusBarHeight.toFloat()
    }


    // 设置初始位置
    fun setInitialPosition() {
//        if(viewModel.hasUserConfigKey(Config.ConfigKeys.SAVE_BUTTON_POSITION)) {
//            val pos = viewModel.getConfigValue(
//                Config.ConfigKeys.SAVE_BUTTON_POSITION
//            ).toFloatList()
//            val targetPos = getNearestPosition(pos)
//            setPosition(targetPos)
//            saveButtonPosition(targetPos)
//        } else {
//            val parentWidth = getParentWidth()
//            val pos = listOf(parentWidth.toFloat(), 0f)
//            val targetPos = getNearestPosition(pos)
////            Toast.makeText(
////                context,
////                "[${pos[0]},${pos[1]}],[${targetPos[0]},${targetPos[1]}]",
////                Toast.LENGTH_SHORT
////            ).show()
//            setPosition(targetPos)
//        }

        val pos = getButtonPosition()
        val targetPos = getNearestPosition(pos)
        setPosition(targetPos)
        saveButtonPosition(targetPos)

    }


    private fun getButtonPosition(): List<Float> {
        return viewModel.getUiStateConfigValue(
            key = buttonPositionConfigKey,
        ).toFloatList().takeIf { it.size == 2 } ?: run {
            val parentWidth = getParentWidth()
            listOf(parentWidth.toFloat(), 0f)
        }
    }


    // 保存按钮的位置
    private fun saveButtonPosition(
        targetPos: List<Float>,
    ) {
        viewModel.updateUiStateConfigValue(
            key = buttonPositionConfigKey,
            value = targetPos,
        )

        viewModel.saveUiStateConfig()
    }


    // 设置位置
    private fun setPosition(pos: List<Float>) {
        this.x = pos[0]
        this.y = pos[1]
    }

}
