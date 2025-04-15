/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import cn.minimote.toolbox.viewModel.ToolboxViewModel
import cn.minimote.toolbox.viewModel.ToolboxViewModel.Companion.ConfigKeys
import cn.minimote.toolbox.viewModel.ToolboxViewModel.Companion.ConfigValues.VibrationMode


object VibrationHelper {


    // 点击时震动
    fun vibrateOnClick(
        context: Context,
        viewModel: ToolboxViewModel,
        milliseconds: Long = 100L,
    ) {
        val vibrator = getVibrator(context)
        if(enableVibration(
                vibrator = vibrator, context = context,
                viewModel = viewModel
            )
        ) {
            clickVibration(vibrator, milliseconds)
        }
    }


    // 获取振动器
    private fun getVibrator(context: Context): Vibrator {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }


    // 判断是否有振动器以及是否允许振动
    private fun enableVibration(
        vibrator: Vibrator,
        context: Context,
        viewModel: ToolboxViewModel,
    ): Boolean {
        val vibrationMode = ConfigHelper.getConfigValue(
            key = ConfigKeys.VIBRATION_MODE,
            viewModel = viewModel,
        ) as String
        if(!vibrator.hasVibrator() || vibrationMode == VibrationMode.OFF) {
            return false
        }
        return vibrationMode == VibrationMode.ON || systemVibrationEnabled(context)
    }


    // 判断是否开启了系统振动
    private fun systemVibrationEnabled(
        context: Context
    ): Boolean {
        val contentResolver: ContentResolver = context.contentResolver
        val tagSet: MutableList<Int> = mutableListOf(
            // 跟随手机系统振动(默认振动)
            Settings.System.getInt(contentResolver, "haptic_feedback_enabled", -1),
            // 跟随手表系统振动(默认振动)
            Settings.System.getInt(contentResolver, "vibrate_mode", -2)
        )
//        Toast.makeText(context, "<$tagSet>", Toast.LENGTH_SHORT).show()
        return 0 !in tagSet
    }


    // 点击的振动样式
    private fun clickVibration(
        vibrator: Vibrator,
        milliseconds: Long,
    ) {
        val vibrationEffect = VibrationEffect.createOneShot(
            milliseconds,
            VibrationEffect.DEFAULT_AMPLITUDE
        )
//        val vibrationAttributes = VibrationAttributes.Builder()
//            .setUsage(VibrationAttributes.USAGE_TOUCH)
//            .build()
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            vibrator.vibrate(vibrationEffect, vibrationAttributes)
//        }
        vibrator.vibrate(vibrationEffect)
    }


    // 获取振动模式对应的字符串
    fun getVibrationModeString(context: Context, vibrationMode: String): String {
        return when(vibrationMode) {
            VibrationMode.ON -> {
                context.getString(cn.minimote.toolbox.R.string.vibration_mode_on)
            }

            VibrationMode.OFF -> {
                context.getString(cn.minimote.toolbox.R.string.vibration_mode_off)
            }

            VibrationMode.AUTO -> {
                context.getString(cn.minimote.toolbox.R.string.vibration_mode_auto)
            }

            else -> {
                throw IllegalArgumentException("非法的振动模式：$vibrationMode")
            }
        }
    }

}
