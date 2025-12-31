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
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.Config
import cn.minimote.toolbox.constant.SeekBarConstants
import cn.minimote.toolbox.constant.ViewList
import cn.minimote.toolbox.constant.ViewTypes
import cn.minimote.toolbox.constant.ViewTypes.Setting.radioViewSet
import cn.minimote.toolbox.constant.ViewTypes.Setting.seekBarSet
import cn.minimote.toolbox.constant.ViewTypes.Setting.switchViewSet
import cn.minimote.toolbox.constant.ViewTypes.Setting.titleViewSet
import cn.minimote.toolbox.helper.ConfigHelper.clearUserConfigOnly
import cn.minimote.toolbox.helper.ConfigHelper.getConfigValue
import cn.minimote.toolbox.helper.ConfigHelper.updateConfigValue
import cn.minimote.toolbox.helper.ConfigHelper.updateSettingWasModified
import cn.minimote.toolbox.helper.SeekBarHelper
import cn.minimote.toolbox.helper.TimeHelper.getFormatTimeString
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.ui.widget.RadioRecyclerView
import cn.minimote.toolbox.viewModel.MyViewModel
import com.google.android.material.switchmaterial.SwitchMaterial


class SettingAdapter(
    private val context: Context,
    private val viewModel: MyViewModel,
) : RecyclerView.Adapter<SettingAdapter.ViewHolder>() {

    private val viewList = ViewList.settingViewList
    private val viewTypes = ViewTypes.Setting


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(
            when(viewType) {
                in titleViewSet -> {
                    R.layout.item_setting_title
                }

                in switchViewSet -> {
                    R.layout.item_setting_switch
                }

                in radioViewSet -> {
                    R.layout.item_edit_radio_recyclerview
                }

                in seekBarSet -> {
                    if(viewModel.isWatch) {
                        R.layout.item_seekbar_watch
                    } else {
                        R.layout.item_seekbar
                    }
                }

                viewTypes.UPDATE_CHECK_FREQUENCY -> {
                    R.layout.item_setting_update_check_frequency
                }

                else -> {
                    R.layout.item_my_check_update
                    // throw IllegalArgumentException("非法的视图类型：$viewType")
                }
            },
            parent, false,
        )

        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(holder.itemViewType) {
            viewTypes.TITLE_CHECK_UPDATE -> {
                setTitle(holder = holder, titleId = R.string.setting_title_update)
            }

            viewTypes.TITLE_VIBRATION -> {
                setTitle(holder = holder, titleId = R.string.setting_title_vibration)
            }

            viewTypes.TITLE_NETWORK -> {
                setTitle(holder = holder, titleId = R.string.setting_title_network)
            }

            viewTypes.TITLE_RESTORE -> {
                setTitle(holder = holder)
            }

            viewTypes.TITLE_LAUNCH -> {
                setTitle(holder = holder, titleId = R.string.setting_title_launch)
            }

            viewTypes.TITLE_DISPLAY -> {
                setTitle(holder = holder, titleId = R.string.setting_title_display)
            }

            viewTypes.EXIT_AFTER_LAUNCH -> {
                setSwitch(
                    holder = holder,
                    titleId = R.string.exit_after_launch,
                    configKey = Config.ConfigKeys.Launch.EXIT_AFTER_LAUNCH,
                )
            }

            viewTypes.SHOW_LIKE_ICON -> {
                setSwitch(
                    holder = holder,
                    titleId = R.string.show_like_icon,
                    configKey = Config.ConfigKeys.Display.SHOW_LIKE_ICON,
                )
            }

            viewTypes.SHOW_UNAVAILABLE_TOOLS -> {
                setSwitch(
                    holder = holder,
                    titleId = R.string.show_unavailable_tools,
                    configKey = Config.ConfigKeys.Display.SHOW_UNAVAILABLE_TOOLS,
                )
            }

            viewTypes.VIBRATION_MODE -> {
                setRadioGroup(
                    holder = holder,
                    titleId = R.string.setting_title_vibration,
                    configKey = Config.ConfigKeys.VIBRATION_MODE,
                    idToStringIdMap = Config.ConfigValues.VibrationMode.idToStringIdMap,
                )
            }

            viewTypes.HOME_PAGE -> {
                setRadioGroup(
                    holder = holder,
                    titleId = R.string.setting_title_home_page,
                    configKey = Config.ConfigKeys.Launch.HOME_PAGE,
                    idToStringIdMap = Config.ConfigValues.HomePage.idToStringIdMap,
                )
            }

            viewTypes.UPDATE_CHECK_FREQUENCY -> {
                val textViewNextCheckTime =
                    holder.itemView.findViewById<TextView>(R.id.textView_next_check_time)

                fun updateView() {
                    textViewNextCheckTime.text = context.getString(
                        R.string.next_check_time,
                        getFormatTimeString(
                            context = context,
                            viewModel.nextUpdateCheckTime,
                        ),
                    )
                }

                setRadioGroup(
                    holder = holder,
                    titleId = R.string.title_check_update_frequency,
                    configKey = Config.ConfigKeys.CheckUpdate.CHECK_UPDATE_FREQUENCY,
                    idToStringIdMap = Config.ConfigValues.CheckUpdateFrequency.idToStringIdMap,
                    onCheckedChangeListener = { _ ->
                        updateView()
                    },
                )
            }

            viewTypes.NETWORK_ACCESS_MODE_MOBILE -> {
                setRadioGroup(
                    holder = holder,
                    titleId = R.string.network_type_mobile,
                    configKey = Config.ConfigKeys.NetworkAccessModeKeys.MOBILE,
                    idToStringIdMap = Config.ConfigValues.NetworkAccessModeValues.idToStringIdMap,
                )
            }

            viewTypes.NETWORK_ACCESS_MODE_BLUETOOTH -> {
                setRadioGroup(
                    holder = holder,
                    titleId = R.string.network_type_bluetooth,
                    configKey = Config.ConfigKeys.NetworkAccessModeKeys.BLUETOOTH,
                    idToStringIdMap = Config.ConfigValues.NetworkAccessModeValues.idToStringIdMap,
                )
            }

            viewTypes.NETWORK_ACCESS_MODE_WIFI -> {
                setRadioGroup(
                    holder = holder,
                    titleId = R.string.network_type_wifi,
                    configKey = Config.ConfigKeys.NetworkAccessModeKeys.WIFI,
                    idToStringIdMap = Config.ConfigValues.NetworkAccessModeValues.idToStringIdMap,
                )
            }

            viewTypes.NETWORK_ACCESS_MODE_OTHER -> {
                setRadioGroup(
                    holder = holder,
                    titleId = R.string.network_type_other,
                    configKey = Config.ConfigKeys.NetworkAccessModeKeys.OTHER,
                    idToStringIdMap = Config.ConfigValues.NetworkAccessModeValues.idToStringIdMap,
                )
            }

            viewTypes.RESTORE_DEFAULT -> {
                setTitle(
                    holder = holder,
                    titleId = R.string.restore_default,
                )
                holder.itemView.setOnClickListener {
                    VibrationHelper.vibrateOnClick(viewModel)
                    if(viewModel.userConfig.isNotEmpty()) {
                        viewModel.clearUserConfigOnly()
                        viewModel.updateSettingWasModified()
                        submitList()
                        Toast.makeText(
                            context,
                            context.getString(R.string.restore_default_setting_success),
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

            viewTypes.SEARCH_HISTORY_MAX_COUNT -> {
                setSeekBar(
                    holder = holder,
                    valueList = SeekBarConstants.searchHistoryMaxCount,
                    titleId = R.string.search_history_max_count,
                    configKey = Config.ConfigKeys.SearchHistory.MAX_COUNT,
                )
            }

            viewTypes.SEARCH_SUGGESTION_MAX_COUNT -> {
                setSeekBar(
                    holder = holder,
                    valueList = SeekBarConstants.searchSuggestionMaxCount,
                    titleId = R.string.search_suggestion_max_count,
                    configKey = Config.ConfigKeys.SearchSuggestion.MAX_COUNT,
                )
            }

            viewTypes.CHECK_UPDATE_IGNORE_NETWORK_RESTRICTIONS -> {
                setSwitch(
                    holder = holder,
                    titleId = R.string.check_update_ignore_network_restrictions,
                    configKey = Config.ConfigKeys.CheckUpdate.CHECK_UPDATE_IGNORE_NETWORK_RESTRICTIONS,
                )
            }
        }
    }


    // 设置标题
    private fun setTitle(
        holder: ViewHolder,
        titleId: Int = R.string.none,
    ) {
        val textViewTitle: TextView = holder.itemView.findViewById(R.id.textView_name)
        textViewTitle.text = context.getString(titleId)
        if(titleId == R.string.none) {
            textViewTitle.visibility = View.GONE
        }
    }


    // 设置开关
    private fun setSwitch(
        holder: ViewHolder,
        titleId: Int,
        configKey: String,
    ) {
        val textViewTitle: TextView = holder.itemView.findViewById(R.id.textView_name)
        textViewTitle.text = context.getString(titleId)

        val switch: SwitchMaterial = holder.itemView.findViewById(R.id.switch_)
        switch.isChecked = viewModel.getConfigValue(
            key = configKey,
        ) as Boolean

        holder.itemView.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            switch.isChecked = !switch.isChecked
            viewModel.updateConfigValue(
                key = configKey,
                value = switch.isChecked,
            )
            viewModel.updateSettingWasModified()
        }
    }


    // 设置单选
    private fun setRadioGroup(
        holder: ViewHolder,
        titleId: Int,
        configKey: String,
        idToStringIdMap: LinkedHashMap<String, Int>,
        onCheckedChangeListener: (String) -> Unit = {},
    ) {
        val textViewTitle: TextView = holder.itemView.findViewById(R.id.textView_name)
        textViewTitle.text = context.getString(titleId)

        val initId = viewModel.getConfigValue(
            key = configKey,
        ).toString()
        onCheckedChangeListener(initId)

        val radioRecyclerView: RadioRecyclerView =
            holder.itemView.findViewById(R.id.radioRecyclerView)
        radioRecyclerView.setRadioGroup(
            viewModel = viewModel,
            idToStringIdMap = idToStringIdMap,
            initId = initId,
            onCheckedChangeListener = { selectedId ->
                viewModel.updateConfigValue(
                    key = configKey,
                    value = selectedId,
                )
                viewModel.updateSettingWasModified()
                onCheckedChangeListener(selectedId)
            },
        )
    }


    // 设置滑动条
    private fun setSeekBar(
        holder: ViewHolder,
        valueList: List<String>,
        titleId: Int,
        configKey: String,
    ) {
        val textViewTitle: TextView = holder.itemView.findViewById(R.id.textView_name)
        textViewTitle.text = context.getString(titleId)

        val textViewContent: TextView = holder.itemView.findViewById(R.id.textView_content)

        val seekBar: SeekBar = holder.itemView.findViewById(R.id.seekBar)
        SeekBarHelper.setSeekBar(
            seekBar = seekBar,
            valueList = valueList,
            initPosition = valueList.indexOf(viewModel.getConfigValue(configKey).toString()),
            viewModel = viewModel,
            textViewDecrease = holder.itemView.findViewById(R.id.textView_decrease),
            textViewIncrease = holder.itemView.findViewById(R.id.textView_increase),
            callback = object : SeekBarHelper.SeekBarCallback {
                override fun updateConfigValue(value: String) {
                    viewModel.updateConfigValue(
                        key = configKey,
                        value = value.toInt(),
                    )
                    viewModel.updateSettingWasModified()
                }

                override fun setupTextView(value: String) {
                    textViewContent.text = value
                }
            },
        )
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
