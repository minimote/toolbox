/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.SeekBarValueList.columnCountList
import cn.minimote.toolbox.constant.ToolConstants
import cn.minimote.toolbox.constant.ToolConstants.Alignment
import cn.minimote.toolbox.constant.ViewList
import cn.minimote.toolbox.constant.ViewTypes
import cn.minimote.toolbox.helper.DialogHelper
import cn.minimote.toolbox.helper.EditTextHelper
import cn.minimote.toolbox.helper.SeekBarHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.ui.widget.RadioRecyclerView
import cn.minimote.toolbox.viewModel.MyViewModel
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

    private val viewTypes = ViewTypes.Edit

    private lateinit var previewAdapter: WidgetPreviewAdapter


    inner class EditViewHolder(
        itemView: View,
        viewType: Int,
    ) : RecyclerView.ViewHolder(itemView) {

        lateinit var editTextNickname: EditText
        lateinit var buttonResetNickname: Button
        lateinit var imageButtonClear: ImageButton

        init {
            when(viewType) {

                // 昵称修改
                viewTypes.NICKNAME -> {
                    editTextNickname = itemView.findViewById(R.id.textView_editText)
                    buttonResetNickname = itemView.findViewById(R.id.button_reset)
                    imageButtonClear = itemView.findViewById(R.id.imageButton_clear)
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
        val layoutId =
            when(viewType) {
                viewTypes.PREVIEW -> R.layout.item_edit_preview

                viewTypes.NICKNAME -> R.layout.item_edit_edittext
                viewTypes.DISPLAY_MODE -> R.layout.item_edit_radio_recyclerview
                viewTypes.WIDTH -> R.layout.item_edit_seekbar
                viewTypes.ALIGNMENT -> R.layout.item_edit_radio_recyclerview
                viewTypes.DELETE -> R.layout.item_my_check_update
                else -> -1
            }
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        return EditViewHolder(view, viewType)
    }


    override fun onBindViewHolder(holder: EditViewHolder, position: Int) {

        when(holder.itemViewType) {

            viewTypes.PREVIEW -> {
                setPreview(holder)
            }

            viewTypes.NICKNAME -> {
                setupNickname(holder)
            }

            viewTypes.DISPLAY_MODE -> {
                setRadioGroup(
                    holder = holder,
                    name = context.getString(R.string.display_mode),
                    idToStringIdMap = ToolConstants.DisplayMode.idToStringIdMap,
                    initId = editedTool.value!!.displayMode,
                    onCheckedChangeListener = { selectedId ->
                        editedTool.value?.displayMode = selectedId
                        viewModel.updateToolDisplayModeChanged()
                        updatePreview()
                    },
                )
            }

            viewTypes.WIDTH -> {
                val textViewToolWidthFraction =
                    holder.itemView.findViewById<TextView>(R.id.textView_content)
                setSeekBar(
                    holder = holder,
                    valueList = columnCountList,
                    initPosition = editedTool.value!!.width,
                    callback = object : SeekBarHelper.SeekBarCallback {
                        override fun updateConfigValue(key: String, value: String) {
                            editedTool.value?.width = value.toInt()
                            viewModel.updateToolWidthChanged()
                            updatePreview()
                        }

                        override fun setupTextView() {
                            // 组件宽度
                            textViewToolWidthFraction.text = context.getString(
                                R.string.widgetSize_fraction,
                                editedTool.value?.width,
                                viewModel.maxWidgetSize,
                            )
                        }
                    },
                )
            }

            viewTypes.ALIGNMENT -> {
                setRadioGroup(
                    holder = holder,
                    name = context.getString(R.string.alignment),
                    idToStringIdMap = Alignment.idToStringIdMap,
                    initId = editedTool.value!!.alignment,
                    onCheckedChangeListener = { selectedId ->
                        editedTool.value?.alignment = selectedId
                        viewModel.updateToolAlignmentChanged()
                        updatePreview()
                    },
                )
            }

            viewTypes.DELETE -> {
                setDeleteTool(holder)
            }
        }

    }


    // 回收时移除观察者
    override fun onViewRecycled(holder: EditViewHolder) {
        super.onViewRecycled(holder)
        removeObserver()
    }


    // 设置预览
    private fun setPreview(holder: EditViewHolder) {
        val recyclerView = holder.itemView.findViewById<RecyclerView>(R.id.recyclerView)

        previewAdapter = WidgetPreviewAdapter(
            context = context,
            viewModel = viewModel,
        )
        recyclerView.adapter = previewAdapter

        // 使用 GridLayoutManager
        val gridLayoutManager = GridLayoutManager(context, viewModel.maxWidgetSize)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return previewAdapter.toolList[position].width
            }
        }
        recyclerView.layoutManager = gridLayoutManager
    }


    // 更新预览
    private fun updatePreview() {
//        notifyItemChanged(0)
        previewAdapter.submit()
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
            updatePreview()
        }
    }


    // 组件名称是否可以重置
    private fun updateToolNicknameNeedReset() {
        toolNicknameNeedReset.value = editedTool.value?.nickname != editedTool.value?.name
    }


    // 设置组件名称的编辑框
    private fun setupEditTextNickname(holder: EditViewHolder) {
        val stringText = originTool.value?.nickname ?: ""
        if(stringText != "") {
            holder.imageButtonClear.visibility = View.VISIBLE
        } else {
            holder.imageButtonClear.visibility = View.GONE
        }
        EditTextHelper.setEditTextAndClearButton(
            editText = holder.editTextNickname,
            stringText = stringText,
            stringHint = context.getString(R.string.can_be_empty),
            viewModel = viewModel,
            afterTextChanged = { s ->
                val newText = s.toString()
                editedTool.value?.nickname = newText
                viewModel.updateToolNicknameChanged()
                updateToolNicknameNeedReset()
                updatePreview()
            },
            imageButtonClear = holder.imageButtonClear,
        )
    }


    // 设置单选组
    private fun setRadioGroup(
        holder: EditViewHolder,
        name: String,
        idToStringIdMap: LinkedHashMap<String, Int>,
        initId: String,
        onCheckedChangeListener: (String) -> Unit = {},
    ) {
        val textViewName = holder.itemView.findViewById<TextView>(R.id.textView_name)
        textViewName.text = name

        val radioGroup = holder.itemView.findViewById<RadioRecyclerView>(R.id.radioRecyclerView)
        radioGroup.setRadioGroup(
            viewModel = viewModel,
            idToStringIdMap = idToStringIdMap,
            initId = initId,
            onCheckedChangeListener = onCheckedChangeListener,
        )
    }


    // 设置 SeekBar
    private fun setSeekBar(
        holder: EditViewHolder,
        valueList: List<String>,
        initPosition: Int,
        callback: SeekBarHelper.SeekBarCallback,
    ) {
        SeekBarHelper.setSeekBar(
            seekBar = holder.itemView.findViewById(R.id.seekBar),
            valueList = valueList,
            initPosition = initPosition,
            lastPosition = AtomicInteger(-1),
            viewModel = viewModel,
            callback = callback,
        )

    }


    // 删除组件
    private fun setDeleteTool(holder: EditViewHolder) {
        val textViewText = holder.itemView.findViewById<TextView>(R.id.textView_text)
        textViewText.text = context.getString(R.string.delete_widget)
        textViewText.setTextColor(context.getColor(R.color.red))

        val clickableContainer = holder.itemView.findViewById<ConstraintLayout>(R.id.clickable_container)

        clickableContainer.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            DialogHelper.showConfirmDialog(
                context = context, viewModel = viewModel, titleText = context.getString(
                    R.string.confirm_remove_from_home,
                    editedTool.value?.nickname,
                ), positiveAction = {
                    viewModel.removeFromSizeChangeMap(editedTool.value!!.id)
                    viewModel.saveWidgetList()
                    Toast.makeText(
                        context, context.getString(
                            R.string.already_remove_from_home,
                            editedTool.value?.nickname,
                        ), Toast.LENGTH_SHORT
                    ).show()
                    // 触发返回键行为
                    (context as? androidx.appcompat.app.AppCompatActivity)?.onBackPressedDispatcher?.onBackPressed()
                })
        }

    }


    // 移除观察者
    private fun removeObserver() {
        toolNicknameNeedReset.removeObserver(toolNicknameNeedResetObserver)
    }

}
