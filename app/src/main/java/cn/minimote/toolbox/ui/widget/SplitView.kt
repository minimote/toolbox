/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import cn.minimote.toolbox.constant.Config
import cn.minimote.toolbox.helper.OtherConfigHelper.getUiStateConfigValue
import cn.minimote.toolbox.helper.OtherConfigHelper.saveUiStateConfig
import cn.minimote.toolbox.helper.OtherConfigHelper.updateUiStateConfigValue
import cn.minimote.toolbox.viewModel.MyViewModel


class SplitView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var initialX: Float = 0f
    private var initialY: Float = 0f
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f
    private var parentHeight: Float = 0f
    private var minY = 0
    private var maxY = 0
    private var edgePercent = 0.1f
//    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private lateinit var viewModel: MyViewModel

    private var marginTop = 0

    private val marginTopConfigKey = Config.ConfigKeys.LONG_PRESS_MENU_MANAGEMENT_TOP_MARGIN


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val parentView = parent as? View
        if(parentView != null && parentHeight == 0f) {
            parentHeight = parentView.height.toFloat()
            updateLimits()
            setInitMarginTop()
//            LogHelper.e("触发onLayout", "parentHeight: $parentHeight")
        }
    }


    private fun updateLimits() {
        minY = (parentHeight * edgePercent).toInt()
        maxY = (parentHeight * (1 - edgePercent)).toInt() - this.height
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = this.x
                initialY = this.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaY = event.rawY - initialTouchY
//                if (abs(deltaY) > touchSlop) { // 只有移动超过阈值才更新
                updateConstraints((initialY + deltaY).toInt())
//                }
            }

            MotionEvent.ACTION_UP -> {
                viewModel.updateUiStateConfigValue(
                    key = marginTopConfigKey,
                    value = marginTop,
                )
                viewModel.saveUiStateConfig()
            }

        }
        return super.onTouchEvent(event)
    }


    private fun updateConstraints(newY: Int) {
        val parent = parent as? ConstraintLayout ?: return

        // 计算新的垂直bias
//        val verticalBias = (newY / parentHeight).coerceIn(edgePercent, 1 - edgePercent)

        // 使用ConstraintSet更新bias
        val constraintSet = ConstraintSet()
        constraintSet.clone(parent)

        marginTop = newY.coerceIn(minY, maxY)

        // 使用 ConstraintSet 设置 marginTop
        constraintSet.setMargin(
            id,
            ConstraintSet.TOP,
            marginTop
        )
//        constraintSet.setVerticalBias(id, verticalBias)

        constraintSet.applyTo(parent)
    }


    private fun setInitMarginTop() {
        marginTop = if(this::viewModel.isInitialized) {
            viewModel.getUiStateConfigValue(
                marginTopConfigKey,
            ) as? Int ?: ((minY + maxY) / 2)
        } else {
            (minY + maxY) / 2
        }

        updateConstraints(marginTop)
    }


    fun setParameters(viewModel: MyViewModel) {
        this.viewModel = viewModel
//        setInitMarginTop()
    }


}
