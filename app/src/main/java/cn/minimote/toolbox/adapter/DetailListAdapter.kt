/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */


package cn.minimote.toolbox.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.ViewTypes
import cn.minimote.toolbox.dataClass.StoredTool
import cn.minimote.toolbox.helper.ClipboardHelper
import cn.minimote.toolbox.helper.SchemeHelper
import cn.minimote.toolbox.helper.TimeHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.MyViewModel


class DetailListAdapter(
    private val context: Context,
    val viewModel: MyViewModel,
) : RecyclerView.Adapter<DetailListAdapter.DetailViewHolder>() {

    private var tool = viewModel.originTool.value!!
    private val dataList = viewModel.getDetailList()

    inner class DetailViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView)


    override fun getItemCount(): Int = dataList.size


    override fun getItemViewType(position: Int): Int {
        // 根据位置返回不同的视图类型
        return dataList[position]
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val layoutId = if(viewType == ViewTypes.WidgetDetail.DEFAULT_NAME) {
            R.layout.item_edit_preview
        } else {
            R.layout.item_detail_item
        }
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        return DetailViewHolder(view)
    }


    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {

        when(holder.itemViewType) {

            ViewTypes.WidgetDetail.DEFAULT_NAME -> {
                val textViewName = holder.itemView.findViewById<TextView>(R.id.textView_name)
                textViewName.text = tool.name
                // 禁用振动反馈
                holder.itemView.isHapticFeedbackEnabled = false
                holder.itemView.setOnLongClickListener {
                    VibrationHelper.vibrateOnLongPress(viewModel)
                    ClipboardHelper.copyToClipboard(
                        context = context,
                        text = textViewName.text.toString(),
                        toastString = context.getString(R.string.tool_name),
                    )
                    true
                }
            }

            ViewTypes.WidgetDetail.SCHEME -> {
                setNameAndContent(
                    holder = holder,
                    name = context.getString(R.string.scheme),
                    content = SchemeHelper.getSchemeFromId(tool.id),
                )
            }

            ViewTypes.WidgetDetail.PACKAGE_NAME -> {
                setNameAndContent(
                    holder = holder,
                    name = context.getString(R.string.textView_package_name),
                    content = tool.packageName,
                )
            }

            ViewTypes.WidgetDetail.ACTIVITY_NAME -> {
                setNameAndContent(
                    holder = holder,
                    name = context.getString(R.string.textView_activity_name),
                    content = tool.activityName,
                )
            }

            ViewTypes.WidgetDetail.INTENT_ACTION -> {
                setNameAndContent(
                    holder = holder,
                    name = context.getString(R.string.intent_action),
                    content = tool.intentAction,
                )
            }

            ViewTypes.WidgetDetail.INTENT_CATEGORY -> {
                setNameAndContent(
                    holder = holder,
                    name = context.getString(R.string.intent_category),
                    content = tool.intentCategory,
                )
            }

            ViewTypes.WidgetDetail.INTENT_EXTRAS -> {
                setNameAndContent(
                    holder = holder,
                    name = context.getString(R.string.intent_extras),
                    content = tool.intentExtras,
                )
            }

            ViewTypes.WidgetDetail.INTENT_FLAG -> {
                setNameAndContent(
                    holder = holder,
                    name = context.getString(R.string.indent_flag),
                    content = StoredTool.getFlagNameList(tool.intentFlag),
                )
            }

            ViewTypes.WidgetDetail.INTENT_URI -> {
                setNameAndContent(
                    holder = holder,
                    name = context.getString(R.string.intent_uri),
                    content = tool.intentUri,
                )
            }

            ViewTypes.WidgetDetail.WARNING_MESSAGE -> {
                setNameAndContent(
                    holder = holder,
                    name = context.getString(R.string.warning_message),
                    nameColor = context.getColor(R.color.red),
                    content = tool.warningMessage,
                )
            }

            ViewTypes.WidgetDetail.DESCRIPTION -> {
                setNameAndContent(
                    holder = holder,
                    name = context.getString(R.string.description),
                    content = tool.description,
                )
            }

            ViewTypes.WidgetDetail.CREATED_TIME -> {
                setNameAndContent(
                    holder = holder,
                    name = context.getString(R.string.created_time),
                    content = TimeHelper.getFormatTimeString(
                        context = context,
                        timestamp = tool.createdTime,
                    ),
                )
            }

            ViewTypes.WidgetDetail.LAST_MODIFIED_TIME -> {
                setNameAndContent(
                    holder = holder,
                    name = context.getString(R.string.last_modified_time),
                    content = TimeHelper.getFormatTimeString(
                        context = context,
                        timestamp = tool.lastModifiedTime,
                    ),
                )
            }

            ViewTypes.WidgetDetail.LAST_USED_TIME -> {
                setNameAndContent(
                    holder = holder,
                    name = context.getString(R.string.last_used_time),
                    content = TimeHelper.getFormatTimeString(
                        context = context,
                        timestamp = tool.lastUsedTime,
                    ),
                )
            }

            ViewTypes.WidgetDetail.USE_COUNT -> {
                setNameAndContent(
                    holder = holder,
                    name = context.getString(R.string.use_count),
                    content = tool.useCount,
                )
            }
        }

    }


    // 设置名称和内容
    private fun setNameAndContent(
        holder: DetailViewHolder,
        name: String,
        nameColor: Int? = null,
        content: Any?,
        contentColor: Int? = null,
    ) {
        holder.itemView.isClickable = false

        val textViewName = holder.itemView.findViewById<TextView>(R.id.textView_name)
        textViewName.text = name
        if(nameColor != null) {
            textViewName.setTextColor(nameColor)
        }

        val textViewContent = holder.itemView.findViewById<TextView>(R.id.textView_content)
        textViewContent.text = content.toString()
        if(contentColor != null) {
            textViewContent.setTextColor(contentColor)
        }

        // 禁用振动反馈
        holder.itemView.isHapticFeedbackEnabled = false
        holder.itemView.setOnLongClickListener {
            VibrationHelper.vibrateOnLongPress(viewModel)
            ClipboardHelper.copyToClipboard(
                context = context,
                text = textViewContent.text.toString(),
                toastString = name,
            )
            true
        }
    }

}
