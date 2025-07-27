/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import androidx.core.graphics.withTranslation
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R


class MyDividerItemDecoration(
    private val context: Context,
    private val dividerColor: Int = context.getColor(R.color.deep_gray),
    private val width: Int = 2,
    private val paddingStart: Int = 0,
    private val paddingEnd: Int = 0,
    private val customLayoutResId: Int? = null, // 添加自定义布局资源ID
) : RecyclerView.ItemDecoration() {


    private val paint = Paint().apply {
        this.color = dividerColor
        this.strokeWidth = width.toFloat()
    }

    private var dividerView: View? = null


    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (customLayoutResId != null) {
            // 使用自定义布局作为分隔符
            drawCustomDivider(c, parent)
        } else {
            // 使用默认的线条分隔符
            drawDefaultDivider(c, parent)
        }
    }

    private fun drawDefaultDivider(c: Canvas, parent: RecyclerView) {
        val left = parent.paddingLeft + paddingStart
        val right = parent.width - parent.paddingRight - paddingEnd

        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = child.bottom + params.bottomMargin
            val bottom = top + width

            c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
        }
    }

    private fun drawCustomDivider(c: Canvas, parent: RecyclerView) {
        if (dividerView == null && customLayoutResId != null) {
            dividerView = LayoutInflater.from(context).inflate(customLayoutResId, null)
        }

        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = child.bottom + params.bottomMargin

            dividerView?.let { view ->
                val dividerHeight = getViewHeight(view, parent)

                c.withTranslation(0f, top.toFloat()) {
                    view.layout(0, 0, parent.width, dividerHeight)
                    view.draw(this)
                }
            }
        }
    }

    private fun getViewHeight(view: View, parent: RecyclerView): Int {
        if (view.height <= 0) {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            return view.measuredHeight
        }
        return view.height
    }

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        if (customLayoutResId != null) {
            // 为自定义分隔符预留空间
            if (parent.getChildAdapterPosition(view) < (parent.adapter?.itemCount?.minus(1) ?: 0)) {
                val tempView = LayoutInflater.from(context).inflate(customLayoutResId, null)
                val dividerHeight = getViewHeight(tempView, parent)
                outRect.set(0, 0, 0, dividerHeight)
            } else {
                outRect.set(0, 0, 0, 0)
            }
        } else {
            // 为默认线条分隔符预留空间
            outRect.set(0, 0, 0, width)
        }
    }
}
