/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.UI
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.MyViewModel
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class RadioRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : RecyclerView(context, attrs) {

    private val adapter = RadioGroupAdapter()
    private var selectedId = ""
    lateinit var viewModel: MyViewModel
    lateinit var onCheckedChangeListener: (selectedId: String) -> Unit

    init {
        setAdapter(adapter)

        // 使用 FlexboxLayoutManager 实现自适应列数
        val flexboxLayoutManager = FlexboxLayoutManager(context)
        flexboxLayoutManager.flexDirection = FlexDirection.ROW
        flexboxLayoutManager.justifyContent = JustifyContent.CENTER
        flexboxLayoutManager.alignItems = AlignItems.CENTER
        flexboxLayoutManager.flexWrap = FlexWrap.WRAP  // 确保可以换行

        layoutManager = flexboxLayoutManager

        // 禁用触摸滚动，避免与外部RecyclerView冲突
        isNestedScrollingEnabled = false
    }

    private fun selectedItem(id: String, invokeListener: Boolean = true) {
        // 更新 RadioButton 的选中状态
        adapter.updateSelection(id)
        selectedId = id

        if(invokeListener) {
            onCheckedChangeListener.invoke(id)
        }
    }

    fun setRadioGroup(
        viewModel: MyViewModel,
        idToStringIdMap: LinkedHashMap<String, Int>,
        initId: String,
        onCheckedChangeListener: (selectedId: String) -> Unit = {},
    ) {
        this.viewModel = viewModel
        this.onCheckedChangeListener = onCheckedChangeListener

        adapter.setItems(idToStringIdMap)
        selectedItem(initId, false)
    }


    inner class RadioGroupAdapter : Adapter<RadioGroupAdapter.ViewHolder>() {
        private var items: List<Pair<String, String>> = listOf() // Pair of (id, text)
        private val viewHolderMap = mutableMapOf<String, ViewHolder>()

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val radioButton: RadioButton = itemView.findViewById(R.id.radioButton)
            val textViewName: TextView = itemView.findViewById(R.id.textView_name)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_radio, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val (id, text) = items[position]
            holder.textViewName.text = text
            holder.itemView.alpha = UI.Alpha.ALPHA_5

            // 将 ViewHolder 保存到 map 中，以便直接更新
            viewHolderMap[id] = holder
            if(id == selectedId) {
                updateSelection(id)
            } else {
                holder.radioButton.isChecked = false
            }

            // 设置 FlexItem 属性，让子项自动填充剩余空间
            val layoutParams = holder.itemView.layoutParams as FlexboxLayoutManager.LayoutParams
            // 设置 flexGrow 为 1，使子项填充剩余空间
            layoutParams.flexGrow = 1f

            holder.itemView.setOnClickListener {
                // 点击相同项时，只会振动，不会重新选中
                if(id != selectedId) {
                    selectedItem(id)
                }
                VibrationHelper.vibrateOnClick(viewModel)
            }
        }

        override fun getItemCount(): Int = items.size

        override fun onViewRecycled(holder: ViewHolder) {
            super.onViewRecycled(holder)
            // 当 ViewHolder 被回收时，从 map 中移除
            items.getOrNull(holder.bindingAdapterPosition)?.let { (id, _) ->
                viewHolderMap.remove(id)
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setItems(idToStringIdMap: LinkedHashMap<String, Int>) {
            // 清空之前的 ViewHolder 引用
            viewHolderMap.clear()

            this.items = idToStringIdMap.map { (id, stringId) ->
                Pair(id, context.getString(stringId))
            }
            notifyDataSetChanged()
        }

        fun updateSelection(newId: String) {
            val oldId = selectedId
            // 直接更新 RadioButton 的选中状态
            viewHolderMap[oldId]?.radioButton?.isChecked = false
            viewHolderMap[oldId]?.itemView?.alpha = UI.Alpha.ALPHA_5

            viewHolderMap[newId]?.radioButton?.isChecked = true
            viewHolderMap[newId]?.itemView?.alpha = UI.Alpha.ALPHA_10
        }
    }
}
