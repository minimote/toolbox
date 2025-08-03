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


    fun showConfirmDialog(
        context: Context,
        viewModel: MyViewModel,
        titleText: String = "",
        titleTextColor: Int? = null,
        messageText: String = "",
        messageTextColor: Int? = null,
        positiveButtonText: String = context.getString(R.string.confirm),
        negativeButtonText: String = context.getString(R.string.cancel),
        positiveButtonTextColor: Int? = null,
        negativeButtonTextColor: Int? = null,
        positiveAction: (() -> Unit) = {},
        negativeAction: (() -> Unit) = {},
    ) {

        val view: View = LayoutInflater.from(context).inflate(
            R.layout.layout_dialog, null
        )
        val dialog = getCustomizeDialog(context = context, view = view)

        val paddingSize = context.resources.getDimension(
            R.dimen.layout_size_2_footnote
        ).toInt()

        val textViewTitle = view.findViewById<TextView>(R.id.textView_title)
        if(titleText.isNotEmpty()) {
            textViewTitle.text = titleText
            if(viewModel.isWatch) {
                textViewTitle.setPadding(
                    paddingSize,
                    paddingSize,
                    paddingSize,
                    paddingSize,
                )
            }
            if(titleTextColor != null) {
                textViewTitle.setTextColor(titleTextColor)
            }
        } else {
            textViewTitle.visibility = View.GONE
        }

        val textViewMessage = view.findViewById<TextView>(R.id.textView_message)
        if(messageText.isNotEmpty()) {
//            val indent = context.getString(R.string.indent)
//            if(!messageText.startsWith(indent)) {
//                textViewMessage.text = context.getString(
//                    R.string.indent_with_arg,
//                    messageText,
//                )
//            } else {
            textViewMessage.text = messageText
//            }
            if(viewModel.isWatch) {
                textViewMessage.setPadding(
                    paddingSize, paddingSize, paddingSize, paddingSize,
                )
            }
            if(messageTextColor != null) {
                textViewMessage.setTextColor(messageTextColor)
            }
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
        if(positiveButtonTextColor != null) {
            buttonPositive.setTextColor(positiveButtonTextColor)
        }

        val buttonNegative = view.findViewById<TextView>(R.id.button_negative)
        buttonNegative.text = negativeButtonText
        buttonNegative.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            negativeAction.invoke()
            dialog.dismiss()
        }
        if(negativeButtonTextColor != null) {
            buttonNegative.setTextColor(negativeButtonTextColor)
        }

        val buttonPaddingSize = context.resources.getDimension(
            R.dimen.layout_size_4_tiny
        ).toInt()
        if(viewModel.isWatch) {
            buttonNegative.setPadding(
                buttonPaddingSize,
                buttonNegative.paddingTop,
                buttonPaddingSize,
                buttonNegative.paddingBottom,
            )
            buttonPositive.setPadding(
                buttonPaddingSize,
                buttonPositive.paddingTop,
                buttonPaddingSize,
                buttonPositive.paddingBottom,
            )
        }

        dialog.show()
    }


    fun getCustomizeDialog(
        context: Context,
        view: View,
    ): AlertDialog {
        val dialog = AlertDialog.Builder(context, R.style.dialog).setView(view).create()
        return dialog
    }
}