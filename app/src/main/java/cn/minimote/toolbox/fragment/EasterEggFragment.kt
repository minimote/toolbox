/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.fragment

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.adapter.EasterEggAdapter
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.ui.widget.ShadowConstraintLayout
import cn.minimote.toolbox.viewModel.MyViewModel
import java.util.LinkedList
import java.util.Queue
import java.util.Random
import kotlin.math.abs


/**
 * 彩蛋 Fragment
 * 进入时播放彩蛋动画
 */
class EasterEggFragment : Fragment() {

    // 直接显示彩蛋内容
    private var showEasterEggDirectly = false

    private lateinit var meteorContainer: FrameLayout
    private lateinit var easterEggImage: ImageView

    private lateinit var shadowConstraintLayout: ShadowConstraintLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EasterEggAdapter

    private lateinit var constraintLayoutRoot: ConstraintLayout
    private lateinit var trailContainer: FrameLayout // 用于显示手指轨迹的容器

    private val viewModel: MyViewModel by activityViewModels()
    private val myActivity: MainActivity get() = requireActivity() as MainActivity

    private val meteors = mutableListOf<View>()
    private val meteorSpeeds = mutableListOf<Float>()
    private val meteorInitialSpeeds = mutableListOf<Float>() // 保存初始速度
    private val random = Random()
    private val handler = Handler(Looper.getMainLooper())
    private var isAccelerating = false // 是否正在加速
    private var accelerationStartTime = 0L // 加速开始时间
    private val ACCELERATION_DURATION = 3000L // 加速到最大速度的时间（毫秒）
    private val REVEAL_DURATION = 6000L // 触发揭示动画的时间（毫秒）
    private val MAX_SPEED_MULTIPLIER = 9f // 最大速度倍数
    private val MIN_SHAKE_DURATION = 20L // 抖动动画最小持续时间（毫秒）
    private val MAX_SHAKE_DURATION = 200L // 初始抖动持续时间使动画更快开始明显变化
    private var shakeAnimation: Animation? = null // 抖动动画
    private var isShaking = false // 是否正在抖动
    private var isRevealed = false // 是否已揭示彩蛋内容

    // 手指轨迹相关变量
    private val touchTrailPoints: Queue<Pair<Float, Float>> = LinkedList()
    private var trailView: TrailView? = null
    private var lastTrailX = 0f
    private var lastTrailY = 0f


