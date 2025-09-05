/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.fragment

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import cn.minimote.toolbox.R
import java.util.Random

/**
 * 彩蛋 Fragment
 * 进入时播放彩蛋动画
 */
class EasterEggFragment : Fragment() {

    private lateinit var meteorContainer: FrameLayout
    private lateinit var easterEggImage: ImageView
    private lateinit var textViewContent: TextView
    private val meteors = mutableListOf<View>()
    private val meteorSpeeds = mutableListOf<Float>()
    private val random = Random()
    private val handler = Handler(Looper.getMainLooper())
    private val meteorRunnable = object : Runnable {
        override fun run() {
            moveMeteors()
            handler.postDelayed(this, 16) // 约60 FPS
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_easter_egg, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        meteorContainer = view.findViewById(R.id.meteorContainer)
        easterEggImage = view.findViewById(R.id.easterEggImage)
        textViewContent = view.findViewById(R.id.textView_content)

        textViewContent.visibility = View.INVISIBLE
        // 同时隐藏文字的父容器(SmartRefreshLayout中的ScrollView)
        val parent = textViewContent.parent.parent as View
        parent.visibility = View.INVISIBLE

        // 创建流星背景
        createMeteors()

        // 启动流星动画
        handler.post(meteorRunnable)

    }

    /**
     * 创建流星背景
     */
    private fun createMeteors() {
        meteorContainer.removeAllViews()
        meteors.clear()
        meteorSpeeds.clear()

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
            meteorSpeeds.add((random.nextInt(5) + 1).toFloat()) // 降低速度范围
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

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(meteorRunnable)
    }
}