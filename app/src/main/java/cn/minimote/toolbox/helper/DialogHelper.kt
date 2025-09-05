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
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.UI.DIALOG_WIDTH_RATE
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
        messageTextColor: Int? = null,
        positiveButtonText: String = context.getString(R.string.confirm),
        negativeButtonText: String = context.getString(R.string.cancel),
        positiveButtonTextColor: Int? = null,
        negativeButtonTextColor: Int? = null,
        positiveAction: (() -> Unit) = {},
        negativeAction: (() -> Unit) = {},
    ) {

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
            messageTextColor = messageTextColor,
            positiveButtonText = positiveButtonText,
            negativeButtonText = negativeButtonText,
            positiveButtonTextColor = positiveButtonTextColor,
            negativeButtonTextColor = negativeButtonTextColor,
            positiveAction = positiveAction,
            negativeAction = negativeAction,
        )
        dialog.showMyDialog(viewModel = viewModel)
    }


    fun setDefaultDialog(
        dialog: AlertDialog,
        view: View,
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

//        val constraintLayoutRoot = view.findViewById<ConstraintLayout>(R.id.constraintLayout_root)
//        constraintLayoutRoot.maxHeight = (viewModel.screenHeight * 0.5).toInt()
//        constraintLayoutRoot.maxWidth = context.resources.getDimension(R.dimen.layout_size_dialog_maxWidth).toInt()


//        val container: ConstraintLayout = view.findViewById(R.id.scrollView_container)
//
//        // 设置最大高度，例如500像素
//        container.maxHeight = (viewModel.screenHeight * 0.5).toInt()


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

            if(messageText.isEmpty()) {
                textViewTitle.setPadding(
                    textViewTitle.paddingLeft,
                    2 * textViewTitle.paddingTop,
                    textViewTitle.paddingRight,
                    textViewTitle.paddingBottom,
                )
            }

            if(titleTextColor != null) {
                textViewTitle.setTextColor(titleTextColor)
            }
        } else {
            textViewTitle.visibility = View.GONE
        }

//        val scrollViewMessage = view.findViewById<ScrollView>(R.id.scrollView_message)
//        // 创建一个自定义ViewTreeObserver来设置ScrollView的最大高度
//        scrollViewMessage.viewTreeObserver.addOnGlobalLayoutListener(object :
//            android.view.ViewTreeObserver.OnGlobalLayoutListener {
//            override fun onGlobalLayout() {
//                // 移除监听器以避免重复调用
//                scrollViewMessage.viewTreeObserver.removeOnGlobalLayoutListener(this)
//
//                val layoutParams = scrollViewMessage.layoutParams
//                val maxHeight = (viewModel.screenHeight * 0.5).toInt()
//                if(scrollViewMessage.height > maxHeight) {
//                    layoutParams.height = maxHeight
//                    scrollViewMessage.layoutParams = layoutParams
//                }
//            }
//        })

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
            } else {
                textViewMessage.setPadding(
                    textViewMessage.paddingLeft,
                    2 * textViewMessage.paddingTop,
                    textViewMessage.paddingRight,
                    textViewMessage.paddingBottom,
                )
                textViewMessage.gravity = Gravity.CENTER
            }
        } else {
            textViewMessage.visibility = View.GONE
        }

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
    }


    fun getCustomizeDialog(
        context: Context,
        view: View,
    ): AlertDialog {
        val dialog = AlertDialog.Builder(
            context, R.style.dialog
        ).setView(view).create()

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
            val maxWidth = viewModel.myContext.resources.getDimension(
                R.dimen.layout_size_dialog_maxWidth
            ).toInt()
//            val maxWidth = window.decorView.width
            val displayWidth = (viewModel.screenWidth * DIALOG_WIDTH_RATE).toInt()
            val dialogWidth = min(maxWidth, displayWidth)
//
//            val displayHeight = (viewModel.screenHeight * DIALOG_HEIGHT_RATE).toInt()
//            val maxHeight = viewModel.myContext.resources.getDimension(
//                R.dimen.layout_size_dialog_maxHeight
//            ).toInt()
//            val dialogHeight = min(maxHeight, displayHeight)
//
//            // 使用post确保在视图布局完成后再获取高度
//            window.decorView.post {
//                val currentHeight = window.decorView.height
//                val dialogHeight = min(maxHeight, currentHeight)
//
//            window.setLayout(dialogWidth, dialogHeight)
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