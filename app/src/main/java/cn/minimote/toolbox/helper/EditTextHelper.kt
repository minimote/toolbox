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
        imageButtonClear: ImageButton,
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
        )
        setClearButton(
            editText = editText,
            viewModel = viewModel,
            imageButtonClear = imageButtonClear,
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
    ) {
        if(stringText.isNotEmpty()) {
            editText.setText(stringText)
        }

        if(stringHint.isNotEmpty()) {
            editText.hint = stringHint
        }

        // 禁用振动反馈
        editText.isHapticFeedbackEnabled = false

        // 手动请求输入法，避免第一次点击出现闪烁
        editText.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
                VibrationHelper.vibrateOnClick(viewModel)
                val imm =
                    v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
                onFocusGained()
            } else {
                onFocusLost()
            }
        }

        // 点击输入框时触发振动
        editText.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
        }

        // 添加 TextWatcher 监听文本变化
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                afterTextChanged(s)
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


    // 设置清除按钮
    private fun setClearButton(
        editText: EditText,
        viewModel: MyViewModel,
        imageButtonClear: ImageButton,
    ) {
        // 设置清空按钮点击事件
        imageButtonClear.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            editText.setText("")
        }
    }
}