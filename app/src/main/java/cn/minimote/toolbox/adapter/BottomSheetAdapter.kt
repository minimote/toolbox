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
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.FragmentName
import cn.minimote.toolbox.constant.MenuType
import cn.minimote.toolbox.constant.ViewList.toolDetailList
import cn.minimote.toolbox.constant.ViewList.widgetDetailList
import cn.minimote.toolbox.dataClass.StoredTool
import cn.minimote.toolbox.dataClass.Tool
import cn.minimote.toolbox.helper.DialogHelper
import cn.minimote.toolbox.helper.EditTextHelper
import cn.minimote.toolbox.helper.FragmentHelper
import cn.minimote.toolbox.helper.ShortcutHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.MyViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog


class BottomSheetAdapter(
    private val context: Context,
    private val viewModel: MyViewModel,
    private val tool: Tool? = null,
    private val bottomSheetDialog: BottomSheetDialog,
    private val menuList: List<Int>,
    private val fragmentManager: FragmentManager? = null,
    private val viewPager: ViewPager2? = null,
    private val constraintLayoutOrigin: ConstraintLayout? = null,
    private val onMenuItemClick: (Int) -> Unit = {}// 回调函数
) : RecyclerView.Adapter<BottomSheetAdapter.BottomSheetHolder>() {

    inner class BottomSheetHolder(itemView: View, val viewType: Int) :
        RecyclerView.ViewHolder(itemView) {
        val textViewMenuItem: TextView = itemView.findViewById(R.id.textView_menuItem)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BottomSheetHolder {
        val layoutId = R.layout.item_bottom_sheet_menu
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)

        return BottomSheetHolder(view, viewType)
    }

    override fun onBindViewHolder(
        holder: BottomSheetHolder,
        position: Int
    ) {
        // 设置菜单项文本
        setMenuItemText(holder)
        // 设置菜单项点击事件
        setupMenuItemClickListener(holder)
    }


    // 设置菜单项文本
    private fun setMenuItemText(holder: BottomSheetHolder) {
        val textId = when(holder.viewType) {
            MenuType.CREATE_SHORTCUT -> R.string.create_shortcut
            MenuType.ADD_TO_HOME_OR_REMOVE_FROM_HOME ->
                if(viewModel.inSizeChangeMap(tool!!.id)) {
                    R.string.remove_from_home
                } else {
                    R.string.add_to_home
                }

            MenuType.EDIT_THIS_WIDGET -> R.string.edit_this_widget
            MenuType.MULTI_SELECT -> R.string.multi_select
            MenuType.SORT -> R.string.sort
            MenuType.CANCEL -> R.string.cancel
            MenuType.SAVE_IMAGE -> R.string.save_image
            MenuType.TOOL_DETAIL -> R.string.tool_detail
            MenuType.WIDGET_DETAIL -> R.string.tool_detail

            // 排序方式
            else -> when(holder.viewType) {
                MenuType.SortOrder.FREE_SORT -> R.string.free_sort

                MenuType.SortOrder.NAME_A_TO_Z -> R.string.sort_order_name_a_to_z
                MenuType.SortOrder.NAME_Z_TO_A -> R.string.sort_order_name_z_to_a

                MenuType.SortOrder.USE_CNT_LESS_TO_MORE -> R.string.sort_order_launch_cnt_less_to_more
                MenuType.SortOrder.USE_CNT_MORE_TO_LESS -> R.string.sort_order_launch_cnt_more_to_less

                MenuType.SortOrder.LAST_USED_TIME_EARLY_TO_LATE -> R.string.sort_order_last_launch_time_early_to_late
                MenuType.SortOrder.LAST_USED_TIME_LATE_TO_EARLY -> R.string.sort_order_last_launch_time_late_to_early

                MenuType.SortOrder.CREATED_TIME_EARLY_TO_LATE -> R.string.sort_order_create_time_early_to_late
                MenuType.SortOrder.CREATED_TIME_LATE_TO_EARLY -> R.string.sort_order_create_time_late_to_early

                MenuType.SortOrder.LAST_MODIFIED_TIME_EARLY_TO_LATE -> R.string.sort_order_modified_time_early_to_late
                MenuType.SortOrder.LAST_MODIFIED_TIME_LATE_TO_EARLY -> R.string.sort_order_modified_time_late_to_early
                else -> R.string.unknown
            }
        }
        holder.textViewMenuItem.text = context.getString(textId)

        if(viewModel.isWatch) {
            val paddingSize = context.resources.getDimension(R.dimen.layout_size_2_footnote).toInt()
            holder.textViewMenuItem.setPadding(
                paddingSize, paddingSize, paddingSize, paddingSize,
            )
        }
    }


    // 设置菜单项点击事件
    private fun setupMenuItemClickListener(holder: BottomSheetHolder) {
        holder.itemView.setOnClickListener {
            // 排序已经在 adapter 中调用 fragment 的按钮，产生振动了
            if(holder.viewType != MenuType.SORT) {
                VibrationHelper.vibrateOnClick(viewModel)
            }
            bottomSheetDialog.dismiss()
            viewPager?.isUserInputEnabled = true

            when(holder.viewType) {
                MenuType.CREATE_SHORTCUT -> {
                    createShortcut(context, tool!!)
                }

                MenuType.ADD_TO_HOME_OR_REMOVE_FROM_HOME -> {
                    addToHomeOrRemoveFromHome(context, tool!!)
                }

                MenuType.EDIT_THIS_WIDGET -> {
                    viewModel.originTool.value = tool as StoredTool
                    viewModel.editedTool.value = tool.copy()

                    FragmentHelper.switchFragment(
                        fragmentName = FragmentName.EDIT_LIST_FRAGMENT,
                        fragmentManager = fragmentManager!!,
                        viewModel = viewModel,
                        viewPager = viewPager,
                        constraintLayoutOrigin = constraintLayoutOrigin,
                    )
                }

                MenuType.MULTI_SELECT -> {
                    viewPager!!.isUserInputEnabled = false
                }

                MenuType.SORT -> {}

                MenuType.CANCEL -> {}

                MenuType.TOOL_DETAIL -> {
                    viewModel.originTool.value = tool!!.toStoredTool()
                    viewModel.updateDetailList(toolDetailList)
                    FragmentHelper.switchFragment(
                        fragmentName = FragmentName.DETAIL_LIST_FRAGMENT,
                        fragmentManager = fragmentManager!!,
                        viewModel = viewModel,
                        viewPager = viewPager,
                        constraintLayoutOrigin = constraintLayoutOrigin,
                    )
                }

                MenuType.WIDGET_DETAIL -> {
                    viewModel.originTool.value = tool as StoredTool
                    viewModel.updateDetailList(widgetDetailList)
                    FragmentHelper.switchFragment(
                        fragmentName = FragmentName.DETAIL_LIST_FRAGMENT,
                        fragmentManager = fragmentManager!!,
                        viewModel = viewModel,
                        viewPager = viewPager,
                        constraintLayoutOrigin = constraintLayoutOrigin,
                    )
                }

                // 排序方式
                else -> {
                    if(holder.viewType in MenuType.SortOrderSet) {
                        viewModel.sortModeString = holder.textViewMenuItem.text.toString()

                        viewPager!!.isUserInputEnabled = false
                        if(holder.viewType != MenuType.SortOrder.FREE_SORT) {
                            viewModel.freeSort = false
                            // 排序
                            viewModel.sortStoredToolList(holder.viewType)
                            Toast.makeText(
                                context,
                                context.getString(R.string.sort_finished),
                                Toast.LENGTH_SHORT,
                            ).show()
                        } else {
                            viewModel.freeSort = true
                            // 自由排序
                            Toast.makeText(
                                context,
                                context.getString(R.string.enter_free_sort),
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                        viewModel.sortMode.value = true
                    }
                }
            }

            onMenuItemClick(holder.viewType)
        }
    }


    override fun getItemCount(): Int = menuList.size


    override fun getItemViewType(position: Int): Int {
        return menuList[position]
    }


    // 创建桌面快捷方式
    private fun createShortcut(context: Context, tool: Tool) {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_dialog_create_shortcut, null)

        val dialog = DialogHelper.getCustomizeDialog(context, view)

        val editTextNickname: EditText = view.findViewById(R.id.editText_nickName)
        val imageButtonClear: ImageButton = view.findViewById(R.id.imageButton_clear)
        val buttonReset: Button = view.findViewById(R.id.button_reset)
        val buttonCancel: Button = view.findViewById(R.id.button_cancel)
        val buttonConfirm: Button = view.findViewById(R.id.button_confirm)

        val paddingSize = context.resources.getDimension(R.dimen.layout_size_2_footnote).toInt()
        if(viewModel.isWatch) {
            buttonReset.setPadding(
                paddingSize, paddingSize, paddingSize, paddingSize,
            )
            val layoutParams = editTextNickname.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.bottomMargin = 0
            editTextNickname.layoutParams = layoutParams
        }


        fun setButtonReset() {
            if(editTextNickname.text.toString() != tool.name) {
                buttonReset.visibility = View.VISIBLE
            } else {
                buttonReset.visibility = View.INVISIBLE
            }
        }

        fun setConfirmButton() {
            if(editTextNickname.text.isNotBlank()) {
                buttonConfirm.setTextColor(context.getColor(R.color.primary))
            } else {
                buttonConfirm.setTextColor(context.getColor(R.color.mid_gray))
            }
        }


        EditTextHelper.setEditTextAndClearButton(
            editText = editTextNickname,
            stringText = tool.nickname,
            stringHint = context.getString(R.string.cannot_be_blank),
            viewModel = viewModel,
            imageButtonClear = imageButtonClear,
            afterTextChanged = {
                setButtonReset()
                setConfirmButton()
            }
        )


        setButtonReset()
        setConfirmButton()


        buttonReset.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            editTextNickname.setText(tool.name)
            // 将光标移动到文本框最后
            editTextNickname.setSelection(editTextNickname.text.length)
        }

        buttonCancel.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            dialog.dismiss()
        }

        buttonConfirm.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)

            if(editTextNickname.text.isNotBlank()) {
                ShortcutHelper.createShortcut(
                    context = context,
                    viewModel = viewModel,
                    tool = tool,
                    shortLabel = editTextNickname.text.toString(),
                    longLabel = tool.description?.ifBlank { tool.nickname } ?: tool.name,
                )
                dialog.dismiss()
            } else {
                Toast.makeText(
                    context,
                    R.string.cannot_be_blank,
                    Toast.LENGTH_SHORT,
                ).show()
            }

        }

        dialog.show()
    }


    // 添加到主页或从主页移除
    private fun addToHomeOrRemoveFromHome(context: Context, tool: Tool) {
        // 已经在主页：显示从主页移除
        if(viewModel.inSizeChangeMap(tool.id)) {
            DialogHelper.showConfirmDialog(
                context = context,
                viewModel = viewModel,
                titleText = context.getString(
                    R.string.confirm_remove_from_home,
                    tool.nickname,
                ),
                positiveAction = {
                    viewModel.removeFromSizeChangeMap(tool.id)
                    viewModel.saveWidgetList()
                    // 触发无组件提示
                    viewModel.toolListSizeChanged.value = true
                    viewModel.toolListSizeChanged.value = false
                    Toast.makeText(
                        context,
                        context.getString(
                            R.string.already_remove_from_home,
                            tool.nickname,
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                },
            )
        } else {
            // 不在主页：添加到主页
            viewModel.addToSizeChangeMap(tool)
            viewModel.saveWidgetList()
            Toast.makeText(
                context,
                context.getString(R.string.add_success),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }
}