    private val meteorRunnable = object : Runnable {
        override fun run() {
            moveMeteors()

            // 检查是否达到揭示时间
            if(isAccelerating && !isRevealed) {
                val elapsed = System.currentTimeMillis() - accelerationStartTime
                if(elapsed >= REVEAL_DURATION) {
                    revealEasterEgg()
                    isRevealed = true
                }
            }

            handler.postDelayed(this, 16) // 约60 FPS
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //  仅用于调试
//        showEasterEggDirectly = true

        return inflater.inflate(R.layout.fragment_easter_egg, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 隐藏状态栏
        hideStatusBar()

        constraintLayoutRoot = view.findViewById(R.id.constraintLayout_root)
        meteorContainer = view.findViewById(R.id.meteorContainer)
        easterEggImage = view.findViewById(R.id.easterEggImage)

//        easterEggImage.setImageDrawable(
//            ContextCompat.getDrawable(
//                myActivity,
//                R.mipmap.ic_launcher_round,
//            )?.toCircularDrawable(myActivity)
//        )

        shadowConstraintLayout =
            view.findViewById(R.id.include_shadow_constraintLayout_recyclerView)
//        shadowConstraintLayout = shadowConstraintLayout.refreshLayout
        recyclerView = shadowConstraintLayout.recyclerView

//        refreshLayout = view.findViewById(R.id.refreshLayout_easterEggContent)
//        recyclerView = view.findViewById(R.id.recyclerView_easterEggContent)

        trailContainer = view.findViewById(R.id.trailContainer) // 获取布局中的轨迹容器

        // 初始化 RecyclerView 和 Adapter
        setupRecyclerView()

        if(showEasterEggDirectly) {
            // 直接显示彩蛋内容，跳过流星动画
            revealEasterEggDirectly()
        } else {
            // 创建流星背景
            createMeteors()

            // 设置触摸监听器处理长按和释放
            @SuppressLint("ClickableViewAccessibility")
            constraintLayoutRoot.setOnTouchListener { _, event ->
//            LogHelper.e("TouchListener", "${event.action}")
                handleTouchForTrail(event) // 处理手指轨迹
                when(event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // 用户按下，开始加速
                        startAcceleration()
                        // 添加长按振动效果
                        VibrationHelper.vibrateOnEasterEggPress(viewModel)
                        true
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        // 用户释放，停止加速
                        stopAcceleration()
                        // 只有在未揭示时才停止振动
                        if(!isRevealed) {
                            VibrationHelper.cancelVibration(viewModel)
                        }
                        true
                    }

                    else -> false
                }
            }

            // 启动流星动画
            handler.post(meteorRunnable)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(meteorRunnable)
        stopShakeAnimation()
        // 停止振动
        VibrationHelper.cancelVibration(viewModel)
        // 恢复状态栏显示
        showStatusBar()
    }


    private fun setupRecyclerView() {
        adapter = EasterEggAdapter(
            myActivity = myActivity,
            viewModel = viewModel,
        )
        recyclerView.adapter = adapter

        recyclerView.layoutManager = LinearLayoutManager(myActivity)
        shadowConstraintLayout.visibility = View.INVISIBLE
        shadowConstraintLayout.setShadow(viewModel = viewModel)
    }


    /**
     * 直接显示彩蛋内容，跳过动画效果
     */
    private fun revealEasterEggDirectly() {
        // 隐藏流星容器和彩蛋图像
        meteorContainer.visibility = View.GONE
        easterEggImage.visibility = View.GONE

        // 直接显示内容
        shadowConstraintLayout.visibility = View.VISIBLE
        shadowConstraintLayout.alpha = 1.0f
    }


    /**
     * 处理手指轨迹的触摸事件
     */
    private fun handleTouchForTrail(event: MotionEvent) {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 立即停止任何正在进行的淡出动画并清除轨迹
                trailView?.cancelFadeOut()
                clearPreviousTrail()

                // 清除之前的轨迹点
                touchTrailPoints.clear()
                lastTrailX = event.x
                lastTrailY = event.y
                touchTrailPoints.offer(Pair(event.x, event.y))

                // 创建新的轨迹视图
                trailView = TrailView(requireContext())
                trailContainer.addView(trailView)
            }

            MotionEvent.ACTION_MOVE -> {
                val currentX = event.x
                val currentY = event.y

                // 只有当手指移动距离超过一定阈值时才添加新的轨迹点
                if(abs(currentX - lastTrailX) > 5 || abs(currentY - lastTrailY) > 5) {
                    touchTrailPoints.offer(Pair(currentX, currentY))
                    lastTrailX = currentX
                    lastTrailY = currentY

                    // 保持轨迹点数量不超过50个，避免过多内存消耗
                    if(touchTrailPoints.size > 50) {
                        touchTrailPoints.poll()
                    }

                    // 更新轨迹视图
                    trailView?.updateTrailPoints(touchTrailPoints.toList())
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 手指离开时，逐渐淡化轨迹
                trailView?.fadeOut()
            }
        }
    }


    /**
     * 清除之前绘制的轨迹
     */
    private fun clearPreviousTrail() {
        trailContainer.removeAllViews()
        trailView = null
    }


    /**
     * 自定义轨迹视图类
     */
    inner class TrailView(context: android.content.Context) : View(context) {
        private val paint = Paint()
        private val trailPoints = mutableListOf<Pair<Float, Float>>()
        private var fadeOutProgress = 0f
        private val handler = Handler(Looper.getMainLooper())
        private var isFadingOut = false
        private var fadeOutRunnable: Runnable? = null
        // 预分配Point对象以避免在onDraw中创建新对象
        private val startPoint = PointF()
        private val endPoint = PointF()
        // 预分配Path对象以避免在onDraw中创建新对象
        private val path = Path()
        // 预分配颜色数组以避免在onDraw中创建新对象
        private val colors = intArrayOf(
            "#E4658A".toColorInt(), // 红
            "#FD7D33".toColorInt(), // 橙
            "#F0C722".toColorInt(), // 黄
            "#8AD662".toColorInt(), // 绿
            "#37C9C3".toColorInt(), // 蓝
            "#379FF4".toColorInt(), // 蓝
            "#9A6EF6".toColorInt(), // 紫
        )

        init {
            paint.strokeWidth = 15f
            paint.style = Paint.Style.STROKE
            paint.strokeJoin = Paint.Join.ROUND
            paint.strokeCap = Paint.Cap.ROUND
            paint.alpha = 150 // 初始透明度约为60%
            paint.isAntiAlias = true // 启用抗锯齿，使线条更平滑
        }

        /**
         * 更新轨迹点并重绘
         */
        fun updateTrailPoints(points: List<Pair<Float, Float>>) {
            trailPoints.clear()
            trailPoints.addAll(points)
            invalidate()
        }

        /**
         * 取消淡出动画
         */
        fun cancelFadeOut() {
            isFadingOut = false
            fadeOutRunnable?.let { handler.removeCallbacks(it) }
            fadeOutRunnable = null
        }

        /**
         * 开始淡出动画
         */
        fun fadeOut() {
            // 如果已经在淡出，先取消
            cancelFadeOut()

            isFadingOut = true
            fadeOutProgress = 0f
            startFadeOutAnimation()
        }

        private fun startFadeOutAnimation() {
            fadeOutRunnable = object : Runnable {
                override fun run() {
                    if(fadeOutProgress < 1f) {
                        fadeOutProgress += 0.05f
                        val alpha = (150 * (1 - fadeOutProgress)).toInt()
                        paint.alpha = alpha.coerceIn(0, 255)
                        invalidate()

                        handler.postDelayed(this, 30)
                    } else {
                        // 动画结束后移除视图
                        trailContainer.removeView(this@TrailView)
                        trailView = null
                        fadeOutRunnable = null
                    }
                }
            }
            fadeOutRunnable?.let { handler.post(it) }
        }

        @SuppressLint("DrawAllocation")
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            if(trailPoints.size > 1) {
                // 使用预分配的Point对象避免在onDraw中创建新对象
                startPoint.set(trailPoints.first().first, trailPoints.first().second)
                endPoint.set(trailPoints.last().first, trailPoints.last().second)

                // 使用 LinearGradient 创建渐变色
                val shader = LinearGradient(
                    startPoint.x,
                    startPoint.y,
                    endPoint.x,
                    endPoint.y,
                    colors,
                    null,
                    Shader.TileMode.CLAMP
                )

                paint.shader = shader

                // 重置路径以重用它
                path.reset()

                // 移动到第一个点
                path.moveTo(trailPoints[0].first, trailPoints[0].second)

                // 如果只有两个点，直接绘制直线
                if(trailPoints.size == 2) {
                    path.lineTo(trailPoints[1].first, trailPoints[1].second)
                } else {
                    // 对于多个点，使用贝塞尔曲线创建平滑轨迹
                    for(i in 1 until trailPoints.size - 1) {
                        // 计算控制点（使用前后点的中点）
                        val prevPoint = trailPoints[i - 1]
                        val currentPoint = trailPoints[i]
                        val nextPoint = trailPoints[i + 1]

                        // 计算进入控制点（前一点和当前点的中点）
                        val controlX1 = (prevPoint.first + currentPoint.first) / 2
                        val controlY1 = (prevPoint.second + currentPoint.second) / 2

                        // 计算离开控制点（当前点和后一点的中点）
                        val controlX2 = (currentPoint.first + nextPoint.first) / 2
                        val controlY2 = (currentPoint.second + nextPoint.second) / 2

                        // 绘制贝塞尔曲线到下一个点
                        path.cubicTo(
                            controlX1, controlY1,
                            controlX2, controlY2,
                            nextPoint.first, nextPoint.second
                        )
                    }
                }

                // 绘制轨迹路径
                canvas.drawPath(path, paint)

                // 清除shader，避免影响其他绘制
                paint.shader = null
            }
        }
    }


