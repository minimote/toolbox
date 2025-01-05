/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.others

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R


class DividerItemDecoration(
    private val context: Context,
    private val colorResId: Int = R.color.deep_gray,
    private val width: Int = 2,
    private val paddingStart: Int = 0,
    private val paddingEnd: Int = 0,
) : RecyclerView.ItemDecoration() {

    private val paint = Paint().apply {
        this.color = ContextCompat.getColor(context, colorResId)
        this.strokeWidth = width.toFloat()
    }


    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingStart + paddingStart
        val right = parent.width - parent.paddingEnd - paddingEnd

        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val currentChild = parent.getChildAt(i)
            val nextChild = parent.getChildAt(i + 1) ?: continue
            val paramsCurrent = currentChild.layoutParams as RecyclerView.LayoutParams
            val paramsNext = nextChild.layoutParams as RecyclerView.LayoutParams


            // 计算当前项和下一项的中间位置
            val topCurrent = currentChild.bottom + paramsCurrent.bottomMargin
            val topNext = nextChild.top - paramsNext.topMargin
            if (topNext <= topCurrent) continue
            val mid = (topCurrent + topNext) / 2f

            c.drawLine(left.toFloat(), mid, right.toFloat(), mid, paint)
        }
    }

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.set(0, 0, 0, width)
    }
}
