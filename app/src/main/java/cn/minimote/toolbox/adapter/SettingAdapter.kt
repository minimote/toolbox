/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.Config
import cn.minimote.toolbox.constant.Config.ConfigKeys
import cn.minimote.toolbox.constant.Config.ConfigValues.CheckUpdateFrequency
import cn.minimote.toolbox.constant.NetworkType
import cn.minimote.toolbox.constant.ViewList
import cn.minimote.toolbox.constant.ViewTypes
import cn.minimote.toolbox.helper.CheckUpdateHelper
import cn.minimote.toolbox.helper.ConfigHelper.clearUserConfig
import cn.minimote.toolbox.helper.ConfigHelper.getConfigValue
import cn.minimote.toolbox.helper.ConfigHelper.updateConfigValue
import cn.minimote.toolbox.helper.ConfigHelper.updateSettingWasModified
import cn.minimote.toolbox.helper.NetworkHelper
import cn.minimote.toolbox.helper.TimeHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.ui.widget.RadioRecyclerView
import cn.minimote.toolbox.viewModel.MyViewModel
import com.google.android.material.switchmaterial.SwitchMaterial


//// 使得 setupSeekBar 内可以调用不同 ViewHolder 的 setupTextView 方法
//typealias SetupTextView = SettingAdapter.ViewHolder.() -> Unit