    /**
     * 开始加速
     */
    private fun startAcceleration() {
        isAccelerating = true
        accelerationStartTime = System.currentTimeMillis()
        startShakeAnimation() // 开始抖动动画
        isRevealed = false // 重置揭示状态
    }


    /**
     * 停止加速
     */
    private fun stopAcceleration() {
        isAccelerating = false
        // 恢复初始速度
        for(i in meteorSpeeds.indices) {
            meteorSpeeds[i] = meteorInitialSpeeds[i]
        }
        stopShakeAnimation() // 停止抖动动画
    }


    /**
     * 开始抖动动画
     */
    private fun startShakeAnimation() {
        if(!isShaking) {
            isShaking = true
            updateShakeAnimation()
        }
    }


    /**
     * 停止抖动动画
     */
    private fun stopShakeAnimation() {
        isShaking = false
        easterEggImage.clearAnimation()
        shakeAnimation = null
    }


    /**
     * 更新抖动动画速度
     */
    private fun updateShakeAnimation() {
        if(!isShaking) return

        easterEggImage.clearAnimation()
        val animationSet = AnimationSet(true)

        // 计算当前加速进度，与流星保持完全同步
        val elapsed = System.currentTimeMillis() - accelerationStartTime
        val progress = (elapsed.toFloat() / ACCELERATION_DURATION).coerceAtMost(1f)

        // 使用线性进度，使加速更加直接明显
        val linearProgress = progress

        // 将抖动幅度设置为固定数值，确保不随时间变化
        val shakeAmplitude = 5f

        // 计算当前动画持续时间，从最大值逐渐减小到最小值
        // 调整计算公式，使变化更加明显
        val currentDuration =
            MAX_SHAKE_DURATION - (MAX_SHAKE_DURATION - MIN_SHAKE_DURATION) * linearProgress

        // 创建抖动动画，保持固定幅度，通过持续时间控制速度
        val translateAnimation = TranslateAnimation(
            -shakeAmplitude, shakeAmplitude,  // 固定X轴幅度
            -shakeAmplitude / 2, shakeAmplitude / 2  // 固定Y轴幅度（X轴的一半）
        )
        // 设置动画持续时间，持续时间越短速度越快
        translateAnimation.duration = currentDuration.toLong()
        translateAnimation.repeatCount = Animation.INFINITE
        translateAnimation.repeatMode = Animation.REVERSE

        animationSet.addAnimation(translateAnimation)
        animationSet.fillAfter = true

        easterEggImage.startAnimation(animationSet)
        shakeAnimation = animationSet

        // 如果仍在加速，继续更新动画（与流星同步更新）
        if(isAccelerating && progress < 1f) {
            handler.postDelayed({
                updateShakeAnimation()
            }, 16) // 与流星使用相同的更新频率（约60 FPS）
        }
    }


