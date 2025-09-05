/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import cn.minimote.toolbox.viewModel.MyViewModel


object EditTextHelper {

    // 设置输入框和清除按钮
    fun setEditTextAndClearButton(
        editText: EditText,
        stringText: String = "",
        stringHint: String = "",
        viewModel: MyViewModel,
        beforeTextChanged: (s: CharSequence?, start: Int, count: Int, after: Int) -> Unit = { s: CharSequence?, start: Int, count: Int, after: Int ->
        },
        onTextChanged: (s: CharSequence?, start: Int, before: Int, count: Int) -> Unit = { s: CharSequence?, start: Int, before: Int, count ->
        },
        afterTextChanged: (s: Editable?) -> Unit = { s: Editable? -> },
        onFocusGained: () -> Unit = {}, // 当获得焦点时执行的逻辑
        onFocusLost: () -> Unit = {}, // 当失去焦点时执行的逻辑
        onClick: () -> Unit = {}, // 点击时执行的逻辑
        onEditorAction: () -> Unit = {}, // 回车键被按下时执行的逻辑
        imageButtonClear: ImageButton,
        onClickClearButton: () -> Unit = {},
    ) {
        setEditText(
            editText = editText,
            stringText = stringText,
            stringHint = stringHint,
            viewModel = viewModel,
            beforeTextChanged = beforeTextChanged,
            onTextChanged = onTextChanged,
            afterTextChanged = afterTextChanged,
            onFocusGained = onFocusGained,
            onFocusLost = onFocusLost,
            onClick = onClick,
            onEditorAction = onEditorAction,
            imageButtonClear = imageButtonClear,
        )
        setClearButton(
            editText = editText,
            viewModel = viewModel,
            imageButtonClear = imageButtonClear,
            onClickClearButton = onClickClearButton,
        )
    }


    // 设置输入框
    private fun setEditText(
        editText: EditText,
        stringText: String = "",
        stringHint: String = "",
        viewModel: MyViewModel,
        beforeTextChanged: (s: CharSequence?, start: Int, count: Int, after: Int) -> Unit = { s: CharSequence?, start: Int, count: Int, after: Int ->
        },
        onTextChanged: (s: CharSequence?, start: Int, before: Int, count: Int) -> Unit = { s: CharSequence?, start: Int, before: Int, count ->
        },
        afterTextChanged: (s: Editable?) -> Unit = { s: Editable? -> },
        onFocusGained: () -> Unit = {}, // 当获得焦点时执行的逻辑
        onFocusLost: () -> Unit = {}, // 当失去焦点时执行的逻辑
        onClick: () -> Unit = {}, // 点击时执行的逻辑
        onEditorAction: () -> Unit = {}, // 回车键被按下时执行的逻辑
        imageButtonClear: ImageButton,
    ) {
        if(stringText.isNotEmpty()) {
            editText.setText(stringText)
            if(canShowClearButton(viewModel)) {
                imageButtonClear.visibility = View.VISIBLE
            }
        }

        if(stringHint.isNotEmpty()) {
            editText.hint = stringHint
        }

        if(!canShowClearButton(viewModel)) {
            removeEditTextRightPadding(editText)
        }

        // 禁用振动反馈
        editText.isHapticFeedbackEnabled = false

        // 点击时执行的逻辑
        editText.setOnClickListener {
            onClick()
        }

        // 回车键显示为完成
        val action = android.view.inputmethod.EditorInfo.IME_ACTION_DONE
        editText.setImeOptions(action)

        // 处理键盘回车键事件
        editText.setOnEditorActionListener { _, actionId, event ->
            var handled = false
            // 回车键被按下
            if(actionId == action) {
                // 隐藏键盘
                val imm =
                    editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(editText.windowToken, 0)
                onEditorAction()
                handled = true
            }
            handled
        }


        // 手动请求输入法，避免第一次点击出现闪烁
        editText.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            val imm =
                v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            if(hasFocus) {
//                LogHelper.e("获得焦点", "获得焦点")
                imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
                onFocusGained()
            } else {
//                LogHelper.e("失去焦点", "失去焦点")
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                onFocusLost()
            }
        }


        // 添加 TextWatcher 监听文本变化
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                afterTextChanged(s)
                val newText = s.toString()
                // 检查文本框内容是否为空
                if(canShowClearButton(
                        viewModel = viewModel,
                        text = newText,
                    )
                ) {
                    imageButtonClear.visibility = View.VISIBLE
                } else {
                    imageButtonClear.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {
                beforeTextChanged(s, start, count, after)
            }

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
                onTextChanged(s, start, before, count)
            }
        })
    }


    // 移除输入框右边的多余间距
    fun removeEditTextRightPadding(editText: EditText) {
        editText.setPadding(
            editText.paddingStart,
            editText.paddingTop,
            editText.paddingStart,
            editText.paddingBottom
        )
    }


    // 可以显示搜索框清除按钮的标志
    private fun canShowClearButton(
        viewModel: MyViewModel,
        text: String = " ",
    ): Boolean {
        return !viewModel.isWatch && text.isNotEmpty()
    }


    // 设置清除按钮
    private fun setClearButton(
        editText: EditText,
        viewModel: MyViewModel,
        imageButtonClear: ImageButton,
        onClickClearButton: () -> Unit = {},
    ) {
        if(canShowClearButton(viewModel)) {
            // 设置清空按钮点击事件
            imageButtonClear.setOnClickListener {
                editText.setText("")
                onClickClearButton()
            }
        } else {
            imageButtonClear.visibility = View.GONE
        }
    }
}