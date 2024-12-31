/*
 * Copyright (c) 2024 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EditWidgetFragment(
    private val widgetInfo: WidgetInfo,
    private val onSave: () -> Unit
) :
    Fragment() {

    private lateinit var appIcon: ImageView
    private lateinit var widgetNameEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var editListRecyclerView: RecyclerView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_list, container, false)

        // 初始化 RecyclerView
        editListRecyclerView = view.findViewById(R.id.fragment_edit_list)
        editListRecyclerView.layoutManager = LinearLayoutManager(context)

        return view
    }
}
