/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.objects.VibrationHelper
import cn.minimote.toolbox.view_model.ToolboxViewModel
import cn.minimote.toolbox.view_model.ToolboxViewModel.Constants.EditViewTypes
import com.google.android.material.switchmaterial.SwitchMaterial


class EditListAdapter(
    private val context: Context,
    val viewModel: ToolboxViewModel,
    private val lifecycleOwner: androidx.lifecycle.LifecycleOwner,
) : RecyclerView.Adapter<EditListAdapter.EditViewHolder>() {

    private val editList = viewModel.editList
    private var originWidget = viewModel.originWidget
    private var modifiedWidget = viewModel.modifiedWidget

    private var widgetNameWasModified = viewModel.widgetNameWasModified
    private var widgetSizeWasModified = viewModel.widgetSizeWasModified
    // 观察者
    private lateinit var widgetNameWasModifiedObserver: Observer<Boolean>
    private lateinit var widgetSizeWasModifiedObserver: Observer<Boolean>

    // 创建一个 ViewModel 的 EditViewTypes 本地引用
    private val editViewTypes = viewModel.editViewTypes
    // 定义不同的视图类型
//    companion object {
//        const val VIEW_TYPE_NAME = 0
//        const val VIEW_TYPE_SHOW_NAME = 1
//        const val VIEW_TYPE_SIZE = 2
//    }


    class EditViewHolder(
        itemView: View,
        viewType: Int,
        editViewTypes: EditViewTypes,
    ) : RecyclerView.ViewHolder(itemView) {
        lateinit var textViewActivityName: TextView

        lateinit var editTextNickname: EditText
        lateinit var buttonResetNickname: Button

        lateinit var switchShowName: SwitchMaterial

        lateinit var buttonWidgetSizeDecrease: Button
        lateinit var buttonWidgetSizeIncrease: Button
        lateinit var textViewWidgetSizeFraction: TextView
        lateinit var buttonResetWidgetSize: Button

        lateinit var buttonDeleteWidget: Button

        init {
            when(viewType) {
                // 活动名称
                editViewTypes.EDIT_VIEW_TYPE_ACTIVITY_NAME -> {
                    textViewActivityName = itemView.findViewById(R.id.textView_activityName)
                }

                // 显示名称修改
                editViewTypes.EDIT_VIEW_TYPE_NICKNAME -> {
                    editTextNickname = itemView.findViewById(R.id.textView_activityName)
                    buttonResetNickname = itemView.findViewById(R.id.button_reset_nickName)
                }

                // 是否显示名称
                editViewTypes.EDIT_VIEW_TYPE_SHOW_NAME -> {
                    switchShowName = itemView.findViewById(R.id.switch_whether_show_widgetName)
                }

                // 组件大小修改
                editViewTypes.EDIT_VIEW_TYPE_SIZE -> {
                    buttonResetWidgetSize = itemView.findViewById(R.id.button_reset_widgetSize)
                    textViewWidgetSizeFraction =
                        itemView.findViewById(R.id.textView_widgetSize_fraction)
                    buttonWidgetSizeDecrease =
                        itemView.findViewById(R.id.button_widgetSize_decrease)
                    buttonWidgetSizeIncrease =
                        itemView.findViewById(R.id.button_widgetSize_increase)
                }

                // 删除组件
                editViewTypes.EDIT_VIEW_TYPE_DELETE -> {
                    buttonDeleteWidget = itemView.findViewById(R.id.button_deleteWidget)
                }
            }
        }
    }


    override fun getItemCount(): Int = editList.size


    override fun getItemViewType(position: Int): Int {
        // 根据位置返回不同的视图类型
        return editList[position]
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditViewHolder {
        val layoutId = when(viewType) {
            editViewTypes.EDIT_VIEW_TYPE_ACTIVITY_NAME -> R.layout.item_edit_activity_name
            editViewTypes.EDIT_VIEW_TYPE_NICKNAME -> R.layout.item_edit_nickname
            editViewTypes.EDIT_VIEW_TYPE_SHOW_NAME -> R.layout.item_edit_whether_show_name
            editViewTypes.EDIT_VIEW_TYPE_SIZE -> R.layout.item_edit_widget_size_new
            editViewTypes.EDIT_VIEW_TYPE_DELETE -> R.layout.item_edit_delete_widget
            else -> -1
        }
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        return EditViewHolder(view, viewType, editViewTypes)
    }


    override fun onBindViewHolder(holder: EditViewHolder, position: Int) {

        when(holder.itemViewType) {
            editViewTypes.EDIT_VIEW_TYPE_ACTIVITY_NAME -> {
                setupActivityName(holder)
            }

            editViewTypes.EDIT_VIEW_TYPE_NICKNAME -> {
                setupWidgetName(holder)
            }

            editViewTypes.EDIT_VIEW_TYPE_SHOW_NAME -> {
                setupShowName(holder)
            }

            editViewTypes.EDIT_VIEW_TYPE_SIZE -> {
                setupWidgetSize(holder)
            }

            editViewTypes.EDIT_VIEW_TYPE_DELETE -> {
                setupDeleteWidget(holder)
            }
        }

    }


    // 当 RecyclerView 离开屏幕时，移除观察者
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        removeObserver()
    }


    // 活动名称
    private fun setupActivityName(holder: EditViewHolder) {
        holder.textViewActivityName.text = originWidget.value?.activityName
    }


    // 组件名称的相关设置
    private fun setupWidgetName(holder: EditViewHolder) {
        // 设置组件名称的重置按钮
        setupButtonResetNickname(holder)

        // 设置组件名称的编辑框
        setupEditTextNickname(holder)
    }


    // 设置组件名称的重置按钮
    private fun setupButtonResetNickname(holder: EditViewHolder) {
        // 默认隐藏重置按钮
        holder.buttonResetNickname.visibility = View.GONE
        viewModel.widgetNameWasModified()
        widgetNameWasModifiedObserver = Observer { wasModified ->
            if(wasModified) {
                holder.buttonResetNickname.visibility = View.VISIBLE
            } else {
                holder.buttonResetNickname.visibility = View.GONE
            }
        }
        widgetNameWasModified.observe(lifecycleOwner, widgetNameWasModifiedObserver)

        // 重置按钮的点击事件
        holder.buttonResetNickname.setOnClickListener {
            VibrationHelper.vibrateOnClick(context)
            holder.editTextNickname.setText(originWidget.value?.appName)
            modifiedWidget.value?.nickName = originWidget.value?.appName.toString()
            //            viewModel.updateWidgetWasChanged()
            viewModel.widgetNameWasModified()
        }
    }


    // 设置组件名称的编辑框
    private fun setupEditTextNickname(holder: EditViewHolder) {
        holder.editTextNickname.let { editText ->
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

            // 点击输入框时触发振动
            editText.setOnClickListener {
                VibrationHelper.vibrateOnClick(context)
            }

            // 添加 TextWatcher 监听文本变化
            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val newText = s.toString()
                    viewModel.modifiedWidget.value?.nickName = newText
                    viewModel.widgetWasModified()
                    viewModel.widgetNameWasModified()
                }

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
            })
        }
    }


    // 是否显示组件名称的相关设置
    private fun setupShowName(holder: EditViewHolder) {
        holder.switchShowName.let { switch ->
            switch.isChecked = originWidget.value?.showName == true

            // 显示组件名称
            holder.itemView.setOnClickListener {
                VibrationHelper.vibrateOnClick(context)
                switch.isChecked = !switch.isChecked
                viewModel.modifiedWidget.value?.showName = switch.isChecked
                viewModel.widgetWasModified()
//                Log.i("showName", "isModified: ${viewModel.wasModified.value}")
//                Log.i(
//                    "showName",
//                    "origin: ${originWidget.value?.showName}, modified: ${modifiedWidget.value?.showName}"
//                )
            }
        }
    }


    // 组件大小的相关设置
    private fun setupWidgetSize(holder: EditViewHolder) {
        // 设置组件大小的重置按钮
        setupButtonResetWidgetSize(holder)

        // 显示组件大小
        showWidgetSize(holder)

        // 减小按钮
        holder.buttonWidgetSizeDecrease.setOnClickListener {
            if(modifiedWidget.value?.widgetSize == 1) {
                return@setOnClickListener
            }
            updateWidgetSize(holder, -1)
        }

        // 增大按钮
        holder.buttonWidgetSizeIncrease.setOnClickListener {
            if(modifiedWidget.value?.widgetSize == viewModel.maxWidgetSize) {
                return@setOnClickListener
            }
            updateWidgetSize(holder, 1)
        }
    }


    // 设置组件大小的重置按钮
    private fun setupButtonResetWidgetSize(holder: EditViewHolder) {
        // 默认隐藏重置按钮
        holder.buttonResetWidgetSize.visibility = View.GONE
        widgetSizeWasModifiedObserver = Observer { wasModified ->
            if(wasModified) {
                holder.buttonResetWidgetSize.visibility = View.VISIBLE
            } else {
                holder.buttonResetWidgetSize.visibility = View.GONE
            }
        }
        widgetSizeWasModified.observe(lifecycleOwner, widgetSizeWasModifiedObserver)

        // 重置按钮点击事件
        holder.buttonResetWidgetSize.setOnClickListener {
            VibrationHelper.vibrateOnClick(context)
            modifiedWidget.value?.widgetSize = originWidget.value?.widgetSize!!
            updateWidgetSize(holder, 0)
            //            viewModel.updateWidgetWasChanged()
            viewModel.widgetSizeWasModified()
        }
    }


    // 显示组件大小
    private fun showWidgetSize(holder: EditViewHolder) {
        // 显示组件大小
        holder.textViewWidgetSizeFraction.text = context.getString(
            R.string.widgetSize_fraction,
            modifiedWidget.value?.widgetSize,
            viewModel.maxWidgetSize,
        )
    }


    // 更新组件大小
    private fun updateWidgetSize(
        holder: EditViewHolder,
        diff: Int,
    ) {
        VibrationHelper.vibrateOnClick(context)
        modifiedWidget.value!!.widgetSize += diff
        viewModel.widgetWasModified()
        viewModel.widgetSizeWasModified()
        showWidgetSize(holder)
    }


    // 删除组件
    private fun setupDeleteWidget(holder: EditViewHolder) {
        // 设置修改字典
        viewModel.setupModifiedActivityNameToStorageActivityMap()
        holder.buttonDeleteWidget.let { button: Button ->
            button.setOnClickListener {
                VibrationHelper.vibrateOnClick(context)
                // 删除组件
                if(button.text == context.getString(R.string.delete_widget)) {
                    viewModel.deleteOrRestoreWidget(
                        isDelete = true,
                        modifiedWidget.value!!.activityName,
                    )
                    setupButtonDeleteOrRestoreWidget(button, false)
                } else { // 恢复组件
                    viewModel.deleteOrRestoreWidget(
                        isDelete = false,
                        modifiedWidget.value!!.activityName,
                    )
                    setupButtonDeleteOrRestoreWidget(button, true)
                }
            }
        }
    }


    // 设置删除组件按钮
    private fun setupButtonDeleteOrRestoreWidget(
        button: Button,
        isDelete: Boolean,
    ) {
        if(isDelete) {
            button.text = context.getString(R.string.delete_widget)
            button.setTextColor(context.getColor(R.color.red))
        } else {
            button.text = context.getString(R.string.restore_widget)
            button.setTextColor(context.getColor(R.color.green))
        }
    }


    // 移除观察者
    private fun removeObserver() {
        if(widgetNameWasModified.hasObservers()) {
            widgetNameWasModified.removeObserver(widgetNameWasModifiedObserver)
        }
        if(widgetSizeWasModified.hasObservers()) {
            widgetSizeWasModified.removeObserver(widgetSizeWasModifiedObserver)
        }
    }
}