class SettingAdapter(
    private val context: Context,
    private val viewModel: MyViewModel,
) : RecyclerView.Adapter<SettingAdapter.ViewHolder>() {

    private val viewList = ViewList.settingViewList
    private val viewTypes = ViewTypes.Setting

    // 定义 sealed class ViewHolder
    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        class TitleViewHolder(itemView: View) : ViewHolder(itemView) {
            val textViewTitle: TextView = itemView.findViewById(R.id.title_name)
        }


        // 更新频率
        class CheckUpdateFrequencyViewHolder(
            itemView: View,
            val viewModel: MyViewModel,
        ) : ViewHolder(itemView) {
            //            private val textViewCheckUpdateFrequency: TextView =
//                itemView.findViewById(R.id.textView_check_update_frequency)
            private val textViewNextCheckTime: TextView =
                itemView.findViewById(R.id.textView_next_check_time)
            val radioRecyclerView: RadioRecyclerView =
                itemView.findViewById(R.id.radioRecyclerView)

            //            val seekBar: SeekBar = itemView.findViewById(R.id.seekBar)
//            var lastPosition: Int = -1
            val frequency: String
                get() = viewModel.getConfigValue(
                    key = ConfigKeys.CHECK_UPDATE_FREQUENCY,
                ).toString()
//            val checkUpdateFrequencyList = SeekBarValueList.checkUpdateFrequencyList

            private val lastUpdateCheckTime get() = viewModel.lastUpdateCheckTime
            private val updateCheckGap: Long
                get() = CheckUpdateHelper.getUpdateCheckGapLong(
                    frequency
                )
            private val nextUpdateCheckTime: Long get() = lastUpdateCheckTime + updateCheckGap

            // 设置检查更新频率的文字显示
            fun updateTextView() {
                val context = itemView.context
//                textViewCheckUpdateFrequency.text = context.getString(
//                    R.string.title_check_update_frequency,
//                    CheckUpdateHelper.getUpdateFrequencyString(
//                        context = context, frequency = frequency
//                    ),
//                )
//
                textViewNextCheckTime.text = if(frequency == CheckUpdateFrequency.NEVER) {
                    context.getString(
                        R.string.next_check_time, context.getString(R.string.none)
                    )
                } else {
                    context.getString(
                        R.string.next_check_time,
                        TimeHelper.getFormatTimeString(
                            context = context,
                            timestamp = nextUpdateCheckTime,
                        ),
                    )
                }
            }

        }


        // 振动模式
        class VibrationModeViewHolder(
            itemView: View,
            val viewModel: MyViewModel,
        ) : ViewHolder(itemView) {
            val textViewName: TextView = itemView.findViewById(R.id.textView_name)
            val radioRecyclerView: RadioRecyclerView =
                itemView.findViewById(R.id.radioRecyclerView)

//            private val textViewVibrationMode: TextView =
//                itemView.findViewById(R.id.textView_vibration_mode)
//            val seekBar: SeekBar = itemView.findViewById(R.id.seekBar)
//            var lastPosition: Int = -1
//            val vibrationModeList = SeekBarValueList.vibrationModeList
//            val vibrationMode: String
//                get() = viewModel.getConfigValue(
//                    key = ConfigKeys.VIBRATION_MODE,
//                ).toString()
//
//            // 设置振动模式的文字显示
//            fun setupTextView() {
//                val context = itemView.context
//                textViewVibrationMode.text = context.getString(
//                    R.string.title_vibration_mode,
//                    VibrationHelper.getVibrationModeString(
//                        context = context,
//                        vibrationMode = vibrationMode,
//                    ),
//                )
//            }
        }


        // 网络访问模式
        class NetworkAccessModeViewHolder(
            itemView: View,
            val viewModel: MyViewModel,
        ) : ViewHolder(itemView) {
            val textViewName: TextView = itemView.findViewById(R.id.textView_name)
            val radioRecyclerView: RadioRecyclerView =
                itemView.findViewById(R.id.radioRecyclerView)
            val viewTypes = ViewTypes.Setting
            //            private val textViewNetworkAccessMode: TextView =
//                itemView.findViewById(R.id.textView_vibration_mode)
//            val seekBar: SeekBar = itemView.findViewById(R.id.seekBar)
//            var lastPosition: Int = -1
//            val networkAccessModeList = SeekBarValueList.networkAccessModeList
//            val networkAccessModeString: String
//                get() = when(itemViewType) {
//                    viewTypes.NETWORK_ACCESS_MODE_WIFI -> {
//                        viewModel.getNetworkAccessMode(
//                            networkType = NetworkType.WIFI,
//                        )
//                    }
//
//                    viewTypes.NETWORK_ACCESS_MODE_MOBILE -> {
//                        viewModel.getNetworkAccessMode(
//                            networkType = NetworkType.MOBILE,
//                        )
//                    }
//
//                    viewTypes.NETWORK_ACCESS_MODE_BLUETOOTH -> {
//                        viewModel.getNetworkAccessMode(
//                            networkType = NetworkType.BLUETOOTH,
//                        )
//                    }
//
//                    viewTypes.NETWORK_ACCESS_MODE_OTHER -> {
//                        viewModel.getNetworkAccessMode(
//                            networkType = NetworkType.OTHER,
//                        )
//                    }
//
//                    else -> {
//                        throw IllegalArgumentException("非法的 itemViewType")
//                    }
//                }

            //            // 设置振动模式的文字显示
//            fun setupTextView() {
//            val context: Context = itemView.context
//            //
//            val networkTypeString = when(itemViewType) {
//                viewTypes.NETWORK_ACCESS_MODE_WIFI -> {
//                    NetworkHelper.getNetworkTypeString(context, NetworkType.WIFI)
//                }
//
//                viewTypes.NETWORK_ACCESS_MODE_MOBILE -> {
//                    NetworkHelper.getNetworkTypeString(context, NetworkType.MOBILE)
//                }
//
//                viewTypes.NETWORK_ACCESS_MODE_BLUETOOTH -> {
//                    NetworkHelper.getNetworkTypeString(context, NetworkType.BLUETOOTH)
//                }
//
//                viewTypes.NETWORK_ACCESS_MODE_OTHER -> {
//                    NetworkHelper.getNetworkTypeString(context, NetworkType.OTHER)
//                }
//
//                else -> throw IllegalArgumentException("非法的 itemViewType")
//            }
//                val networkAccessMode = when(itemViewType) {
//                    viewTypes.NETWORK_ACCESS_MODE_WIFI -> {
//                        viewModel.getNetworkAccessMode(NetworkType.WIFI)
//                    }
//
//                    viewTypes.NETWORK_ACCESS_MODE_MOBILE -> {
//                        viewModel.getNetworkAccessMode(NetworkType.MOBILE)
//                    }
//
//                    viewTypes.NETWORK_ACCESS_MODE_BLUETOOTH -> {
//                        viewModel.getNetworkAccessMode(NetworkType.BLUETOOTH)
//                    }
//
//                    viewTypes.NETWORK_ACCESS_MODE_OTHER -> {
//                        viewModel.getNetworkAccessMode(NetworkType.OTHER)
//                    }
//
//                    else -> throw IllegalArgumentException("非法的 itemViewType")
//                }
//                val networkAccessModeString = NetworkHelper.getNetworkAccessModeString(
//                    context = context,
//                    networkAccessMode = networkAccessMode,
//                )
//                textViewNetworkAccessMode.text = context.getString(
//                    R.string.title_network_access_mode,
//                    networkTypeString,
//                    networkAccessModeString,
//                )
//            }
        }


//        // 页面设置
//        class PageSettingViewHolder(
//            itemView: View,
//            val viewModel: MyViewModel,
//        ) : ViewHolder(itemView) {
//            private val textViewPageSetting: TextView =
//                itemView.findViewById(R.id.textView_vibration_mode)
//            val seekBar: SeekBar = itemView.findViewById(R.id.seekBar)
//            var lastPosition: Int = -1
//            val columnCountList = SeekBarValueList.columnCountList
//            val key
//                get() = when(itemViewType) {
//                    viewTypes.TOOL_LIST_COLUMN_COUNT -> {
//                        ConfigKeys.TOOL_LIST_COLUMN_COUNT
//                    }
//
//                    viewTypes.SCHEME_LIST_COLUMN_COUNT -> {
//                        ConfigKeys.SCHEME_LIST_COLUMN_COUNT
//                    }
//
//                    else -> throw IllegalArgumentException("PageSettingViewHolder 非法的 itemViewType: $itemViewType")
//                }
//            val columnCount: Int
//                get() = viewModel.getConfigValue(
//                    key = key,
//                ).toString().toInt()
//            val viewTypes = ViewTypes.Setting
//
//            val titleStringId
//                get() = when(itemViewType) {
//                    viewTypes.TOOL_LIST_COLUMN_COUNT -> {
//                        R.string.tool_list_column_count
//                    }
//
//                    viewTypes.SCHEME_LIST_COLUMN_COUNT -> {
//                        R.string.scheme_list_column_count
//                    }
//
//                    else -> throw IllegalArgumentException("非法的 itemViewType")
//                }
//
//
//            // 设置文字显示
//            fun setupTextView() {
//                val context = itemView.context
//                val columnCount = viewModel.getConfigValue(
//                    key = when(itemViewType) {
//                        viewTypes.TOOL_LIST_COLUMN_COUNT -> {
//                            ConfigKeys.TOOL_LIST_COLUMN_COUNT
//                        }
//
//                        viewTypes.SCHEME_LIST_COLUMN_COUNT -> {
//                            ConfigKeys.SCHEME_LIST_COLUMN_COUNT
//                        }
//
//                        else -> throw IllegalArgumentException("非法的 itemViewType")
//                    }
//                ).toString()
//                textViewPageSetting.text = context.getString(
//                    R.string.title_network_access_mode,
//                    context.getString(titleStringId),
//                    columnCount,
//                )
//            }
//        }


        // 恢复默认
        class RestoreDefaultViewHolder(
            itemView: View,
            val viewModel: MyViewModel,
        ) : ViewHolder(itemView)


        class SwitchViewHolder(
            itemView: View,
            val viewModel: MyViewModel,
        ) : ViewHolder(itemView) {
            val textViewName: TextView = itemView.findViewById(R.id.textView_name)
            val switch: SwitchMaterial = itemView.findViewById(R.id.switch_)
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
                    .inflate(R.layout.item_edit_radio_recyclerview, parent, false)
                ViewHolder.VibrationModeViewHolder(view, viewModel)
            }

            viewTypes.TITLE_NETWORK -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_setting_title, parent, false)
                ViewHolder.TitleViewHolder(view)
            }

            viewTypes.NETWORK_ACCESS_MODE_WIFI -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_edit_radio_recyclerview, parent, false)
                ViewHolder.NetworkAccessModeViewHolder(view, viewModel)
            }

            viewTypes.NETWORK_ACCESS_MODE_MOBILE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_edit_radio_recyclerview, parent, false)
                ViewHolder.NetworkAccessModeViewHolder(view, viewModel)
            }

            viewTypes.NETWORK_ACCESS_MODE_BLUETOOTH -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_edit_radio_recyclerview, parent, false)
                ViewHolder.NetworkAccessModeViewHolder(view, viewModel)
            }

            viewTypes.NETWORK_ACCESS_MODE_OTHER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_edit_radio_recyclerview, parent, false)
                ViewHolder.NetworkAccessModeViewHolder(view, viewModel)
            }

            viewTypes.TITLE_RESTORE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_setting_title, parent, false)
                ViewHolder.TitleViewHolder(view)
            }

            viewTypes.RESTORE_DEFAULT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_my_check_update, parent, false)
                ViewHolder.RestoreDefaultViewHolder(view, viewModel)
            }

