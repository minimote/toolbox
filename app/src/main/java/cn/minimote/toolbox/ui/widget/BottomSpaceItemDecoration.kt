/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.ui.widget

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.helper.DimensionHelper
import cn.minimote.toolbox.helper.LogHelper


class BottomSpaceItemDecoration(
    private val spaceHeight: Int? = null,
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val actualSpaceHeight = spaceHeight ?: DimensionHelper.getLayoutSize(
            context = parent.context,
            layoutDimensionId = R.dimen.layout_size_fragment_gapSize,
        )

        if(actualSpaceHeight <= 0) {
            return
        }

        val position = parent.getChildAdapterPosition(view)
        val adapter = parent.adapter

        // 检查是否为最后一个可见项目
        if(adapter != null && position == adapter.itemCount - 1) {
            outRect.bottom = actualSpaceHeight
            LogHelper.e("底部计算", "当前位置：$position，数量：${adapter.itemCount}")
        }

//        val position = parent.getChildAdapterPosition(view)
//        if(position == (parent.adapter?.itemCount?.minus(1) ?: 0)) {
//            outRect.bottom = actualSpaceHeight
//        }
    }
}
