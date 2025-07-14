/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import cn.minimote.toolbox.R
import cn.minimote.toolbox.viewModel.MyViewModel

object DialogHelper {
    //TODO:自定义样式
    fun showConfirmDialog(
        context: Context,
        viewModel: MyViewModel,
        titleText: String = "",
        messageText: String = "",
        positiveButtonText: String = context.getString(R.string.confirm),
        negativeButtonText: String = context.getString(R.string.cancel),
        positiveAction: (() -> Unit) = {},
        negativeAction: (() -> Unit) = {},
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_layout, null)

        val dialog = AlertDialog.Builder(context).setView(view).create()

        val textViewTitle = view.findViewById<TextView>(R.id.textView_title)
        if(titleText.isNotEmpty()) {
            textViewTitle.text = titleText
        } else {
            textViewTitle.visibility = View.GONE
        }

        val textViewMessage = view.findViewById<TextView>(R.id.textView_message)
        if(messageText.isNotEmpty()) {
            textViewMessage.text = messageText
            // 两者都有内容时，取消消息顶部的 padding
            if(titleText.isNotEmpty()) {
                textViewMessage.setPadding(
                    textViewMessage.paddingLeft,
                    0,
                    textViewMessage.paddingRight,
                    textViewMessage.paddingBottom,
                )
            }
        } else {
            textViewMessage.visibility = View.GONE
        }

        val buttonPositive = view.findViewById<TextView>(R.id.button_positive)
        buttonPositive.text = positiveButtonText
        buttonPositive.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            positiveAction.invoke()
            dialog.dismiss()
        }

        val buttonNegative = view.findViewById<TextView>(R.id.button_negative)
        buttonNegative.text = negativeButtonText
        buttonNegative.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            negativeAction.invoke()
            dialog.dismiss()
        }

        dialog.show()
    }
}