/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.widget.SeekBar
import cn.minimote.toolbox.constant.Config.ConfigKeys
import cn.minimote.toolbox.viewModel.MyViewModel
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs


object SeekBarHelper {

    // 添加一个方法来获取当前值，这个方法需要在具体的适配器中实现
    interface SeekBarCallback {
        fun updateConfigValue(key: String, value: String)
        fun setupTextView()
    }

    fun setSeekBar(
        seekBar: SeekBar,
        valueList: List<String>,
        initPosition: Int,
        lastPosition: AtomicInteger,
        viewModel: MyViewModel,
        callback: SeekBarCallback,
    ) {
        callback.setupTextView()
        // 设置初始进度
        seekBar.progress = initPosition

        // 可停留的位置数量
        val positionNumber = valueList.size
        seekBar.max = positionNumber - 1
        val positions = IntArray(positionNumber)
        for(i in 0 until positionNumber) {
            positions[i] = i // 直接使用整数值 [0, 1, 2, 3, 4, 5]
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    val closestPosition = positions.minByOrNull { abs(it - progress) }
                    seekBar.progress = closestPosition ?: 0
                    if(seekBar.progress != lastPosition.get()) {
                        lastPosition.set(seekBar.progress)
                        val selectedValue = valueList[seekBar.progress]
                        callback.updateConfigValue(ConfigKeys.CHECK_UPDATE_FREQUENCY, selectedValue)
                        callback.setupTextView()
                        VibrationHelper.vibrateOnClick(viewModel)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

}
