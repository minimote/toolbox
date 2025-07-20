/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.MyViewModel


class MyRadioGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : RadioGroup(context, attrs) {

    private val idToRadioButtonMap = mutableMapOf<Int, RadioButton>()
    private var selectedId = -1
    lateinit var viewModel: MyViewModel
    lateinit var onCheckedChangeListener: (selectedId: Int) -> Unit

    init {
        // 设置为垂直方向
        orientation = VERTICAL
    }

    private fun addItem(
        id: Int,
        text: String,
    ) {
        val item = LayoutInflater.from(context).inflate(R.layout.item_radio, this, false)
        val radioButton = item.findViewById<RadioButton>(R.id.radioButton)
        val textView = item.findViewById<TextView>(R.id.textView_name)

        textView.text = text

//        val id = idToRadioButtonMap.size
        idToRadioButtonMap[id] = radioButton

        // 设置点击事件，触发 RadioButton 的状态切换
        item.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            selectedItem(id)
        }

//        radioButton.setOnCheckedChangeListener { _, isChecked ->
//            onCheckedChangeListener?.invoke(isChecked)
//        }


        addView(item)
    }


    private fun selectedItem(id: Int, invokeListener: Boolean = true) {
        idToRadioButtonMap[selectedId]?.isChecked = false
        idToRadioButtonMap[id]?.isChecked = true
        selectedId = id

        if (invokeListener) {
            onCheckedChangeListener.invoke(id)
        }
    }


    fun setRadioGroup(
        viewModel: MyViewModel,
        idToStringIdMap: LinkedHashMap<Int, Int>,
        initId: Int,
        onCheckedChangeListener: (selectedId: Int) -> Unit = {},
    ) {
        this.viewModel = viewModel
        this.onCheckedChangeListener = onCheckedChangeListener

        for ((id, stringId) in idToStringIdMap) {
            addItem(id, context.getString(stringId))
        }

        selectedItem(initId, false)
    }
}