    /**
     * 创建流星背景
     */
    private fun createMeteors() {
        meteorContainer.removeAllViews()
        meteors.clear()
        meteorSpeeds.clear()
        meteorInitialSpeeds.clear()

        // 创建20个随机形状的流星
        repeat(20) {
            // 随机选择形状类型
            val meteorType = random.nextInt(4) // 0=圆形, 1=方形, 2=三角形, 3=星形

            val meteor = when(meteorType) {
                0 -> createCircleMeteor()
                1 -> createSquareMeteor()
                2 -> createTriangleMeteor()
                3 -> createStarMeteor()
                else -> createCircleMeteor()
            }

            // 设置随机位置和大小
            val size = random.nextInt(20) + 20
            val startX = random.nextInt(resources.displayMetrics.widthPixels)
            val startY = random.nextInt(resources.displayMetrics.heightPixels)

            val layoutParams = FrameLayout.LayoutParams(size, size)
            layoutParams.setMargins(startX, startY, 0, 0)
            meteor.layoutParams = layoutParams

            // 设置随机颜色
            val color =
                Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
            when(meteor) {
                is ImageView -> meteor.setColorFilter(color)

                else -> {
                    if(meteor.background != null) {
                        meteor.background.setTint(color)
                    } else {
                        meteor.setBackgroundColor(color)
                    }
                }
            }

            meteorContainer.addView(meteor)
            meteors.add(meteor)

            // 保存初始速度
            val initialSpeed = (random.nextInt(5) + 1).toFloat()
            meteorSpeeds.add(initialSpeed)
            meteorInitialSpeeds.add(initialSpeed)
        }
    }


