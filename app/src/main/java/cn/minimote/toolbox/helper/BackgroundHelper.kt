/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.UI
import cn.minimote.toolbox.helper.DimensionHelper.setTextSize
import cn.minimote.toolbox.viewModel.MyViewModel


object BackgroundHelper {

    fun setBackgroundImage(
        imageViewBackground: ImageView,
        viewModel: MyViewModel,
        heightRate: Float = if(viewModel.isWatch) {
            UI.BackgroundImage.SizeRate.Normal.WIDTH_RATE_WATCH
        } else {
            UI.BackgroundImage.SizeRate.Normal.WIDTH_RATE
        },
        widthRate: Float = if(viewModel.isWatch) {
            UI.BackgroundImage.SizeRate.Normal.HEIGHT_RATE_WATCH
        } else {
            UI.BackgroundImage.SizeRate.Normal.HEIGHT_RATE
        },
        alpha: Float = UI.BackgroundImage.ALPHA,
    ) {

        val layoutParams = imageViewBackground.layoutParams as ConstraintLayout.LayoutParams

        // 修改高度百分比
        layoutParams.matchConstraintPercentHeight = heightRate
        // 修改宽度百分比
        layoutParams.matchConstraintPercentWidth = widthRate

        // 应用更改
        imageViewBackground.layoutParams = layoutParams

        imageViewBackground.alpha = alpha
    }


    fun setBackgroundText(
        viewModel: MyViewModel,
        textViewBackground: TextView,
        alpha: Float = UI.BackgroundText.ALPHA,
        textSizeDimensionId: Int = R.dimen.text_size_5_large,
    ) {
        textViewBackground.visibility = View.INVISIBLE
        textViewBackground.alpha = alpha
        setTextSize(
            textView = textViewBackground,
            textSizeDimensionId = textSizeDimensionId,
            context = viewModel.myContext,
        )
        textViewBackground.visibility = if(viewModel.isWatch) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }


    fun setBackgroundImageAndTextSearchMode(
        viewModel: MyViewModel,
        imageViewBackground: ImageView,
        heightRate: Float = if(viewModel.isWatch) {
            UI.BackgroundImage.SizeRate.Search.HEIGHT_RATE_WATCH
        } else {
            UI.BackgroundImage.SizeRate.Search.HEIGHT_RATE
        },
        widthRate: Float = if(viewModel.isWatch) {
            UI.BackgroundImage.SizeRate.Search.WIDTH_RATE_WATCH
        } else {
            UI.BackgroundImage.SizeRate.Search.WIDTH_RATE
        },
        imageAlpha: Float = UI.BackgroundImage.ALPHA,
        textViewBackground: TextView,
        textAlpha: Float = UI.BackgroundText.ALPHA,
        textSizeDimensionId: Int = R.dimen.text_size_5_large,
    ) {
        setBackgroundImage(
            imageViewBackground = imageViewBackground,
            viewModel = viewModel,
            heightRate = heightRate,
            widthRate = widthRate,
            alpha = imageAlpha,
        )
        setBackgroundText(
            textViewBackground = textViewBackground,
            viewModel = viewModel,
            alpha = textAlpha,
            textSizeDimensionId = textSizeDimensionId,
        )
        textViewBackground.text = viewModel.myContext.getString(
            R.string.search_mode
        )
    }
}