/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.ViewTypes
import cn.minimote.toolbox.viewModel.MyViewModel


class DialogAdapter(
    val viewModel: MyViewModel,
    val context: Context,
    val dataList: List<String>,
    val gravity: Int,
    val textColor: Int?,
) : RecyclerView.Adapter<DialogAdapter.DialogViewHolder>() {

    class DialogViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {
//
    }


    override fun getItemCount(): Int = dataList.size


    override fun getItemViewType(position: Int): Int {
        return when(dataList[position]) {
            context.getString(R.string.download_progress) -> {
                ViewTypes.Dialog.DOWNLOAD
            }

            context.getString(R.string.create_shortcut) -> {
                ViewTypes.Dialog.CREATE_SHORTCUT
            }

            else -> {
                ViewTypes.Dialog.TEXT
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogViewHolder {
        val layoutId = when(viewType) {
            ViewTypes.Dialog.DOWNLOAD -> {
                R.layout.layout_dialog_download
            }

            ViewTypes.Dialog.CREATE_SHORTCUT -> {
                R.layout.layout_dialog_create_shortcut
            }

            else -> {
                R.layout.layout_dialog_message
            }
        }

        val view = LayoutInflater.from(context).inflate(
            layoutId, parent, false
        )

        return DialogViewHolder(view)
    }


    override fun onBindViewHolder(holder: DialogViewHolder, position: Int) {

        when(holder.itemViewType) {
            ViewTypes.Dialog.DOWNLOAD -> {
            }

            ViewTypes.Dialog.CREATE_SHORTCUT -> {
//                createShortcut(holder, tool)
            }

            else -> {
                val textViewMessage = holder.itemView.findViewById<TextView>(R.id.textView_message)
                val message = dataList[position]
                textViewMessage.text = message
                textViewMessage.gravity = gravity
                textColor?.let {
                    textViewMessage.setTextColor(it)
                }
            }
        }


    }


//    private fun createShortcut(holder: DialogViewHolder, tool: Tool) {
////        val view =
////            LayoutInflater.from(context).inflate(R.layout.layout_dialog_create_shortcut, null)
////
////        val dialog = DialogHelper.getCustomizeDialog(context, view)
//        val view = holder.itemView
//
//        val editTextNickname: EditText = view.findViewById(R.id.editText_nickName)
//        val imageButtonClear: ImageButton = view.findViewById(R.id.imageButton_clear)
//        val buttonReset: Button = view.findViewById(R.id.button_reset)
//        val buttonCancel: Button = view.findViewById(R.id.button_cancel)
//        val buttonConfirm: Button = view.findViewById(R.id.button_confirm)
//        val imageViewIcon: ImageView = view.findViewById(R.id.imageView_icon)
//
//        imageViewIcon.setImageIcon(viewModel.getHighResIcon(tool))
//
//
////        val paddingSize = getLayoutSize(
////            myActivity, R.dimen.layout_size_2_footnote
////        )
////        if(viewModel.isWatch) {
////            buttonReset.setPadding(
////                paddingSize, paddingSize, paddingSize, paddingSize,
////            )
////            val layoutParams = editTextNickname.layoutParams as ViewGroup.MarginLayoutParams
////            layoutParams.bottomMargin = 0
////            editTextNickname.layoutParams = layoutParams
////        }
//
//
//        fun setButtonReset() {
//            if(editTextNickname.text.toString() != tool.name) {
//                buttonReset.visibility = View.VISIBLE
//            } else {
//                buttonReset.visibility = View.INVISIBLE
//            }
//        }
//
//        fun setConfirmButton() {
//            if(editTextNickname.text.isNotBlank()) {
//                buttonConfirm.setTextColor(context.getColor(R.color.primary))
//            } else {
//                buttonConfirm.setTextColor(context.getColor(R.color.mid_gray))
//            }
//        }
//
//
//        EditTextHelper.setEditTextAndClearButton(
//            editText = editTextNickname,
//            stringText = tool.nickname,
//            stringHint = context.getString(R.string.cannot_be_blank),
//            viewModel = viewModel,
//            imageButtonClear = imageButtonClear,
//            afterTextChanged = {
//                setButtonReset()
//                setConfirmButton()
//            },
//            onClick = {
//                VibrationHelper.vibrateOnClick(viewModel)
//            },
//            onFocusGained = {
//                VibrationHelper.vibrateOnClick(viewModel)
//            },
//            onClickClearButton = {
//                VibrationHelper.vibrateOnClick(viewModel)
//            },
//        )
//
//
//        setButtonReset()
//        setConfirmButton()
//
//
//        buttonReset.setOnClickListener {
//            VibrationHelper.vibrateOnClick(viewModel)
//            editTextNickname.setText(tool.name)
//            // 将光标移动到文本框最后
//            editTextNickname.setSelection(editTextNickname.text.length)
//        }
//
//        buttonCancel.setOnClickListener {
//            VibrationHelper.vibrateOnClick(viewModel)
//            dialog.dismiss()
//        }
//
//        buttonConfirm.setOnClickListener {
//            VibrationHelper.vibrateOnClick(viewModel)
//
//            if(editTextNickname.text.isNotBlank()) {
//                ShortcutHelper.createShortcut(
//                    context = context,
//                    viewModel = viewModel,
//                    tool = tool,
//                    shortLabel = editTextNickname.text.toString(),
//                    longLabel = tool.description?.ifBlank { tool.nickname } ?: tool.name,
//                )
//                dialog.dismiss()
//            } else {
//                Toast.makeText(
//                    context,
//                    R.string.cannot_be_blank,
//                    Toast.LENGTH_SHORT,
//                ).show()
//            }
//
//        }
//
//        dialog.showMyDialog(viewModel)
//    }


}
