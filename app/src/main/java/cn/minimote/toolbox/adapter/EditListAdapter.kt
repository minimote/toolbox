/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.objects.VibrationHelper
import cn.minimote.toolbox.view_model.ActivityViewModel
import com.google.android.material.switchmaterial.SwitchMaterial

class EditListAdapter(
    private val context: Context,
    val viewModel: ActivityViewModel
) : RecyclerView.Adapter<EditListAdapter.EditViewHolder>() {

    private val editList = viewModel.editList
    private var originWidget = viewModel.originWidget
    private var modifiedWidget = viewModel.modifiedWidget

    // 定义不同的视图类型
    companion object {
        const val VIEW_TYPE_NAME = 0
        const val VIEW_TYPE_SHOW_NAME = 1
        const val VIEW_TYPE_SIZE = 2
    }

    class EditViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val editTextNickName: EditText? = itemView.findViewById(R.id.editText_nickName)
        val buttonReset: Button? = itemView.findViewById(R.id.button_reset_nickName)

        val switchShowName: SwitchMaterial? =
            itemView.findViewById(R.id.switch_whether_show_widgetName)

        val textViewWidgetSizeNum: TextView? = itemView.findViewById(R.id.textView_widgetSize_num)
        val buttonWidgetSizeDecrease: View? = itemView.findViewById(R.id.button_widgetSize_decrease)
        val buttonWidgetSizeIncrease: View? = itemView.findViewById(R.id.button_widgetSize_increase)
    }

    override fun getItemViewType(position: Int): Int {
        // 根据位置返回不同的视图类型
        return editList[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditViewHolder {
        val layoutId = when(viewType) {
            VIEW_TYPE_NAME -> R.layout.item_edit_nickname
            VIEW_TYPE_SHOW_NAME -> R.layout.item_edit_whether_show_name
            VIEW_TYPE_SIZE -> R.layout.item_edit_widget_size
            else -> -1
        }
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        return EditViewHolder(view)
    }

    override fun onBindViewHolder(holder: EditViewHolder, position: Int) {
//        val editItem = editList[position]

        when(holder.itemViewType) {
            VIEW_TYPE_NAME -> {
                // 设置重置按钮事件
                holder.buttonReset?.setOnClickListener {
                    VibrationHelper.vibrateOnClick(context)

                    if(holder.editTextNickName?.text.toString() != originWidget.value?.appName) {
                        holder.editTextNickName?.setText(originWidget.value?.appName)
                        viewModel.modifiedWidget.value?.nickName =
                            originWidget.value?.appName.toString()
                        viewModel.isModified.value = viewModel.hasEditListChanged()
                    } else {
                        Toast.makeText(context, R.string.have_reset, Toast.LENGTH_SHORT).show()
                    }
                }

                // 修改组件名称
                holder.editTextNickName?.let { editText ->
                    editText.setText(originWidget.value?.nickName)

                    // 手动请求输入法，避免第一次点击出现闪烁
                    editText.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                        if(hasFocus) {
                            VibrationHelper.vibrateOnClick(context)
                            val imm =
                                v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
                        }
                    }

                    editText.setOnClickListener {
                        VibrationHelper.vibrateOnClick(context)
                    }

                    // 添加 TextWatcher 监听文本变化
                    editText.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                        }

                        override fun afterTextChanged(s: Editable?) {
                            val newText = s.toString()
                            viewModel.modifiedWidget.value?.nickName = newText
                            viewModel.isModified.value = viewModel.hasEditListChanged()
                        }
                    })
                }
            }

            VIEW_TYPE_SHOW_NAME -> {
                holder.switchShowName?.let { switch ->
                    switch.isChecked = originWidget.value?.showName == true

                    // 显示组件名称
                    holder.itemView.setOnClickListener {
                        VibrationHelper.vibrateOnClick(context)
                        switch.isChecked = !switch.isChecked
                        viewModel.modifiedWidget.value?.showName = switch.isChecked
                        viewModel.isModified.value = viewModel.hasEditListChanged()
                        Log.i("showName", "isModified: ${viewModel.isModified.value}")
                        Log.i(
                            "showName",
                            "origin: ${originWidget.value?.showName}, modified: ${modifiedWidget.value?.showName}"
                        )
                    }
                }
            }

            VIEW_TYPE_SIZE -> {
                // 显示组件大小
                holder.textViewWidgetSizeNum?.text =
                    context.getString(R.string.widget_size_num, modifiedWidget.value?.widgetSize)

                // 设置按钮事件
                holder.buttonWidgetSizeDecrease?.setOnClickListener {
                    if(modifiedWidget.value?.widgetSize == 1) {
                        return@setOnClickListener
                    }
                    VibrationHelper.vibrateOnClick(context)
                    modifiedWidget.value!!.widgetSize -= 1
                    viewModel.isModified.value = viewModel.hasEditListChanged()
//                    Log.i(
//                        "EditListAdapter",
//                        "增加${viewModel.isModified.value}：原始${originWidget.value?.widgetSize}，现在${modifiedWidget.value?.widgetSize}"
//                    )
                    holder.textViewWidgetSizeNum?.text = context.getString(
                        R.string.widget_size_num,
                        modifiedWidget.value?.widgetSize
                    )
                }
                holder.buttonWidgetSizeIncrease?.setOnClickListener {
                    if(modifiedWidget.value?.widgetSize == viewModel.maxWidgetSize) {
                        return@setOnClickListener
                    }
                    VibrationHelper.vibrateOnClick(context)
                    modifiedWidget.value!!.widgetSize += 1
                    viewModel.isModified.value = viewModel.hasEditListChanged()
//                    Log.i(
//                        "EditListAdapter",
//                        "增加${viewModel.isModified.value}：原始${originWidget.value?.widgetSize}，现在${modifiedWidget.value?.widgetSize}"
//                    )
                    holder.textViewWidgetSizeNum?.text = context.getString(
                        R.string.widget_size_num,
                        modifiedWidget.value?.widgetSize
                    )
                }
            }
        }

//        holder.itemView.setOnClickListener {
//            when(holder.itemViewType) {
//                VIEW_TYPE_SIZE -> {
//                    holder.textViewWidgetSizeNum?.text = context.getString(
//                        R.string.widget_size_num,
//                        modifiedWidget.value?.widgetSize
//                    )
//                }
//                else -> {}
//            }
//        }
    }


    override fun getItemCount(): Int = editList.size

}
