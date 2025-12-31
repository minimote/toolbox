/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.constant.UI.ANIMATION_DURATION

open class FixedSizeRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RecyclerView(context, attrs, defStyleAttr) {


//    private lateinit var viewModel: MyViewModel
//    lateinit var viewShadowTop: View
//    lateinit var viewShadowBottom: View


    init {
        setHasFixedSize(false)
        itemAnimator?.addDuration = ANIMATION_DURATION
    }


//    fun setShadow(
//        viewModel: MyViewModel,
//        viewShadowTop: View,
//        viewShadowBottom: View,
//    ) {
//        this.viewModel = viewModel
//        this.viewShadowTop = viewShadowTop
//        this.viewShadowBottom = viewShadowBottom
//
//
//        ShadowHelper.setShadow(
//            recyclerView = this,
//            viewShadowTop = viewShadowTop,
//            viewShadowBottom = viewShadowBottom,
//            viewModel = viewModel,
//        )
//    }
//
//
//    fun updateShadow() {
//        ShadowHelper.updateShadow(
//            viewShadowTop = viewShadowTop,
//            viewShadowBottom = viewShadowBottom,
//            layoutManager = layoutManager,
//        )
//    }

}