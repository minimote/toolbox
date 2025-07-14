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
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.ViewList
import cn.minimote.toolbox.constant.ViewType
import cn.minimote.toolbox.helper.ClipboardHelper
import cn.minimote.toolbox.helper.DialogHelper
import cn.minimote.toolbox.helper.SeekBarHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.MyViewModel
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.concurrent.atomic.AtomicInteger


class EditListAdapter(
    private val context: Context,
    val viewModel: MyViewModel,
    private val lifecycleOwner: androidx.lifecycle.LifecycleOwner,
) : RecyclerView.Adapter<EditListAdapter.EditViewHolder>() {

    private val editList = ViewList.editList
    private var originTool = viewModel.originTool
    private var editedTool = viewModel.editedTool

    private var toolNicknameNeedReset: MutableLiveData<Boolean> = MutableLiveData()
    // 观察者
    private lateinit var toolNicknameNeedResetObserver: Observer<Boolean>

    private val viewTypes = ViewType.Edit


    inner class EditViewHolder(
        itemView: View,
        viewType: Int,
    ) : RecyclerView.ViewHolder(itemView) {
        lateinit var textViewPackageName: TextView

        lateinit var textViewActivityName: TextView

        lateinit var editTextNickname: EditText
        lateinit var buttonResetNickname: Button

        lateinit var switchShowName: SwitchMaterial

        lateinit var textViewToolWidthFraction: TextView
        lateinit var seekBar: SeekBar
        var lastPosition: Int = -1

        lateinit var buttonDeleteTool: Button

        init {
            when(viewType) {
                // 包名
                viewTypes.PACKAGE_NAME -> {
                    textViewPackageName = itemView.findViewById(R.id.textView_nickName)
                }

                // 活动名
                viewTypes.ACTIVITY_NAME -> {
                    textViewActivityName = itemView.findViewById(R.id.textView_activityName)
                }

                // 显示名称修改
                viewTypes.NICKNAME -> {
                    editTextNickname = itemView.findViewById(R.id.textView_nickName)
                    buttonResetNickname = itemView.findViewById(R.id.button_reset_nickName)
                }

                // 是否显示名称
                viewTypes.SHOW_NAME -> {
                    switchShowName = itemView.findViewById(R.id.switch_whether_show_widgetName)
                }

                // 组件大小修改
                viewTypes.SIZE -> {
//                    buttonResetWidgetSize = itemView.findViewById(R.id.button_reset_widgetSize)
                    textViewToolWidthFraction =
                        itemView.findViewById(R.id.textView_widgetSize_fraction)
                    seekBar = itemView.findViewById(R.id.seekBar)
//                    buttonWidgetSizeDecrease =
//                        itemView.findViewById(R.id.button_widgetSize_decrease)
//                    buttonWidgetSizeIncrease =
//                        itemView.findViewById(R.id.button_widgetSize_increase)
                }

                // 删除组件
                viewTypes.DELETE -> {
                    buttonDeleteTool = itemView.findViewById(R.id.button_removeFromHome)
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
            viewTypes.PACKAGE_NAME -> R.layout.item_edit_package_name
            viewTypes.ACTIVITY_NAME -> R.layout.item_edit_activity_name
            viewTypes.NICKNAME -> R.layout.item_edit_nickname
            viewTypes.SHOW_NAME -> R.layout.item_edit_whether_show_name
            viewTypes.SIZE -> R.layout.item_edit_widget_width
            viewTypes.DELETE -> R.layout.item_edit_remove_from_home
            else -> -1
        }
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        return EditViewHolder(view, viewType)
    }


    override fun onBindViewHolder(holder: EditViewHolder, position: Int) {

        when(holder.itemViewType) {
            viewTypes.PACKAGE_NAME -> {
                setupPackageName(holder)
            }


            viewTypes.ACTIVITY_NAME -> {
                setupActivityName(holder)
            }

            viewTypes.NICKNAME -> {
                setupNickname(holder)
            }

            viewTypes.SHOW_NAME -> {
                setupShowName(holder)
            }

            viewTypes.SIZE -> {
                setupToolWidth(holder)
            }

            viewTypes.DELETE -> {
                setupDeleteTool(holder)
            }
        }

    }


    // 回收时移除观察者
    override fun onViewRecycled(holder: EditViewHolder) {
        super.onViewRecycled(holder)
        removeObserver()
    }


    // 包名
    private fun setupPackageName(holder: EditViewHolder) {
        holder.textViewPackageName.text = originTool.value?.packageName
        // 禁用振动反馈
        holder.itemView.isHapticFeedbackEnabled = false
        holder.itemView.setOnLongClickListener {
            VibrationHelper.vibrateOnLongPress(viewModel)
            ClipboardHelper.copyToClipboard(
                context = context,
                text = holder.textViewPackageName.text as String,
                label = context.getString(R.string.textView_package_name),
            )
            true
        }
    }


    // 活动名
    private fun setupActivityName(holder: EditViewHolder) {
        holder.textViewActivityName.text = originTool.value?.activityName
        // 禁用振动反馈
        holder.itemView.isHapticFeedbackEnabled = false
        holder.itemView.setOnLongClickListener {
            VibrationHelper.vibrateOnLongPress(viewModel)
            ClipboardHelper.copyToClipboard(
                context = context,
                text = holder.textViewActivityName.text as String,
                label = context.getString(R.string.textView_activity_name),
            )
            true
        }
    }


    // 组件名称的相关设置
    private fun setupNickname(holder: EditViewHolder) {
        // 设置组件名称的重置按钮
        setupButtonResetNickname(holder)

        // 设置组件名称的编辑框
        setupEditTextNickname(holder)
    }


    // 设置组件名称的重置按钮
    private fun setupButtonResetNickname(holder: EditViewHolder) {
        // 默认隐藏重置按钮
        toolNicknameNeedResetObserver = Observer { wasModified ->
            if(wasModified) {
                holder.buttonResetNickname.visibility = View.VISIBLE
            } else {
                holder.buttonResetNickname.visibility = View.INVISIBLE
            }
        }
        toolNicknameNeedReset.observe(lifecycleOwner, toolNicknameNeedResetObserver)
        updateToolNicknameNeedReset()

        // 重置按钮的点击事件
        holder.buttonResetNickname.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            holder.editTextNickname.setText(originTool.value?.name)
            editedTool.value?.nickname = originTool.value?.name.toString()
            // 将光标移动到文本框最后
            holder.editTextNickname.setSelection(holder.editTextNickname.text.length)
            // viewModel.updateWidgetWasChanged()
            updateToolNicknameNeedReset()
        }
    }


    // 组件名称是否被修改
    private fun updateToolNicknameNeedReset() {
        toolNicknameNeedReset.value = editedTool.value?.nickname != editedTool.value?.name
    }


    // 设置组件名称的编辑框
    private fun setupEditTextNickname(holder: EditViewHolder) {
        holder.editTextNickname.let { editText ->
            editText.setText(originTool.value?.nickname)

            // 手动请求输入法，避免第一次点击出现闪烁
            editText.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                if(hasFocus) {
                    VibrationHelper.vibrateOnClick(viewModel)
                    val imm =
                        v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
                }
            }

            // 点击输入框时触发振动
            editText.setOnClickListener {
                VibrationHelper.vibrateOnClick(viewModel)
            }

            // 添加 TextWatcher 监听文本变化
            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val newText = s.toString()
                    editedTool.value?.nickname = newText
                    viewModel.updateToolNicknameChanged()
                    updateToolNicknameNeedReset()
                }

                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?, start: Int, before: Int, count: Int
                ) {
                }
            })
        }
    }


    // 是否显示组件名称的相关设置
    private fun setupShowName(holder: EditViewHolder) {
        holder.switchShowName.let { switch ->
            switch.isChecked = originTool.value?.showName == true

            // 显示组件名称
            holder.itemView.setOnClickListener {
                VibrationHelper.vibrateOnClick(viewModel)
                switch.isChecked = !switch.isChecked
                editedTool.value?.showName = switch.isChecked
                viewModel.updateToolShowNameChanged()
            }
        }
    }


    // 组件宽度的相关设置
    private fun setupToolWidth(holder: EditViewHolder) {

        SeekBarHelper.setupSeekBar(
            seekBar = holder.seekBar,
            valueList = (viewModel.minWidgetSize..viewModel.maxWidgetSize).map {
                it.toString()
            },
            initPosition = editedTool.value!!.width,
            lastPosition = AtomicInteger(holder.lastPosition),
            viewModel = viewModel,
            callback = object : SeekBarHelper.SeekBarSetupCallback {
                override fun updateConfigValue(key: String, value: String) {
                    editedTool.value?.width = value.toInt()
                    viewModel.updateToolWidthChanged()
                }

                override fun setupTextView() {
                    showWidgetSize(holder)
                }
            },
        )

    }


    // 显示组件大小
    private fun showWidgetSize(holder: EditViewHolder) {
        // 显示组件大小
        holder.textViewToolWidthFraction.text = context.getString(
            R.string.widgetSize_fraction,
            editedTool.value?.width,
            viewModel.maxWidgetSize,
        )
    }


    // 删除组件
    private fun setupDeleteTool(holder: EditViewHolder) {
        holder.buttonDeleteTool.let { button: Button ->
            button.setOnClickListener {
                VibrationHelper.vibrateOnClick(viewModel)
                DialogHelper.showConfirmDialog(
                    context = context,
                    viewModel = viewModel,
                    titleText = context.getString(
                        R.string.confirm_remove_from_home,
                        editedTool.value?.nickname,
                    ),
                    positiveAction = {
                        viewModel.removeFromSizeChangeMap(editedTool.value!!.id)
                        viewModel.saveWidgetList()
                        Toast.makeText(
                            context,
                            context.getString(
                                R.string.already_remove_from_home,
                                editedTool.value?.nickname,
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                        // 触发返回键行为
                        (context as? androidx.appcompat.app.AppCompatActivity)?.onBackPressedDispatcher?.onBackPressed()
                    }
                )
            }
        }
    }


    // 移除观察者
    private fun removeObserver() {
        toolNicknameNeedReset.removeObserver(toolNicknameNeedResetObserver)
    }
}