//            viewTypes.TITLE_PAGE_SETTING -> {
//                val view = LayoutInflater.from(parent.context)
//                    .inflate(R.layout.item_setting_title, parent, false)
//                ViewHolder.TitleViewHolder(view)
//            }
//
//            viewTypes.TOOL_LIST_COLUMN_COUNT -> {
//                val view = LayoutInflater.from(parent.context)
//                    .inflate(R.layout.item_setting_vibration_mode, parent, false)
//                ViewHolder.PageSettingViewHolder(view, viewModel)
//            }
//
//            viewTypes.SCHEME_LIST_COLUMN_COUNT -> {
//                val view = LayoutInflater.from(parent.context)
//                    .inflate(R.layout.item_setting_vibration_mode, parent, false)
//                ViewHolder.PageSettingViewHolder(view, viewModel)
//            }

            viewTypes.TITLE_LAUNCH -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_setting_title, parent, false)
                ViewHolder.TitleViewHolder(view)
            }

            viewTypes.EXIT_AFTER_LAUNCH -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_setting_exit_after_launch, parent, false)
                ViewHolder.SwitchViewHolder(view, viewModel)
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
                val textId = when(viewType) {
                    viewTypes.TITLE_CHECK_UPDATE -> {
                        R.string.setting_title_update
                    }

                    viewTypes.TITLE_VIBRATION -> {
                        R.string.setting_title_vibration
                    }

                    viewTypes.TITLE_NETWORK -> {
                        R.string.setting_title_network
                    }

                    viewTypes.TITLE_RESTORE -> {
                        holder.textViewTitle.visibility = View.GONE
                        R.string.none
                    }

                    viewTypes.TITLE_LAUNCH -> {
                        R.string.setting_title_launch
                    }

                    else -> {
                        throw IllegalArgumentException("非法的视图类型")
                    }
                }
                holder.textViewTitle.text = context.getString(textId)
            }

            is ViewHolder.CheckUpdateFrequencyViewHolder -> {
                val key = ConfigKeys.CHECK_UPDATE_FREQUENCY
                holder.updateTextView()
                holder.radioRecyclerView.setRadioGroup(
                    viewModel = viewModel,
                    idToStringIdMap = CheckUpdateFrequency.idToStringIdMap,
                    initId = viewModel.getConfigValue(
                        key = key,
                    ).toString(),
                    onCheckedChangeListener = { selectedId ->
                        viewModel.updateConfigValue(
                            key = key,
                            value = selectedId,
                        )
                        viewModel.updateSettingWasModified()
                        holder.updateTextView()
                    },
                )
//                SeekBarHelper.setSeekBar(
//                    seekBar = holder.seekBar,
//                    valueList = holder.checkUpdateFrequencyList,
//                    initPosition = holder.checkUpdateFrequencyList.indexOf(holder.frequency),
//                    lastPosition = AtomicInteger(holder.lastPosition),
//                    viewModel = viewModel,
//                    callback = object : SeekBarHelper.SeekBarCallback {
//                        override fun updateConfigValue(key: String, value: String) {
//                            viewModel.updateConfigValue(
//                                key = ConfigKeys.CHECK_UPDATE_FREQUENCY,
//                                value = value,
//                            )
//                            viewModel.updateSettingWasModified()
//                        }
//
//                        override fun setupTextView() {
//                            holder.setupTextView()
//                        }
//
//                    },
//                )
            }

            is ViewHolder.VibrationModeViewHolder -> {
                holder.textViewName.text = context.getString(R.string.title_vibration_mode)
                val key = ConfigKeys.VIBRATION_MODE
//                holder.updateTextView()
                holder.radioRecyclerView.setRadioGroup(
                    viewModel = viewModel,
                    idToStringIdMap = Config.ConfigValues.VibrationMode.idToStringIdMap,
                    initId = viewModel.getConfigValue(
                        key = key,
                    ).toString(),
                    onCheckedChangeListener = { selectedId ->
                        viewModel.updateConfigValue(
                            key = key,
                            value = selectedId,
                        )
                        viewModel.updateSettingWasModified()
//                        holder.updateTextView()
                    },
                )
//                SeekBarHelper.setSeekBar(
//                    seekBar = holder.seekBar,
//                    valueList = holder.vibrationModeList,
//                    initPosition = holder.vibrationModeList.indexOf(holder.vibrationMode),
//                    lastPosition = AtomicInteger(holder.lastPosition),
//                    viewModel = viewModel,
//                    callback = object : SeekBarHelper.SeekBarCallback {
//                        override fun updateConfigValue(key: String, value: String) {
//                            viewModel.updateConfigValue(
//                                key = ConfigKeys.VIBRATION_MODE,
//                                value = value,
//                            )
//                            viewModel.updateSettingWasModified()
//                        }
//
//                        override fun setupTextView() {
//                            holder.setupTextView()
//                        }
//
//                    },
//                )
            }

            is ViewHolder.NetworkAccessModeViewHolder -> {
                val context: Context = holder.itemView.context
                //
                val networkTypeString = when(holder.itemViewType) {
                    viewTypes.NETWORK_ACCESS_MODE_WIFI -> {
                        NetworkHelper.getNetworkTypeString(context, NetworkType.WIFI)
                    }

                    viewTypes.NETWORK_ACCESS_MODE_MOBILE -> {
                        NetworkHelper.getNetworkTypeString(context, NetworkType.MOBILE)
                    }

                    viewTypes.NETWORK_ACCESS_MODE_BLUETOOTH -> {
                        NetworkHelper.getNetworkTypeString(context, NetworkType.BLUETOOTH)
                    }

                    viewTypes.NETWORK_ACCESS_MODE_OTHER -> {
                        NetworkHelper.getNetworkTypeString(context, NetworkType.OTHER)
                    }

                    else -> throw IllegalArgumentException("非法的 itemViewType")
                }
                holder.textViewName.text = networkTypeString
                val key = when(holder.itemViewType) {
                    holder.viewTypes.NETWORK_ACCESS_MODE_WIFI -> {
                        ConfigKeys.NetworkAccessModeKeys.WIFI
                    }

                    holder.viewTypes.NETWORK_ACCESS_MODE_MOBILE -> {
                        ConfigKeys.NetworkAccessModeKeys.MOBILE
                    }

                    holder.viewTypes.NETWORK_ACCESS_MODE_BLUETOOTH -> {
                        ConfigKeys.NetworkAccessModeKeys.BLUETOOTH
                    }

                    holder.viewTypes.NETWORK_ACCESS_MODE_OTHER -> {
                        ConfigKeys.NetworkAccessModeKeys.OTHER
                    }

                    else -> {
                        throw IllegalArgumentException("非法的视图类型")
                    }
                }
//                holder.updateTextView()
                holder.radioRecyclerView.setRadioGroup(
                    viewModel = viewModel,
                    idToStringIdMap = Config.ConfigValues.NetworkAccessModeValues.idToStringIdMap,
                    initId = viewModel.getConfigValue(
                        key = key,
                    ).toString(),
                    onCheckedChangeListener = { selectedId ->
                        viewModel.updateConfigValue(
                            key = key,
                            value = selectedId,
                        )
                        viewModel.updateSettingWasModified()
//                        holder.updateTextView()
                    },
                )
//                SeekBarHelper.setSeekBar(
//                    seekBar = holder.seekBar,
//                    valueList = holder.networkAccessModeList,
//                    initPosition = holder.networkAccessModeList.indexOf(
//                        holder.networkAccessModeString
//                    ),
//                    lastPosition = AtomicInteger(holder.lastPosition),
//                    viewModel = viewModel,
//                    callback = object : SeekBarHelper.SeekBarCallback {
//                        override fun updateConfigValue(key: String, value: String) {
//                            when(holder.itemViewType) {
//                                holder.viewTypes.NETWORK_ACCESS_MODE_WIFI -> {
//                                    viewModel.updateConfigValue(
//                                        key = ConfigKeys.NetworkAccessModeKeys.WIFI,
//                                        value = value,
//                                    )
//                                }
//
//                                holder.viewTypes.NETWORK_ACCESS_MODE_MOBILE -> {
//                                    viewModel.updateConfigValue(
//                                        key = ConfigKeys.NetworkAccessModeKeys.MOBILE,
//                                        value = value,
//                                    )
//                                }
//
//                                holder.viewTypes.NETWORK_ACCESS_MODE_BLUETOOTH -> {
//                                    viewModel.updateConfigValue(
//                                        key = ConfigKeys.NetworkAccessModeKeys.BLUETOOTH,
//                                        value = value,
//                                    )
//                                }
//
//                                holder.viewTypes.NETWORK_ACCESS_MODE_OTHER -> {
//                                    viewModel.updateConfigValue(
//                                        key = ConfigKeys.NetworkAccessModeKeys.OTHER,
//                                        value = value,
//                                    )
//                                }
//                            }
//                            viewModel.updateSettingWasModified()
//                        }
//
//                        override fun setupTextView() {
//                            holder.setupTextView()
//                        }
//                    }
//                )
            }

            is ViewHolder.RestoreDefaultViewHolder -> {
                val textView = holder.itemView.findViewById<TextView>(R.id.textView_text)
                textView.text = context.getString(R.string.restore_default)
                val clickableContainer =
                    holder.itemView.findViewById<ConstraintLayout>(R.id.clickable_container)

                clickableContainer.setOnClickListener {
                    VibrationHelper.vibrateOnClick(viewModel)
                    if(viewModel.userConfig.isNotEmpty()) {
                        viewModel.clearUserConfig()
                        viewModel.updateSettingWasModified()
                        submitList()
                        Toast.makeText(
                            context,
                            context.getString(R.string.restore_default_success),
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.already_default),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            }

//            is ViewHolder.PageSettingViewHolder -> {
//                SeekBarHelper.setSeekBar(
//                    seekBar = holder.seekBar,
//                    valueList = holder.columnCountList,
//                    initPosition = holder.columnCountList.indexOf(holder.columnCount.toString()),
//                    lastPosition = AtomicInteger(holder.lastPosition),
//                    viewModel = viewModel,
//                    callback = object : SeekBarHelper.SeekBarCallback {
//                        override fun updateConfigValue(key: String, value: String) {
//                            viewModel.updateConfigValue(
//                                key = holder.key,
//                                value = value.toInt(),
//                            )
//                            viewModel.updateSettingWasModified()
//                        }
//
//                        override fun setupTextView() {
//                            holder.setupTextView()
//                        }
//
//                    },
//                )
//            }


            is ViewHolder.SwitchViewHolder -> {
                holder.textViewName.text = context.getString(
                    when(holder.itemViewType) {
                        viewTypes.EXIT_AFTER_LAUNCH -> R.string.exit_after_launch
                        else -> throw IllegalArgumentException("非法的视图类型:${holder.itemViewType}")
                    }
                )
                holder.switch.isChecked = viewModel.getConfigValue(
                    key = ConfigKeys.EXIT_AFTER_LAUNCH,
                ) as Boolean
                holder.itemView.setOnClickListener {
                    VibrationHelper.vibrateOnClick(viewModel)
                    holder.switch.isChecked = !holder.switch.isChecked
                    viewModel.updateConfigValue(
                        key = ConfigKeys.EXIT_AFTER_LAUNCH,
                        value = holder.switch.isChecked,
                    )
                    viewModel.updateSettingWasModified()
                }
            }
        }
    }

    override fun getItemCount(): Int = viewList.size

    override fun getItemViewType(position: Int): Int {
        return viewList[position]
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList() {
        notifyDataSetChanged()
    }
}