    /**
     * 创建圆形流星
     */
    private fun createCircleMeteor(): View {
        val view = View(context)
        val shape = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.WHITE)
        }
        view.background = shape
        return view
    }


    /**
     * 创建方形流星
     */
    private fun createSquareMeteor(): View {
        val view = View(context)
        val shape = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.WHITE)
        }
        view.background = shape
        return view
    }


    /**
     * 创建三角形流星
     */
    private fun createTriangleMeteor(): ImageView {
        val imageView = ImageView(context)
        imageView.setImageResource(R.drawable.ic_triangle)
        return imageView
    }


    /**
     * 创建星形流星
     */
    private fun createStarMeteor(): ImageView {
        val imageView = ImageView(context)
        imageView.setImageResource(R.drawable.ic_star)
        return imageView
    }


    /**
     * 移动流星
     */
    private fun moveMeteors() {
        // 如果正在加速，计算当前速度
        if(isAccelerating) {
            val elapsed = System.currentTimeMillis() - accelerationStartTime
            val progress = (elapsed.toFloat() / ACCELERATION_DURATION).coerceAtMost(1f)
            val speedMultiplier = 1f + (MAX_SPEED_MULTIPLIER - 1f) * progress

            for(i in meteorSpeeds.indices) {
                meteorSpeeds[i] = meteorInitialSpeeds[i] * speedMultiplier
            }
        }

        for(i in meteors.indices) {
            val meteor = meteors[i]
            val speed = meteorSpeeds[i]

            val layoutParams = meteor.layoutParams as FrameLayout.LayoutParams

            layoutParams.leftMargin =
                (layoutParams.leftMargin + speed.toInt()) % resources.displayMetrics.widthPixels
            layoutParams.topMargin =
                (layoutParams.topMargin + speed.toInt()) % resources.displayMetrics.heightPixels
            meteor.layoutParams = layoutParams
        }
    }


    /**
     * 揭示彩蛋内容
     */
    private fun revealEasterEgg() {
        // 恢复状态栏显示
        showStatusBar()
        // 停止振动
        VibrationHelper.cancelVibration(viewModel)

        // 淡出图片和流星
        val fadeOut = AlphaAnimation(1.0f, 0.0f)
        fadeOut.duration = 2000
        fadeOut.fillAfter = true
//        fadeOut.setAnimationListener(object : Animation.AnimationListener {
//            override fun onAnimationStart(animation: Animation?) {}
//
//            override fun onAnimationRepeat(animation: Animation?) {}
//
//            override fun onAnimationEnd(animation: Animation?) {
//            }
//        })

        val frameLayoutImageContainer =
            view?.findViewById<FrameLayout>(R.id.frameLayoutImageContainer)
        frameLayoutImageContainer?.startAnimation(fadeOut)
//        easterEggImage.startAnimation(fadeOut)
        meteorContainer.startAnimation(fadeOut)

        // 淡入文字
        shadowConstraintLayout.visibility = View.VISIBLE

        val fadeIn = AlphaAnimation(0.0f, 1.0f)
        fadeIn.duration = 2000
        fadeIn.fillAfter = true
        shadowConstraintLayout.startAnimation(fadeIn)
    }


    /**
     * 隐藏状态栏
     */
    private fun hideStatusBar() {
        myActivity.hideExitButton()

        activity?.window?.apply {
            // 对于API 30及以上版本
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                insetsController?.apply {
                    hide(android.view.WindowInsets.Type.statusBars())
                    systemBarsBehavior =
                        android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                // 对于API 30以下版本
                @Suppress("DEPRECATION")
                decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        )
                @Suppress("DEPRECATION")
                requestFeature(android.view.Window.FEATURE_NO_TITLE)
            }
        }
    }


    /**
     * 显示状态栏
     */
    private fun showStatusBar() {
        myActivity.showExitButton()

        activity?.window?.apply {
            // 对于API 30及以上版本
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                insetsController?.show(android.view.WindowInsets.Type.statusBars())
            } else {
                // 对于API 30以下版本
                @Suppress("DEPRECATION")
                decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        )
            }
        }
    }
}