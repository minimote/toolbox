/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.holder

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import cn.minimote.toolbox.R
import cn.minimote.toolbox.helper.DimensionHelper

class NoResultViewHolder(
    parent: ViewGroup,
    val actionOnClick: (() -> Unit)? = null,
) : ViewHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_setting_title, parent, false
    )
) {

    private val context: Context = parent.context

    var textView: TextView = itemView.findViewById(R.id.textView_name)
    val textStyle = Typeface.NORMAL
    val textColor = context.getColor(R.color.mid_gray)


    fun bind(text: String = context.getString(R.string.no_result)) {
        textView.text = text

        // 设置字体样式为正常
        textView.setTypeface(null, Typeface.NORMAL)
        // 设置字体大小
        DimensionHelper.setTextSize(
            textView = textView,
            textSizeDimensionId = R.dimen.text_size_3_large,
            context = context,
        )
        // 设置字体颜色为灰色
        textView.setTextColor(context.getColor(R.color.mid_gray))

        actionOnClick?.let {
            textView.setOnClickListener {
                actionOnClick.invoke()
            }
        }
    }
}
