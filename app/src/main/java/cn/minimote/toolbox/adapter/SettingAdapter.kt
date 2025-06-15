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
import cn.minimote.toolbox.constant.Config.ConfigKeys
import cn.minimote.toolbox.constant.Config.ConfigValues.CheckUpdateFrequency
import cn.minimote.toolbox.constant.NetworkTypes
import cn.minimote.toolbox.constant.SeekBarValueList
import cn.minimote.toolbox.constant.ViewLists
import cn.minimote.toolbox.constant.ViewTypes
import cn.minimote.toolbox.helper.CheckUpdateHelper
import cn.minimote.toolbox.helper.ConfigHelper
import cn.minimote.toolbox.helper.NetworkHelper
import cn.minimote.toolbox.helper.SeekBarHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.ToolboxViewModel
import java.util.concurrent.atomic.AtomicInteger


//// 使得 setupSeekBar 内可以调用不同 ViewHolder 的 setupTextView 方法
//typealias SetupTextView = SettingAdapter.ViewHolder.() -> Unit


class SettingAdapter(
    private val context: Context,
    private val viewModel: ToolboxViewModel,
) : RecyclerView.Adapter<SettingAdapter.ViewHolder>() {

    private val viewList = ViewLists.settingViewList
    private val viewTypes = ViewTypes.Setting

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
            val checkUpdateFrequencyList = SeekBarValueList.checkUpdateFrequencyList

            private val lastUpdateCheckTime = viewModel.lastUpdateCheckTime
            private val updateCheckGap: Long
                get() = CheckUpdateHelper.getUpdateCheckGapLong(
                    frequency
                )
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
            val vibrationModeList = SeekBarValueList.vibrationModeList
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

        // 网络访问模式
        class NetworkAccessModeViewHolder(
            itemView: View,
            val viewModel: ToolboxViewModel,
        ) : ViewHolder(itemView) {
            private val textViewNetworkAccessMode: TextView =
                itemView.findViewById(R.id.textView_vibration_mode)
            val seekBar: SeekBar = itemView.findViewById(R.id.seekBar)
            var lastPosition: Int = -1
            val networkAccessModeList = SeekBarValueList.networkAccessModeList
            val networkAccessModeString: String
                get() = when(itemViewType) {
                    viewTypes.NETWORK_ACCESS_MODE_WIFI -> {
                        NetworkHelper.getNetworkAccessMode(
                            networkType = NetworkTypes.WIFI,
                            viewModel = viewModel,
                        )
                    }

                    viewTypes.NETWORK_ACCESS_MODE_MOBILE -> {
                        NetworkHelper.getNetworkAccessMode(
                            networkType = NetworkTypes.MOBILE,
                            viewModel = viewModel,
                        )
                    }

                    viewTypes.NETWORK_ACCESS_MODE_BLUETOOTH -> {
                        NetworkHelper.getNetworkAccessMode(
                            networkType = NetworkTypes.BLUETOOTH,
                            viewModel = viewModel,
                        )
                    }

                    viewTypes.NETWORK_ACCESS_MODE_OTHER -> {
                        NetworkHelper.getNetworkAccessMode(
                            networkType = NetworkTypes.OTHER,
                            viewModel = viewModel,
                        )
                    }

                    else -> {
                        throw IllegalArgumentException("非法的 itemViewType")
                    }
                }
            val viewTypes = ViewTypes.Setting

            // 设置振动模式的文字显示
            fun setupTextView() {
                val context = itemView.context

                val networkTypeString = when(itemViewType) {
                    viewTypes.NETWORK_ACCESS_MODE_WIFI -> {
                        NetworkHelper.getNetworkTypeString(context, NetworkTypes.WIFI)
                    }

                    viewTypes.NETWORK_ACCESS_MODE_MOBILE -> {
                        NetworkHelper.getNetworkTypeString(context, NetworkTypes.MOBILE)
                    }

                    viewTypes.NETWORK_ACCESS_MODE_BLUETOOTH -> {
                        NetworkHelper.getNetworkTypeString(context, NetworkTypes.BLUETOOTH)
                    }

                    viewTypes.NETWORK_ACCESS_MODE_OTHER -> {
                        NetworkHelper.getNetworkTypeString(context, NetworkTypes.OTHER)
                    }

                    else -> throw IllegalArgumentException("非法的 itemViewType")
                }
                val networkAccessMode = when(itemViewType) {
                    viewTypes.NETWORK_ACCESS_MODE_WIFI -> {
                        NetworkHelper.getNetworkAccessMode(NetworkTypes.WIFI, viewModel)
                    }

                    viewTypes.NETWORK_ACCESS_MODE_MOBILE -> {
                        NetworkHelper.getNetworkAccessMode(NetworkTypes.MOBILE, viewModel)
                    }

                    viewTypes.NETWORK_ACCESS_MODE_BLUETOOTH -> {
                        NetworkHelper.getNetworkAccessMode(NetworkTypes.BLUETOOTH, viewModel)
                    }

                    viewTypes.NETWORK_ACCESS_MODE_OTHER -> {
                        NetworkHelper.getNetworkAccessMode(NetworkTypes.OTHER, viewModel)
                    }

                    else -> throw IllegalArgumentException("非法的 itemViewType")
                }
                val networkAccessModeString = NetworkHelper.getNetworkAccessModeString(
                    context = context,
                    networkAccessMode = networkAccessMode,
                )
                textViewNetworkAccessMode.text = context.getString(
                    R.string.title_network_access_mode,
                    networkTypeString,
                    networkAccessModeString,
                )
            }
        }

        // 恢复默认
        class RestoreDefaultViewHolder(
            itemView: View,
            val viewModel: ToolboxViewModel,
        ) : ViewHolder(itemView) {

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

            viewTypes.TITLE_NETWORK -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_setting_title, parent, false)
                ViewHolder.TitleViewHolder(view)
            }

            viewTypes.NETWORK_ACCESS_MODE_WIFI -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_setting_vibration_mode, parent, false)
                ViewHolder.NetworkAccessModeViewHolder(view, viewModel)
            }

            viewTypes.NETWORK_ACCESS_MODE_MOBILE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_setting_vibration_mode, parent, false)
                ViewHolder.NetworkAccessModeViewHolder(view, viewModel)
            }

            viewTypes.NETWORK_ACCESS_MODE_BLUETOOTH -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_setting_vibration_mode, parent, false)
                ViewHolder.NetworkAccessModeViewHolder(view, viewModel)
            }

            viewTypes.NETWORK_ACCESS_MODE_OTHER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_setting_vibration_mode, parent, false)
                ViewHolder.NetworkAccessModeViewHolder(view, viewModel)
            }

            viewTypes.RESTORE_DEFAULT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_setting_restore_default, parent, false)
                ViewHolder.RestoreDefaultViewHolder(view, viewModel)
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

                        viewTypes.TITLE_NETWORK -> {
                            context.getString(R.string.setting_title_network)
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

            is ViewHolder.NetworkAccessModeViewHolder -> {
                SeekBarHelper.setupSeekBar(
                    seekBar = holder.seekBar,
                    valueList = holder.networkAccessModeList,
                    initPosition = holder.networkAccessModeList.indexOf(
                        holder.networkAccessModeString
                    ),
                    lastPosition = AtomicInteger(holder.lastPosition),
                    context = context,
                    viewModel = viewModel,
                    callback = object : SeekBarHelper.SeekBarSetupCallback {
                        override fun updateConfigValue(key: String, value: String) {
                            when(holder.itemViewType) {
                                holder.viewTypes.NETWORK_ACCESS_MODE_WIFI -> {
                                    ConfigHelper.updateConfigValue(
                                        key = ConfigKeys.NetworkAccessModeKeys.WIFI,
                                        value = value,
                                        viewModel = viewModel,
                                    )
                                }

                                holder.viewTypes.NETWORK_ACCESS_MODE_MOBILE -> {
                                    ConfigHelper.updateConfigValue(
                                        key = ConfigKeys.NetworkAccessModeKeys.MOBILE,
                                        value = value,
                                        viewModel = viewModel,
                                    )
                                }

                                holder.viewTypes.NETWORK_ACCESS_MODE_BLUETOOTH -> {
                                    ConfigHelper.updateConfigValue(
                                        key = ConfigKeys.NetworkAccessModeKeys.BLUETOOTH,
                                        value = value,
                                        viewModel = viewModel,
                                    )
                                }

                                holder.viewTypes.NETWORK_ACCESS_MODE_OTHER -> {
                                    ConfigHelper.updateConfigValue(
                                        key = ConfigKeys.NetworkAccessModeKeys.OTHER,
                                        value = value,
                                        viewModel = viewModel,
                                    )
                                }
                            }
                            ConfigHelper.updateSettingWasModified(viewModel)
                        }

                        override fun setupTextView() {
                            holder.setupTextView()
                        }
                    }
                )
            }

            is ViewHolder.RestoreDefaultViewHolder -> {
                holder.itemView.setOnClickListener {
                    VibrationHelper.vibrateOnClick(context, viewModel)
                    ConfigHelper.clearUserConfig(viewModel)
                    ConfigHelper.updateSettingWasModified(viewModel)
                }
            }
        }
    }

    override fun getItemCount(): Int = viewList.size

    override fun getItemViewType(position: Int): Int {
        return viewList[position]
    }
}
