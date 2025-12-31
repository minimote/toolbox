/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import cn.minimote.toolbox.R
import cn.minimote.toolbox.adapter.DialogAdapter
import cn.minimote.toolbox.constant.UI.DIALOG_WIDTH_RATE
import cn.minimote.toolbox.helper.DimensionHelper.getLayoutSize
import cn.minimote.toolbox.helper.DimensionHelper.setTextSize
import cn.minimote.toolbox.ui.widget.ShadowConstraintLayout
import cn.minimote.toolbox.viewModel.MyViewModel
import kotlin.math.min


object DialogHelper {


    @SuppressLint("InflateParams")
    fun setAndShowDefaultDialog(
        context: Context,
        viewModel: MyViewModel,
        titleText: String = "",
        titleTextColor: Int? = null,
        messageText: String = "",
        messageTextList: List<String> = listOf(),
        messageTextColor: Int? = null,
        positiveButtonText: String = context.getString(R.string.confirm),
        negativeButtonText: String = context.getString(R.string.cancel),
        positiveButtonTextColor: Int? = null,
        negativeButtonTextColor: Int? = null,
        positiveAction: (() -> Unit) = {},
        negativeAction: (() -> Unit) = {},
    ): AlertDialog {

        val view: View = LayoutInflater.from(context).inflate(
            R.layout.layout_dialog,
            (context as? Activity)?.findViewById(android.R.id.content),
            false
        )
        val dialog = getCustomizeDialog(context = context, view = view)

        setDefaultDialog(
            dialog = dialog,
            view = view,
            context = context,
            viewModel = viewModel,
            titleText = titleText,
            titleTextColor = titleTextColor,
            messageText = messageText,
            messageTextList = messageTextList,
            messageTextColor = messageTextColor,
            positiveButtonText = positiveButtonText,
            negativeButtonText = negativeButtonText,
            positiveButtonTextColor = positiveButtonTextColor,
            negativeButtonTextColor = negativeButtonTextColor,
            positiveAction = positiveAction,
            negativeAction = negativeAction,
        )
        dialog.showMyDialog(viewModel = viewModel)

        return dialog
    }


    fun setDefaultDialog(
        dialog: AlertDialog,
        view: View,
        context: Context,
        viewModel: MyViewModel,
        titleText: String = "",
        titleTextColor: Int? = null,
        messageText: String = "",
        messageTextList: List<String> = listOf(),
        messageTextColor: Int? = null,
        positiveButtonText: String = context.getString(R.string.confirm),
        negativeButtonText: String = context.getString(R.string.cancel),
        positiveButtonTextColor: Int? = null,
        negativeButtonTextColor: Int? = null,
        positiveAction: (() -> Unit) = {},
        negativeAction: (() -> Unit) = {},
    ) {

        val buttonPositive = view.findViewById<TextView>(R.id.button_positive)
        buttonPositive.text = positiveButtonText
        buttonPositive.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            positiveAction()
            dialog.dismiss()
        }
        if(positiveButtonTextColor != null) {
            buttonPositive.setTextColor(positiveButtonTextColor)
        }

        val buttonNegative = view.findViewById<TextView>(R.id.button_negative)
        buttonNegative.text = negativeButtonText
        buttonNegative.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            negativeAction()
            dialog.dismiss()
        }
        if(negativeButtonTextColor != null) {
            buttonNegative.setTextColor(negativeButtonTextColor)
        }

        val shadowConstraintLayout =
            view.findViewById<ShadowConstraintLayout>(R.id.include_shadow_constraintLayout_recyclerView)


        val textViewTitle = view.findViewById<TextView>(R.id.textView_title)



        if(titleText.isNotEmpty()) {

            textViewTitle.text = titleText
            textViewTitle.setTypeface(null, android.graphics.Typeface.BOLD)
            if(titleTextColor != null) {
                textViewTitle.setTextColor(titleTextColor)
            }

        }


        if(messageText.isNotEmpty()) {
            if(titleText.isEmpty()) {
                // 只有单条消息内容，没有标题
                textViewTitle.text = messageText
                textViewTitle.setTypeface(null, android.graphics.Typeface.NORMAL)
                setTextSize(
                    textView = textViewTitle,
                    textSizeDimensionId = R.dimen.text_size_0_normal,
                    context = context,
                )

                // 显示分割线
                val viewFooterSeparator = view.findViewById<View>(R.id.view_footer_separator)
                viewFooterSeparator.visibility = View.VISIBLE

                if(messageTextColor != null) {
                    textViewTitle.setTextColor(messageTextColor)
                }
                shadowConstraintLayout.visibility = View.GONE
                return
            }
        } else if(titleText.isEmpty()) {
            // 没有标题，没有单条消息(有多条消息)
            textViewTitle.visibility = View.GONE
        }

        val recyclerView = shadowConstraintLayout.recyclerView

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = DialogAdapter(
            viewModel = viewModel,
            context = context,
            dataList = if(messageText.isEmpty()) {
                messageTextList
            } else {
                listOf(messageText)
            },
            gravity = if(messageText.isEmpty()) {
                Gravity.START
            } else {
                Gravity.CENTER
            },
            textColor = messageTextColor,
        )
        shadowConstraintLayout.setShadow(
            viewModel = viewModel,
            addBottomPadding = false,
            shadowTopBackgroundResId = R.drawable.background_mask_top_verydeepgray,
            shadowBottomBackgroundResId = R.drawable.background_mask_bottom_verydeepgray,
        )

    }


    fun getCustomizeDialog(
        context: Context,
        view: View,
    ): AlertDialog {
        val dialog = AlertDialog.Builder(
            context, R.style.dialog
        ).setView(view).create()

        if(DeviceHelper.isWatch(context)) {
            val buttonPositive = view.findViewById<TextView>(R.id.button_positive)
            setTextSize(
                textView = buttonPositive,
                textSizeDimensionId = R.dimen.text_size_1_small,
                context = context,
            )

            val buttonNegative = view.findViewById<TextView>(R.id.button_negative)
            setTextSize(
                textView = buttonNegative,
                textSizeDimensionId = R.dimen.text_size_1_small,
                context = context,
            )
        }


        // 允许点击外部区域关闭对话框
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }


    fun AlertDialog.showMyDialog(
        viewModel: MyViewModel,
    ) {
        // 先设置窗口属性为不可见，避免调整大小时的闪烁/

        this.show()
//        this.window.decorView.width

//        // 在显示后立即设置窗口属性，避免闪烁
        this.window?.let { window ->
            val maxWidth = getLayoutSize(
                viewModel.myContext,
                R.dimen.layout_size_dialog_maxWidth
            )

            val displayWidth = (viewModel.screenWidth * DIALOG_WIDTH_RATE).toInt()
            val dialogWidth = min(maxWidth, displayWidth)

            window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER)

            // 强制刷新窗口布局
            window.decorView.requestLayout()
//
//                // 恢复窗口可见性和焦点
//                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
//                window.attributes.alpha = 1f
//                window.attributes = window.attributes
//            }
        }
    }
}