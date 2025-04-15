/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.helper.CheckUpdateHelper
import cn.minimote.toolbox.helper.ConfigHelper
import cn.minimote.toolbox.helper.SeekBarHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.ToolboxViewModel
import cn.minimote.toolbox.viewModel.ToolboxViewModel.Companion.ConfigKeys
import cn.minimote.toolbox.viewModel.ToolboxViewModel.Companion.ConfigValues.CheckUpdateFrequency
import java.util.concurrent.atomic.AtomicInteger


// 使得 setupSeekBar 内可以调用不同 ViewHolder 的 setupTextView 方法
typealias SetupTextView = SettingAdapter.ViewHolder.() -> Unit


class SettingAdapter(
    private val context: Context,
    private val viewModel: ToolboxViewModel,
) : RecyclerView.Adapter<SettingAdapter.ViewHolder>() {

    private val viewList = viewModel.settingViewList
    private val viewTypes = ToolboxViewModel.Companion.ViewTypes.Setting

    // 定义 sealed class ViewHolder
    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        class TitleViewHolder(itemView: View) : ViewHolder(itemView) {
            val textViewTitle: TextView = itemView.findViewById(R.id.title_name)
        }

        // 更新频率
        class CheckUpdateFrequencyViewHolder(
            itemView: View,
            val viewModel: ToolboxViewModel,
        ) : ViewHolder(itemView) {
            private val textViewCheckUpdateFrequency: TextView =
                itemView.findViewById(R.id.textView_check_update_frequency)
            private val textViewNextCheckTime: TextView =
                itemView.findViewById(R.id.textView_next_check_time)
            val seekBar: SeekBar = itemView.findViewById(R.id.seekBar)
            var lastPosition: Int = -1
            val frequency: String
                get() = ConfigHelper.getConfigValue(
                    key = ConfigKeys.CHECK_UPDATE_FREQUENCY,
                    viewModel = viewModel,
                ).toString()
            val checkUpdateFrequencyList = viewModel.checkUpdateFrequencyList

            private val lastUpdateCheckTime = viewModel.lastUpdateCheckTime
            private val updateCheckGap: Long get() = CheckUpdateHelper.getUpdateCheckGap(frequency)
            private val nextUpdateCheckTime: Long get() = lastUpdateCheckTime + updateCheckGap

            // 设置检查更新频率的文字显示
            fun setupTextView() {
                val context = itemView.context
                textViewCheckUpdateFrequency.text = context.getString(
                    R.string.title_check_update_frequency,
                    CheckUpdateHelper.getUpdateFrequencyString(
                        context = context, frequency = frequency
                    ),
                )

                textViewNextCheckTime.text = if(frequency == CheckUpdateFrequency.NEVER) {
                    context.getString(
                        R.string.next_check_time, context.getString(R.string.none)
                    )
                } else {
                    context.getString(
                        R.string.next_check_time,
                        CheckUpdateHelper.getFormatTimeString(nextUpdateCheckTime),
                    )
                }
            }

        }

        // 振动模式
        class VibrationModeViewHolder(
            itemView: View,
            val viewModel: ToolboxViewModel,
        ) : ViewHolder(itemView) {
            private val textViewVibrationMode: TextView =
                itemView.findViewById(R.id.textView_vibration_mode)
            val seekBar: SeekBar = itemView.findViewById(R.id.seekBar)
            var lastPosition: Int = -1
            val vibrationModeList = viewModel.vibrationModeList
            val vibrationMode: String
                get() = ConfigHelper.getConfigValue(
                    key = ConfigKeys.VIBRATION_MODE,
                    viewModel = viewModel,
                ).toString()

            // 设置振动模式的文字显示
            fun setupTextView() {
                val context = itemView.context
                textViewVibrationMode.text = context.getString(
                    R.string.title_vibration_mode,
                    VibrationHelper.getVibrationModeString(
                        context = context,
                        vibrationMode = vibrationMode,
                    ),
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            viewTypes.TITLE_CHECK_UPDATE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_setting_title, parent, false)
                ViewHolder.TitleViewHolder(view)
            }

            viewTypes.UPDATE_CHECK_FREQUENCY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_setting_update_check_frequency, parent, false)
                ViewHolder.CheckUpdateFrequencyViewHolder(view, viewModel)
            }

            viewTypes.TITLE_VIBRATION -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_setting_title, parent, false)
                ViewHolder.TitleViewHolder(view)
            }

            viewTypes.VIBRATION_MODE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_setting_vibration_mode, parent, false)
                ViewHolder.VibrationModeViewHolder(view, viewModel)
            }

            else -> {
                throw IllegalArgumentException("非法的视图类型")
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(holder) {
            is ViewHolder.TitleViewHolder -> {
                val viewType = getItemViewType(position)
                holder.textViewTitle.text =
                    when(viewType) {
                        viewTypes.TITLE_CHECK_UPDATE -> {
                            context.getString(R.string.setting_title_update)
                        }

                        viewTypes.TITLE_VIBRATION -> {
                            context.getString(R.string.setting_title_vibration)
                        }

                        else -> {
                            throw IllegalArgumentException("非法的视图类型")
                        }
                    }
            }

            is ViewHolder.CheckUpdateFrequencyViewHolder -> {
                SeekBarHelper.setupSeekBar(
                    seekBar = holder.seekBar,
                    valueList = holder.checkUpdateFrequencyList,
                    initPosition = holder.checkUpdateFrequencyList.indexOf(holder.frequency),
                    lastPosition = AtomicInteger(holder.lastPosition),
                    context = context,
                    viewModel = viewModel,
                    callback = object : SeekBarHelper.SeekBarSetupCallback {
                        override fun updateConfigValue(key: String, value: String) {
                            ConfigHelper.updateConfigValue(
                                key = ConfigKeys.CHECK_UPDATE_FREQUENCY,
                                value = value,
                                viewModel = viewModel,
                            )
                            ConfigHelper.updateSettingWasModified(viewModel)
                        }

                        override fun setupTextView() {
                            holder.setupTextView()
                        }

                    },
                )
            }

            is ViewHolder.VibrationModeViewHolder -> {
                SeekBarHelper.setupSeekBar(
                    seekBar = holder.seekBar,
                    valueList = holder.vibrationModeList,
                    initPosition = holder.vibrationModeList.indexOf(holder.vibrationMode),
                    lastPosition = AtomicInteger(holder.lastPosition),
                    context = context,
                    viewModel = viewModel,
                    callback = object : SeekBarHelper.SeekBarSetupCallback {
                        override fun updateConfigValue(key: String, value: String) {
                            ConfigHelper.updateConfigValue(
                                key = ConfigKeys.VIBRATION_MODE,
                                value = value,
                                viewModel = viewModel,
                            )
                            ConfigHelper.updateSettingWasModified(viewModel)
                        }

                        override fun setupTextView() {
                            holder.setupTextView()
                        }

                    },
                )
            }
        }
    }

    override fun getItemCount(): Int = viewList.size

    override fun getItemViewType(position: Int): Int {
        return viewList[position]
    }
}
