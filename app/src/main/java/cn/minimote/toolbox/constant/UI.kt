/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

import android.view.animation.LinearInterpolator

object UI {

    object Alpha {
        const val ALPHA_0 = 0f
        const val ALPHA_1 = 0.1f
        const val ALPHA_3 = 0.3f
        const val ALPHA_5 = 0.5f
        const val ALPHA_7 = 0.7f
        const val ALPHA_9 = 0.9f
        const val ALPHA_10 = 1f
    }


    // 全屏图片比例
    const val FULL_SCREEN_IMAGE_RATE = 0.7f


    // 动画时长(ms)
    const val ANIMATION_DURATION = 300L
    const val ANIMATION_DURATION_SEARCH_MODE = ANIMATION_DURATION / 2


    // 插值器
    object Interpolator {
        val LINEAR = LinearInterpolator()
    }


    // 状态栏高度(dp)
    const val STATUS_BAR_HEIGHT = 40


    // 对话框宽度比例
    const val DIALOG_WIDTH_RATE = 0.85
    // 对话框高度比例
    const val DIALOG_HEIGHT_RATE = 0.5f


    object BackgroundImage {
        // 背景图片透明度
        const val ALPHA = Alpha.ALPHA_3

        // 背景图片大小比例
        object SizeRate {
            object Normal {
                const val WIDTH_RATE = 0.7f
                const val HEIGHT_RATE = WIDTH_RATE

                const val WIDTH_RATE_WATCH = 0.5f
                const val HEIGHT_RATE_WATCH = WIDTH_RATE_WATCH
            }

            object Search {
                const val WIDTH_RATE = 0.6f
                const val HEIGHT_RATE = WIDTH_RATE

                const val WIDTH_RATE_WATCH = 0.5f
                const val HEIGHT_RATE_WATCH = WIDTH_RATE_WATCH
            }
        }
    }


    // 背景图片文字
    object BackgroundText {
        const val ALPHA = BackgroundImage.ALPHA
    }

}