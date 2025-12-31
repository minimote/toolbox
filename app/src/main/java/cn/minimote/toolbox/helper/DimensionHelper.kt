/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.content.Context
import android.util.TypedValue
import android.widget.TextView

object DimensionHelper {

    fun setTextSize(
        textView: TextView,
        textSizeDimensionId: Int,
        context: Context,
    ) {
        textView.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            context.resources.getDimension(textSizeDimensionId)
        )
    }


    fun getLayoutSize(
        context: Context,
        layoutDimensionId: Int,
        rate: Float = 1f,
    ): Int {
        return (context.resources.getDimensionPixelSize(layoutDimensionId) * rate).toInt()
    }

}