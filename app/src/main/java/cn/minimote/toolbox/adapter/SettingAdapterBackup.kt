/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

// import com.heytap.wearable.support.recycler.widget.RecyclerView
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
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.ToolboxViewModel
import cn.minimote.toolbox.viewModel.ToolboxViewModel.Companion.ConfigKeys
import cn.minimote.toolbox.viewModel.ToolboxViewModel.Companion.ConfigValues
import kotlin.math.abs


class SettingAdapterBackup(
    private val context: Context,
    private val viewModel: ToolboxViewModel,
) : RecyclerView.Adapter<SettingAdapterBackup.AppViewHolder>() {

    private val viewList = viewModel.settingViewList
    private val viewTypes = ToolboxViewModel.Companion.ViewTypes.Setting

    inner class AppViewHolder(itemView: View, val viewType: Int) :
        RecyclerView.ViewHolder(itemView) {

        lateinit var textViewTitle: TextView

        lateinit var textViewUpdateCheckFrequency: TextView
        lateinit var textViewNextCheckTime: TextView
        lateinit var seekBar: SeekBar
        var lastPosition: Int = -1


        init {
            when(viewType) {
                viewTypes.TITLE_CHECK_UPDATE -> {
                    textViewTitle = itemView.findViewById(R.id.title_name)
                }

                viewTypes.UPDATE_CHECK_FREQUENCY -> {
                    textViewUpdateCheckFrequency =
                        itemView.findViewById(R.id.textView_check_update_frequency)
                    textViewNextCheckTime = itemView.findViewById(R.id.textView_next_check_time)
                    seekBar = itemView.findViewById(R.id.seekBar)
                }
            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val layoutId = when(viewType) {
            viewTypes.TITLE_CHECK_UPDATE -> R.layout.item_setting_title
            viewTypes.UPDATE_CHECK_FREQUENCY -> R.layout.item_setting_update_check_frequency
            else -> -1
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return AppViewHolder(view, viewType)
    }


    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        when(holder.viewType) {
            viewTypes.TITLE_CHECK_UPDATE -> {
                holder.textViewTitle.text = context.getString(R.string.setting_title_update)
            }

            viewTypes.UPDATE_CHECK_FREQUENCY -> {

                val updateCheckFrequency = when(ConfigHelper.getConfigValue(
                    key = ConfigKeys.CHECK_UPDATE_FREQUENCY,
                    viewModel = viewModel,
                )) {
                    ConfigValues.CheckUpdateFrequency.DAILY -> {
                        context.getString(R.string.check_update_frequency_daily)
                    }

                    ConfigValues.CheckUpdateFrequency.WEEKLY -> {
                        context.getString(R.string.check_update_frequency_weekly)
                    }

                    ConfigValues.CheckUpdateFrequency.MONTHLY -> {
                        context.getString(R.string.check_update_frequency_monthly)
                    }

                    ConfigValues.CheckUpdateFrequency.NEVER -> {
                        context.getString(R.string.check_update_frequency_never)
                    }

                    else -> {
                        context.getString(R.string.error)
                    }
                }
                holder.textViewUpdateCheckFrequency.text = context.getString(
                    R.string.title_check_update_frequency,
                    updateCheckFrequency,
                )
                holder.textViewNextCheckTime.text = if(ConfigHelper.getConfigValue(
                        key = ConfigKeys.CHECK_UPDATE_FREQUENCY,
                        viewModel = viewModel,
                    ) == ConfigValues.CheckUpdateFrequency.NEVER
                ) {
                    context.getString(
                        R.string.next_check_time,
                        context.getString(R.string.none),
                    )
                } else {
                    context.getString(
                        R.string.next_check_time,
                        CheckUpdateHelper.getFormatTimeString(viewModel.nextUpdateCheckTime),
                    )
                }

                setupSeekBar(seekBar = holder.seekBar, positionNumber = 4, holder = holder)
                holder.lastPosition = 0
            }
        }

    }


    // 设置 SeekBar
    private fun setupSeekBar(
        holder: AppViewHolder,
        seekBar: SeekBar,
        positionNumber: Int, // 可停留的位置数量
    ) {
        val positions = IntArray(positionNumber)
        val step = 100 / (positionNumber - 1)
        for(i in 0 until positionNumber) {
            positions[i] = i * step
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    val closestPosition = positions.minByOrNull { abs(it - progress) }
                    seekBar?.progress = closestPosition ?: 0
                    if(seekBar?.progress != holder.lastPosition) {
                        VibrationHelper.vibrateOnClick(context, viewModel)
                        holder.lastPosition = seekBar?.progress ?: -1
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }


    override fun getItemCount(): Int = viewList.size


    override fun getItemViewType(position: Int): Int {
        return viewList[position]
    }

}
