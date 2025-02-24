/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.pageTransformer

import android.view.View
import androidx.viewpager2.widget.ViewPager2


class ViewPager2Transformer : ViewPager2.PageTransformer {

    // 方块
    override fun transformPage(page: View, position: Float) {
        val maxRotation = 20f
        val minScale = 1f
        page.apply {
            pivotY = height / 2f
            when {
                position < -1 -> {
                    rotationY = -maxRotation
                    pivotX = width.toFloat()
                }

                position <= 1 -> {
                    rotationY = position * maxRotation
                    if(position < 0) {
                        pivotX = width.toFloat()
                        val scale =
                            minScale + 4f * (1f - minScale) * (position + 0.5f) * (position + 0.5f)
                        scaleX = scale
                        scaleY = scale
                    } else {
                        pivotX = 0f
                        val scale =
                            minScale + 4f * (1f - minScale) * (position - 0.5f) * (position - 0.5f)
                        scaleX = scale
                        scaleY = scale
                    }
                }

                else -> {
                    rotationY = maxRotation
                    pivotX = 0f
                }
            }
        }
    }


//    // 缩小
//    override fun transformPage(view: View, position: Float) {
//        val minScale = 0.85f
//        val minAlpha = 0.5f
//        view.apply {
//            val pageWidth = width
//            val pageHeight = height
//            when {
//                position < -1 -> { // [-Infinity,-1)
//                    // This page is way off-screen to the left.
//                    alpha = 0f
//                }
//
//                position <= 1 -> { // [-1,1]
//                    // Modify the default slide transition to shrink the page as well.
//                    val scaleFactor = minScale.coerceAtLeast(1 - abs(position))
//                    val vMargin = pageHeight * (1 - scaleFactor) / 2
//                    val hMargin = pageWidth * (1 - scaleFactor) / 2
//                    translationX = if(position < 0) {
//                        hMargin - vMargin / 2
//                    } else {
//                        hMargin + vMargin / 2
//                    }
//
//                    // Scale the page down (between MIN_SCALE and 1).
//                    scaleX = scaleFactor
//                    scaleY = scaleFactor
//
//                    // Fade the page relative to its size.
//                    alpha = (minAlpha +
//                            (((scaleFactor - minScale) / (1 - minScale)) * (1 - minAlpha)))
//                }
//
//                else -> { // (1,+Infinity]
//                    // This page is way off-screen to the right.
//                    alpha = 0f
//                }
//            }
//        }
//    }


//    // 堆叠
//    override fun transformPage(view: View, position: Float) {
//        val minScale = 0.85f
//        view.apply {
//            val pageWidth = width
//            when {
//                position < -1 -> { // [-Infinity,-1)
//                    // This page is way off-screen to the left.
//                    alpha = 0f
//                }
//
//                position <= 0 -> { // [-1,0]
//                    // Use the default slide transition when moving to the left page.
//                    alpha = 1f
//                    translationX = 0f
//                    translationZ = 0f
//                    scaleX = 1f
//                    scaleY = 1f
//                }
//
//                position <= 1 -> { // (0,1]
//                    // Fade the page out.
//                    alpha = 1 - position
//
//                    // Counteract the default slide transition.
//                    translationX = pageWidth * -position
//                    // Move it behind the left page.
//                    translationZ = -1f
//
//                    // Scale the page down (between MIN_SCALE and 1).
//                    val scaleFactor = (minScale + (1 - minScale) * (1 - Math.abs(position)))
//                    scaleX = scaleFactor
//                    scaleY = scaleFactor
//                }
//
//                else -> { // (1,+Infinity]
//                    // This page is way off-screen to the right.
//                    alpha = 0f
//                }
//            }
//        }
//    }


//    // 淡入淡出
//    override fun transformPage(view: View, position: Float) {
//        view.apply {
//            if(position < -1) {
//                // 页面在左边
//                view.alpha = 0f
//            } else if(position <= 0) {
//                // 页面从左边滑动到中间
//                view.alpha = 1f + position
//                view.translationX = -view.width * position
//            } else if(position <= 1) {
//                // 页面从中间滑动到右边
//                view.alpha = 1f - position
//                view.translationX = -view.width * position
//            } else {
//                // 页面在右边
//                view.alpha = 0f
//            }
//        }
//    }
}