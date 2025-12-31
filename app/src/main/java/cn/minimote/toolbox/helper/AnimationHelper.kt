/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import cn.minimote.toolbox.constant.UI.ANIMATION_DURATION
import cn.minimote.toolbox.helper.AnimationHelper.scale

object AnimationHelper {


    /**
     * 放大缩小动画
     * @param originalScale 原始缩放比例
     * @param targetScale 目标缩放比例
     * @param duration 动画持续时间，默认使用全局动画时长
     * @param interpolator 动画插值器，默认使用加速减速插值器
     */
    fun View.scale(
        originalScale: Float,
        targetScale: Float,
        duration: Long = ANIMATION_DURATION,
        interpolator: Interpolator = AccelerateDecelerateInterpolator(),
    ) {
        this.scaleX = originalScale
        this.scaleY = originalScale

        this.animate()
            .scaleX(targetScale)
            .scaleY(targetScale)
            .setDuration(duration)
            .setInterpolator(interpolator)
            .start()
    }


    /**
     * 放大动画
     * @param duration 动画持续时间，默认使用全局动画时长
     * @param interpolator 动画插值器，默认使用加速减速插值器
     */
    fun View.scaleUp(
        duration: Long = ANIMATION_DURATION,
        interpolator: Interpolator = AccelerateDecelerateInterpolator(),
    ) {
        this.scale(
            originalScale = 0f,
            targetScale = 1f,
            duration = duration,
            interpolator = interpolator,
        )
    }


    /**
     * 缩小动画
     * @param scale 目标缩放比例，默认为0.0(完全缩小)
     * @param duration 动画持续时间，默认使用全局动画时长
     * @param interpolator 动画插值器，默认使用加速减速插值器
     */
    fun View.scaleDown(
        duration: Long = ANIMATION_DURATION,
        interpolator: Interpolator = AccelerateDecelerateInterpolator(),
    ) {
        this.scale(
            originalScale = 1f,
            targetScale = 0f,
            duration = duration,
            interpolator = interpolator,
        )
    }


    /**
     * 旋转动画
     * @param rotationFrom 起始旋转角度，默认为当前角度
     * @param rotationTo 目标旋转角度
     * @param duration 动画持续时间，默认使用全局动画时长
     * @param interpolator 动画插值器，默认使用加速减速插值器
     */
    fun View.rotate(
        rotationFrom: Float = this.rotation,
        rotationTo: Float,
        duration: Long = ANIMATION_DURATION,
        interpolator: Interpolator = AccelerateDecelerateInterpolator(),
    ) {
        this.rotation = rotationFrom

        this.animate()
            .rotation(rotationTo)
            .setDuration(duration)
            .setInterpolator(interpolator)
            .start()
    }


    /**
     * 透明度动画
     * @param originalAlpha 原始透明度 (0.0 = 完全透明, 1.0 = 完全不透明)
     * @param targetAlpha 目标透明度 (0.0 = 完全透明, 1.0 = 完全不透明)
     * @param duration 动画持续时间，默认使用全局动画时长
     * @param interpolator 动画插值器，默认使用加速减速插值器
     */
    fun View.changeAlpha(
        originalAlpha: Float,
        targetAlpha: Float,
        duration: Long = ANIMATION_DURATION,
        interpolator: Interpolator = AccelerateDecelerateInterpolator(),
    ) {
        this.alpha = originalAlpha

        this.animate()
            .alpha(targetAlpha)
            .setDuration(duration)
            .setInterpolator(interpolator)
            .start()
    }


    /**
     * 渐显动画
     * @param duration 动画持续时间，默认使用全局动画时长
     * @param interpolator 动画插值器，默认使用加速减速插值器
     */
    fun View.fadeIn(
        duration: Long = ANIMATION_DURATION,
        interpolator: Interpolator = AccelerateDecelerateInterpolator(),
    ) {
        this.changeAlpha(
            originalAlpha = 0f,
            targetAlpha = 1f,
            duration = duration,
            interpolator = interpolator,
        )
    }

    /**
     * 渐隐动画
     * @param duration 动画持续时间，默认使用全局动画时长
     * @param interpolator 动画插值器，默认使用加速减速插值器
     */
    fun View.fadeOut(
        duration: Long = ANIMATION_DURATION,
        interpolator: Interpolator = AccelerateDecelerateInterpolator(),
    ) {
        this.changeAlpha(
            originalAlpha = 1f,
            targetAlpha = 0f,
            duration = duration,
            interpolator = interpolator,
        )
    }


    /**
     * 取消正在进行的动画
     */
    fun View.cancelAnimation() {
        this.animate().cancel()
    }


}
