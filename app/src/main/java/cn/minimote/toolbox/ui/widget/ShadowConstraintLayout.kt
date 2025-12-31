/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import cn.minimote.toolbox.R
import cn.minimote.toolbox.helper.DeviceHelper
import cn.minimote.toolbox.helper.DimensionHelper
import cn.minimote.toolbox.helper.ShadowHelper
import cn.minimote.toolbox.viewModel.MyViewModel
import com.scwang.smart.refresh.layout.SmartRefreshLayout


class ShadowConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {


    lateinit var recyclerView: FixedSizeRecyclerView
    lateinit var refreshLayout: SmartRefreshLayout
    private lateinit var viewShadowTop: View
    private lateinit var viewShadowBottom: View


    // 判断设备是否为手表
    val isWatch: Boolean get() = DeviceHelper.isWatch(context)


    // 底部间距高度
    var spaceHeight = 0

    // 在布局完成后初始化视图引用
    override fun onFinishInflate() {
        super.onFinishInflate()
        try {
            recyclerView = findViewById(R.id.recyclerView)
            refreshLayout = findViewById(R.id.refreshLayout)
            viewShadowTop = findViewById(R.id.view_shadow_top)
            viewShadowBottom = findViewById(R.id.view_shadow_bottom)
//            refreshLayout.setOnDragListener {
//                return@setOnDragListener true
//            }

        } catch(e: Exception) {
            // 处理找不到视图的情况
            e.printStackTrace()
        }
    }


    fun setShadow(
        viewModel: MyViewModel,
        addBottomPadding: Boolean = true,
        shadowTopBackgroundResId: Int? = null,
        shadowBottomBackgroundResId: Int? = null,
    ) {
        shadowTopBackgroundResId?.let {
            setShadowBackground(viewShadowTop, it)
        }
        shadowBottomBackgroundResId?.let {
            setShadowBackground(viewShadowBottom, it)
        }

        spaceHeight = if(addBottomPadding) {
            DimensionHelper.getLayoutSize(
                context = context,
                layoutDimensionId = R.dimen.layout_size_bottom_sheet_cancel_margin,
//                layoutDimensionId = R.dimen.layout_size_3_large,
            )
        } else {
            0
        }

        ShadowHelper.setShadow(
            recyclerView = recyclerView,
            viewShadowTop = viewShadowTop,
            viewShadowBottom = viewShadowBottom,
            viewModel = viewModel,
            spaceHeight = spaceHeight,
        )


        if(addBottomPadding) {
            recyclerView.setPadding(
                recyclerView.paddingLeft,
                recyclerView.paddingTop,
                recyclerView.paddingRight,
                spaceHeight,
            )
            recyclerView.clipToPadding = false
//            recyclerView.addItemDecoration(
//                BottomSpaceItemDecoration(
//                    spaceHeight = spaceHeight,
//                )
//            )
        }
    }


    fun updateShadow() {
        ShadowHelper.updateShadow(
            viewShadowTop = viewShadowTop,
            viewShadowBottom = viewShadowBottom,
            recyclerView = recyclerView,
//            isWatch = isWatch,
            spaceHeight = spaceHeight,
        )
    }


    fun setShadowBackground(
        viewShadow: View,
        shadowBackgroundResId: Int,
    ) {
        viewShadow.background = AppCompatResources.getDrawable(
            context, shadowBackgroundResId
        )
    }


}
